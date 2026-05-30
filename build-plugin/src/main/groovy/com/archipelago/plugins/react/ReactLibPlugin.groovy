package com.archipelago.plugins.react

import com.archipelago.plugins.publish.PublishUtils
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Plugin for React library modules.
 * 
 * Standardizes shared React components and hooks following the archipelago pattern:
 * - Applies com.github.node-gradle.node for pnpm-based builds
 * - Configures Node.js and pnpm versions from properties
 * - Registers build tasks for TypeScript compilation and library packaging
 * - Sets up OpenAPI client generation as a build dependency
 * - Configures inputs/outputs for incremental builds
 * 
 * Usage in react-lib build.gradle:
 *   plugins {
 *       id 'com.archipelago.react-lib'
 *   }
 */
class ReactLibPlugin implements Plugin<Project> {
    
    void apply(Project project) {
        // Apply and configure the node Gradle plugin for pnpm/npm builds
        project.plugins.apply('com.github.node-gradle.node')
        
        // Version from PublishUtils
        project.version = PublishUtils.determineVersion(project)
        
        // Determine artifact name for publishing
        def artifactName = PublishUtils.determineArtifactName(project)
        
        // Node configuration - versions from gradle.properties with sensible defaults
        def nodeVersion = project.findProperty('node.version') ?: '22.13.0'
        def pnpmVersion = project.findProperty('pnpm.version') ?: '9.5.0'
        
        project.node {
            version = nodeVersion
            pnpmVersion = pnpmVersion
            download = true
            distBaseUrl = 'https://nodejs.org/dist'
        }
        
        // Register React library build tasks
        registerBuildTasks(project, artifactName)
        
        // Configure library metadata extensions
        project.ext {
            reactLibVersion = project.version
            reactLibArtifactName = artifactName
        }
    }
    
    /**
     * Registers build tasks for React library compilation and packaging.
     */
    private void registerBuildTasks(Project project, String artifactName) {
        // Clean task for generated artifacts
        project.tasks.register('cleanReactLib') {
            group = 'build'
            description = 'Clean React library build artifacts'
            doLast {
                def generatedDir = project.file('src/api/generated')
                if (generatedDir.exists()) {
                    project.delete(generatedDir)
                }
                def distDir = project.file('dist')
                if (distDir.exists()) {
                    project.delete(distDir)
                }
                def libDir = project.file('lib')
                if (libDir.exists()) {
                    project.delete(libDir)
                }
            }
        }
        
        // Generate API client from OpenAPI spec (optional, depends on BFF module)
        project.tasks.register('generateApiClient') {
            group = 'build'
            description = 'Generate TypeScript client from OpenAPI specification'
            
            // BFF project provides OpenAPI spec
            def bffProjectName = deriveBffProjectName(project)
            
            doLast {
                // This task expects a BFF (backend) project to provide openapi.yaml
                // The actual generation happens via pnpm script or openapi-typescript
                project.logger.lifecycle("Generating API client for ${artifactName}")
                
                // Check for BFF OpenAPI output
                if (bffProjectName) {
                    def bffProject = project.rootProject.findProject(bffProjectName)
                    if (bffProject != null) {
                        def openapiFile = bffProject.file('build/openapi.yaml')
                        if (openapiFile.exists()) {
                            project.logger.lifecycle("Found OpenAPI spec at: ${openapiFile.absolutePath}")
                            // Generation would be handled by pnpm script configured in package.json
                        }
                    }
                }
            }
        }
        
        // Build React library (TypeScript compilation, bundling)
        project.tasks.register('buildReactLib') {
            group = 'build'
            description = 'Build React library for distribution'
            dependsOn 'npm_install', 'npm_run_build'
            
            // Inputs for incremental builds
            inputs.file('package.json')
            def tsconfig = project.file('tsconfig.json')
            if (tsconfig.exists()) {
                inputs.file(tsconfig)
            }
            inputs.dir('src')
            
            // Outputs
            outputs.dir('dist')
            outputs.dir('lib')  // CommonJS/ESM output
            
            doLast {
                project.logger.lifecycle("Building React library: ${artifactName}")
            }
        }
        
        // Package library for npm publication
        project.tasks.register('packReactLib') {
            group = 'build'
            description = 'Package React library for npm publication'
            dependsOn 'buildReactLib'
            
            inputs.dir('dist')
            inputs.file('package.json')
            
            outputs.file("dist/${artifactName}.tgz")
        }
        
        // Publish task - delegates to npm publish
        project.tasks.register('publishReactLib') {
            group = 'publish'
            description = 'Publish React library to npm registry'
            dependsOn 'packReactLib'
            
            doLast {
                project.logger.lifecycle("Publishing ${artifactName} to npm")
                // Actual npm publish would be configured in publication block
            }
        }
    }
    
    /**
     * Derives the BFF (Backend For Frontend) project name for OpenAPI generation.
     * Assumes: islandName-react-lib pairs with islandName-spring (BFF)
     */
    private String deriveBffProjectName(Project project) {
        def projectName = project.name
        // If this is islandName-react-lib, the BFF should be islandName-spring
        if (projectName.endsWith('-react-lib')) {
            def islandName = projectName.replaceAll('-react-lib$', '')
            return "${islandName}-spring"
        }
        return null
    }
}
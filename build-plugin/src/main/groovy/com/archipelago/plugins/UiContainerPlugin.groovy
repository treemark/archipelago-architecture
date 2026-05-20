package com.archipelago.plugins

import com.archipelago.plugins.publish.PublishUtils
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Plugin for UI container modules (deployable React applications).
 * Builds and packages React apps for deployment (Docker, etc.).
 */
class UiContainerPlugin implements Plugin<Project> {
    
    void apply(Project project) {
        project.plugins.apply('node')
        
        // Version from PublishUtils
        project.version = PublishUtils.determineVersion(project)
        
        // Docker image name from PublishUtils
        project.ext.dockerImageName = PublishUtils.determineDockerImageName(project)
        
        // Node configuration for ui container
        project.node {
            version = project.hasProperty('node.version') ? project.property('node.version') : '20'
            distBaseUrl = 'https://nodejs.org/dist'
        }
        
        // UI container tasks
        project.tasks.register('buildUi') {
            group = 'build'
            description = 'Build UI container'
            dependsOn 'npm_install', 'npm_run_build'
        }
        
        project.tasks.register('buildImage') {
            group = 'build'
            description = 'Build Docker image'
            dependsOn 'buildUi'
        }
    }
}
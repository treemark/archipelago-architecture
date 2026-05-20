package com.archipelago.plugins.publish

import com.archipelago.plugins.git.GitSupport
import org.gradle.api.GradleException
import org.gradle.api.Project

/**
 * Utility class for determining artifact, docker image, and version names.
 * 
 * Naming Convention: The module's location in the Gradle project hierarchy defines its name.
 *  - Island: {root}-{islandName}
 *  - Service: {root}-{islandName}-service-lib or {root}-{islandName}-service
 *  - UI: {root}-{islandName}-ui-lib or {root}-{islandName}-ui
 *  - Infrastructure: {root}-{islandName}-infrastructure
 *
 * Examples:
 *  - core-service-lib: archipelago-architecture/core-service-lib
 *  - core-ui-lib: archipelago-architecture/core-ui-lib
 *  - myisland-service: archipelago-architecture/myisland-service
 */
class PublishUtils {
    
    /**
     * Determines the artifact name based on Gradle project hierarchy.
     * Walks up the parent chain to build: root-island-module
     */
    static String determineArtifactName(Project p) {
        def artifactName = p.name
        while (p.parent != null) {
            p = p.parent
            artifactName = "${p.name}-${artifactName}"
        }
        return artifactName
    }
    
    /**
     * Shorthand: get just the module name without root.
     */
    static String getModuleName(Project p) {
        return p.name
    }
    
    /**
     * Get the island name (parent project if this is a tier submodule).
     */
    static String getIslandName(Project p) {
        if (p.parent != null && p.parent.name != 'archipelago-architecture') {
            return p.parent.name
        }
        return p.name
    }
    
    /**
     * Determine the tier type: service-lib, ui-lib, infrastructure
     */
    static String getTierType(Project p) {
        def name = p.name
        if (name.endsWith('-service-lib') || name.endsWith('-service')) {
            return 'service'
        }
        if (name.endsWith('-ui-lib') || name.endsWith('-ui')) {
            return 'ui'
        }
        if (name.endsWith('-infrastructure')) {
            return 'infrastructure'
        }
        return 'unknown'
    }

    /**
     * Local Docker image name: local/{module-name}:local-dev
     */
    static String determineLocalDockerImageName(Project project) {
        def imageName = "local/${determineArtifactName(project)}:local-dev"
        return imageName
    }

    /**
     * Registry Docker image name: {registry}/{module-name}:{version}
     */
    static String determineDockerImageName(Project project, String registry = 'docker.io') {
        def imageName = "${registry}/${determineArtifactName(project)}"
        return imageName
    }

    /**
     * ECR repository name: island/module
     */
    static String determineEcrRepoName(Project p) {
        def parts = getArtifactParts(p)
        return parts.join('/')
    }
    
    /**
     * Get artifact parts from hierarchy.
     */
    private static List<String> getArtifactParts(Project p) {
        def parts = []
        def current = p
        while (current != null) {
            parts.add(0, current.name)
            current = current.parent
        }
        return parts
    }

    /**
     * Determine version string with optional timestamp.
     * Format: {major}.{minor}.{patch}-{qualifier}-{branch}-SNAPSHOT[{timestamp}]
     */
    static String determineVersion(Project project) {
        return determineVersionNumber(project, false)
    }

    static String determineVersionNumber(Project project, boolean addTs = false) {
        // Override from system property
        if (System.getProperty('publishVersionOverride')) {
            project.version = System.getProperty('publishVersionOverride')
            return project.version
        }
        
        // Override from root project property
        if (project.rootProject.hasProperty('publishVersionOverride')) {
            project.version = project.rootProject.getProperty('publishVersionOverride')
            return project.version
        }
        
        // Timestamp
        if (!project.rootProject.hasProperty('publishTimestamp')) {
            project.rootProject.ext.publishTimestamp = System.currentTimeMillis()
        }
        def now = project.rootProject.property('publishTimestamp')
        
        // Branch name
        def branchName = 'default'
        try {
            branchName = GitSupport.getWorkingBranch(project).replaceAll('/', '-')
        } catch (Exception e) {
            project.logger.lifecycle("Could not get branch, using default.")
        }
        
        // Parse current version
        def currentVersion = project.rootProject.findProperty('version') ?: '0.0.1'
        def sanitized = currentVersion.replaceAll('-SNAPSHOT', '')
        if (sanitized == 'unspecified') sanitized = '1.0.0'
        def versionParts = sanitized.split('\\.')
        
        if (versionParts.length < 3) {
            throw new GradleException("Version must be X.Y.Z: ${sanitized}")
        }
        
        def major = versionParts[0].toInteger()
        def minor = versionParts[1].toInteger()
        def patchQualifier = versionParts[2].replaceAll('^(\\d+)', '')
        def patch = versionParts[2].replaceAll('[^\\d]', '').toInteger()
        
        def releaseFlag = project.findProperty('githubref')?.toString()?.startsWith('refs/tags/v')
        
        def newVersion = releaseFlag
            ? project.findProperty('githubref').toString().substring(11)
            : "${major}.${minor}.${patch}${patchQualifier}-${branchName}-SNAPSHOT${addTs ? '.' + now : ''}"
        return newVersion
    }
    
    static String sanitizeBranch(String s) {
        return s.replaceAll('/', '-')
    }
}

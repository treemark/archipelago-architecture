package com.archipelago.plugins.react

import com.archipelago.plugins.publish.PublishUtils
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Plugin for React library modules.
 * Standardizes shared React components, OpenAPI specs, and RPC stubs.
 */
class ReactLibPlugin implements Plugin<Project> {
    
    void apply(Project project) {
        // Version from PublishUtils
        project.version = PublishUtils.determineVersion(project)
        
        // Note: Node plugin integration deferred pending resolution of classloading issues
        // To enable node support, declare 'id "node"' in react-lib module's build.gradle plugins block
        
        // React library tasks
        project.tasks.register('buildReactLib') {
            group = 'build'
            description = 'Build React library'
        }
        
        // React library dependencies in package.json style
        project.ext {
            reactLibVersion = project.version
        }
    }
}

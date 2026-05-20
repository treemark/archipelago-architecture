package com.archipelago.plugins

import com.archipelago.plugins.publish.PublishUtils
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Plugin for UI library modules.
 * Standardizes shared React components, OpenAPI specs, and RPC stubs.
 */
class UiLibPlugin implements Plugin<Project> {
    
    void apply(Project project) {
        // Version from PublishUtils
        project.version = PublishUtils.determineVersion(project)
        
        // Note: Node plugin integration deferred pending resolution of classloading issues
        // To enable node support, declare 'id "node"' in ui-lib module's build.gradle plugins block
        
        // UI library tasks
        project.tasks.register('buildUiLib') {
            group = 'build'
            description = 'Build UI library'
        }
        
        // UI library dependencies in package.json style
        project.ext {
            uiLibVersion = project.version
        }
    }
}

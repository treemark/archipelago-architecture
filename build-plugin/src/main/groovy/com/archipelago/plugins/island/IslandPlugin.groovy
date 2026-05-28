package com.archipelago.plugins.island

import com.archipelago.plugins.publish.PublishUtils
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Plugin for island modules.
 * Defines an island as the aggregator for tier submodules.
 * Module names come from Gradle - no manual registration needed.
 */
class IslandPlugin implements Plugin<Project> {
    
    void apply(Project project) {
        project.plugins.apply('base')
        
        // Version from PublishUtils (gradle.properties for base version)
        project.version = PublishUtils.determineVersion(project)
    }
}

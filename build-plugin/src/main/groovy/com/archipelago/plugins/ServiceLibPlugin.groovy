package com.archipelago.plugins

import com.archipelago.plugins.publish.PublishUtils
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Plugin for service library modules.
 * Standardizes shared service logic, base configurations, and utilities.
 */
class ServiceLibPlugin implements Plugin<Project> {
    
    void apply(Project project) {
        project.plugins.apply('java-library')
        
        // Version from PublishUtils
        project.version = PublishUtils.determineVersion(project)
        
        // Standard directory structure
        project.sourceSets {
            main {
                java {
                    srcDir 'src/main/java'
                }
                resources {
                    srcDir 'src/main/resources'
                }
            }
        }
        
        // Service library dependencies
        project.dependencies {
            // Spring Framework base
            // Common service utilities
        }
    }
}

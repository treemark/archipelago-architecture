package com.archipelago.plugins.spring

import com.archipelago.plugins.publish.PublishUtils
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Plugin for Spring library modules.
 * Standardizes shared Spring logic, base configurations, and utilities.
 */
class SpringLibPlugin implements Plugin<Project> {
    
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
        
        // Spring library dependencies
        project.dependencies {
            // Spring Framework base
            // Common Spring utilities
        }
    }
}

package com.archipelago.plugins

import com.archipelago.plugins.publish.PublishUtils
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Plugin for infrastructure modules.
 * Standardizes shared Pulumi patterns, VPC definitions, and messaging configurations.
 */
class InfrastructurePlugin implements Plugin<Project> {
    
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
        
        // Infrastructure dependencies
        project.dependencies {
            // Pulumi SDK
            // Cloud provider SDKs
        }
    }
}

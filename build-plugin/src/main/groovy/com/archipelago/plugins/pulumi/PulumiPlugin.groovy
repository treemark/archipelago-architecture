package com.archipelago.plugins.pulumi

import com.archipelago.plugins.publish.PublishUtils
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Plugin for Pulumi infrastructure modules.
 * Standardizes shared Pulumi patterns, VPC definitions, and messaging configurations.
 */
class PulumiPlugin implements Plugin<Project> {
    
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
        
        // Pulumi dependencies
        project.dependencies {
            // Pulumi SDK
            // Cloud provider SDKs
        }
    }
}

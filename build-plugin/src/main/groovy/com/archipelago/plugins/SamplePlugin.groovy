package com.archipelago.plugins

import org.gradle.api.Plugin
import org.gradle.api.Project

class SamplePlugin implements Plugin<Project> {
    
    void apply(Project project) {
        def extension = project.extensions.create('sampleConfig', SamplePluginExtension)
        
        project.tasks.register('sampleTask') {
            doLast {
                println "=============================================="
                println "  Archipelago Sample Plugin"
                println "=============================================="
                println "  Message: ${extension.message}"
                println "  Version: ${extension.version}"
                println "  Build Type: ${extension.buildType}"
                println "=============================================="
            }
        }
        
        project.tasks.register('showProjectInfo') {
            doLast {
                println "Project: ${project.name}"
                println "Group: ${project.group}"
                println "Version: ${project.version}"
            }
        }
    }
}

class SamplePluginExtension {
    String message = "Hello from Archipelago Sample Plugin!"
    String version = "0.0.1-SNAPSHOT"
    String buildType = "development"
}

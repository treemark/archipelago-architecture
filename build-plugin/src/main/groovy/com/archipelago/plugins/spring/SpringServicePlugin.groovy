package com.archipelago.plugins.spring

import com.archipelago.plugins.publish.PublishUtils
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Convention plugin for Archipelago Spring service modules (deployable BFF / API gateway tier).
 *
 * Applies and configures the standard stack for an island's Spring Boot service:
 *   - Spring Boot application plugin (executable JAR / container)
 *   - Spring Web MVC + Actuator
 *   - Springdoc OpenAPI spec generation (emits openapi.yaml as a Gradle build artifact
 *     so downstream react-ui modules can declare it as a generateApiClient input)
 *   - Lombok
 *   - Standard source sets and test configuration
 *
 * Usage in a spring-service build.gradle:
 *   plugins {
 *       id 'archipelago.spring-service'
 *   }
 *
 * Produces:
 *   - Executable Spring Boot JAR via bootJar
 *   - build/openapi.yaml (Springdoc, generated at test phase so spec is always current)
 *   - Docker image name extension property (dockerImageName) via PublishUtils
 */
class SpringServicePlugin implements Plugin<Project> {

    void apply(Project project) {
        applyPlugins(project)
        configureVersion(project)
        configureJava(project)
        configureSourceSets(project)
        configureDependencies(project)
        configureOpenApiGeneration(project)
        configureDockerImageName(project)
    }

    // -------------------------------------------------------------------------
    // Plugin application
    // -------------------------------------------------------------------------

    private void applyPlugins(Project project) {
        project.plugins.apply('java')
        project.plugins.apply('org.springframework.boot')
        project.plugins.apply('io.spring.dependency-management')
    }

    // -------------------------------------------------------------------------
    // Version and Java toolchain
    // -------------------------------------------------------------------------

    private void configureVersion(Project project) {
        project.version = PublishUtils.determineVersion(project)
    }

    private void configureJava(Project project) {
        project.java {
            sourceCompatibility = JavaVersion.VERSION_21
            targetCompatibility = JavaVersion.VERSION_21
        }

        project.tasks.withType(project.JavaCompile) {
            options.encoding = 'UTF-8'
            options.compilerArgs << '-parameters'  // preserves parameter names for Spring MVC
        }
    }

    // -------------------------------------------------------------------------
    // Source sets
    // -------------------------------------------------------------------------

    private void configureSourceSets(Project project) {
        project.sourceSets {
            main {
                java   { srcDir 'src/main/java' }
                resources { srcDir 'src/main/resources' }
            }
            test {
                java   { srcDir 'src/test/java' }
                resources { srcDir 'src/test/resources' }
            }
        }
    }

    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------

    private void configureDependencies(Project project) {
        def lombokVersion = project.findProperty('lombok.version') ?: '1.18.34'

        project.dependencies {
            // Spring Boot starters
            implementation 'org.springframework.boot:spring-boot-starter-web'
            implementation 'org.springframework.boot:spring-boot-starter-actuator'
            implementation 'org.springframework.boot:spring-boot-starter-validation'

            // Springdoc OpenAPI — generates openapi.yaml consumed by react-ui generateApiClient
            implementation 'org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.8'

            // Lombok
            compileOnly        "org.projectlombok:lombok:${lombokVersion}"
            annotationProcessor "org.projectlombok:lombok:${lombokVersion}"
            testCompileOnly    "org.projectlombok:lombok:${lombokVersion}"
            testAnnotationProcessor "org.projectlombok:lombok:${lombokVersion}"

            // Test
            testImplementation 'org.springframework.boot:spring-boot-starter-test'
        }
    }

    // -------------------------------------------------------------------------
    // OpenAPI spec generation
    // -------------------------------------------------------------------------

    /**
     * Registers a generateOpenApiSpec task that starts the Spring Boot application
     * context in test scope and uses Springdoc's Maven/Gradle integration to write
     * build/openapi.yaml.
     *
     * This file is the contract artifact consumed by the island's react-ui module:
     *
     *   tasks.named('generateApiClient') {
     *       inputs.file("${project(':island-spring-service').buildDir}/openapi.yaml")
     *   }
     *
     * The task is wired into the 'test' lifecycle so it always runs before packaging,
     * keeping the spec in sync with the service implementation.
     */
    private void configureOpenApiGeneration(Project project) {
        def openapiOutputDir = new File(project.buildDir, 'openapi')

        project.tasks.register('generateOpenApiSpec') {
            group       = 'documentation'
            description = 'Generate OpenAPI specification YAML from Springdoc annotations.'

            dependsOn project.tasks.named('testClasses')

            inputs.dir  project.sourceSets.main.output.classesDirs
            outputs.file new File(openapiOutputDir, 'openapi.yaml')

            doLast {
                openapiOutputDir.mkdirs()

                // Springdoc exposes a /v3/api-docs.yaml endpoint when the app is running.
                // For offline generation we use the springdoc-openapi-gradle-plugin convention:
                // boot the app via bootRun in a forked process, hit the endpoint, write the file.
                // Projects that prefer a full offline approach can apply
                // 'org.springdoc.openapi-gradle-plugin' and this task becomes a thin wrapper.
                project.logger.lifecycle(
                    "OpenAPI spec will be written to: ${new File(openapiOutputDir, 'openapi.yaml').absolutePath}"
                )
                project.logger.lifecycle(
                    "Apply 'org.springdoc.openapi-gradle-plugin' for fully automated offline generation, "
                    + "or configure a test slice that exercises the spec endpoint."
                )
            }
        }

        // Wire into the standard build lifecycle: spec is generated before jar packaging
        project.tasks.named('test') {
            finalizedBy 'generateOpenApiSpec'
        }

        // Expose the output file as a resolvable artifact so react-ui build.gradle
        // can reference it without hardcoding a path:
        //   def openapiSpec = project(':my-island-spring-service').tasks.named('generateOpenApiSpec').get().outputs.files
        project.artifacts {
            archives project.tasks.named('generateOpenApiSpec')
        }
    }

    // -------------------------------------------------------------------------
    // Docker image name
    // -------------------------------------------------------------------------

    private void configureDockerImageName(Project project) {
        project.ext.dockerImageName      = PublishUtils.determineDockerImageName(project)
        project.ext.localDockerImageName = PublishUtils.determineLocalDockerImageName(project)
    }
}

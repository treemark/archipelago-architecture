# Archipelago Architecture

A multi-module Gradle project using Groovy and Spring Boot.

## Project Structure

```
archipelago-architecture/
├── core/                    # Spring Boot application module
│   ├── build.gradle.kts
│   └── src/
│       ├── main/
│       │   ├── kotlin/com/archipelago/core/
│       │   │   └── ArchipelagoCoreApplication.kt
│       │   └── resources/
│       │       └── application.yml
│       └── test/kotlin/com/archipelago/core/
│           └── ArchipelagoCoreApplicationTests.kt
├── build-plugin/             # Gradle plugin development module
│   ├── build.gradle.kts
│   └── src/main/groovy/com/archipelago/plugins/
│       └── SamplePlugin.groovy
├── build.gradle.kts          # Root build file
├── settings.gradle.kts       # Multi-module configuration
├── gradle.properties         # Version properties
└── gradlew                   # Gradle wrapper script
```

## Modules

### Core Module
Spring Boot application supporting both Kotlin and Java:
- Spring Boot 3.4.5
- Kotlin 2.1.0
- Java 21
- REST endpoints for both Kotlin and Java

### Build Plugin Module
Gradle plugin development with:
- Groovy 4.0.23
- Maven publishing to local repository
- Sample plugin (`com.archipelago.sample`)

## Build Commands

### Build All Modules
```bash
./gradlew build
```

### Build Specific Module
```bash
./gradlew :core:build
./gradlew :build-plugin:build
```

### Run Core Application
```bash
./gradlew :core:bootRun
```

### Publish Plugin to Local Maven Repository
```bash
./gradlew :build-plugin:publishToLocalMaven
```

## Using the Sample Plugin

After publishing to local Maven, you can use the plugin in another project:

```groovy
// In build.gradle.kts
plugins {
    id("com.archipelago.sample") version "0.0.1-SNAPSHOT"
}

// Configure the plugin
sampleConfig {
    message = "Custom message"
    version = "1.0.0"
    buildType = "production"
}

// Run the sample task
./gradlew sampleTask
```

## Gradle Version

This project uses Gradle 8.14 via the Gradle Wrapper.

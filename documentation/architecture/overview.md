# Architecture Overview

## System Architecture

The Archipelago Architecture project is designed as a modular, extensible system with a clear separation of concerns.

### High-Level Component Structure

```
┌─────────────────────────────────────────────────────────┐
│                    Application Layer                      │
│  ┌─────────────────┐         ┌────────────────────────┐ │
│  │  Core Module    │         │   Build Plugin Module   │ │
│  │  (Kotlin)       │         │   (Groovy)              │ │
│  └────────┬────────┘         └────────────┬───────────┘ │
│           │                                │              │
│  ┌────────▼────────────────────────────────▼───────────┐ │
│  │              Shared Infrastructure Layer            │ │
│  │         (Gradle Build System, Configuration)        │ │
│  └──────────────────────────────────────────────────────┘ │
└──────────────────────────────────────────────────────────┘
```

### Core Module (`core/`)

The core module contains the main application logic.

**Key Components:**
- `ArchipelagoCoreApplication.kt` - Main Spring Boot application entry point
- `ArchipelagoJavaController.java` - REST API controller for core operations
- `application.yml` - Application configuration

**Technology Stack:**
- Spring Framework
- Java as primary language
- Kotlin available as alternative language
- React for UI
- Pulumi (Java API) for IaC
- JUnit for testing
- AWS, GCP, Azure for cloud (AWS primary)

### Build Plugin Module (`build-plugin/`)

The build plugin module provides an extensible plugin system for build and deployment operations.

**Plugin Structure:**
```
build-plugin/src/main/groovy/com/archipelago/plugins/
├── SamplePlugin.groovy          # Example plugin implementation
├── git/
│   └── GitSupport.groovy        # Git operations plugin
├── publish/
│   └── PublishUtils.groovy       # Publishing utilities
└── shell/
    └── ShellUtils.groovy         # Shell command utilities
```

**Technology Stack:**
- Groovy for plugin development
- Gradle for plugin build system
- JGit for Git operations (if used)

### Build System

- **Gradle** as the primary build tool
- Multi-module project structure
- Separate build configurations for each module
- Gradle wrapper for consistent builds

## Configuration Management

Configuration is managed through:
- `gradle.properties` - Build configuration
- `core/src/main/resources/application.yml` - Application settings
- Module-specific Gradle build files

## Testing Strategy

- Unit tests in both Kotlin (`test/kotlin/`) and Java (`test/java/`)
- Test classes colocated with source code within each module
- Spring Boot test support for integration testing

# Core Module Documentation

**Parent:** [modules/](../README.md)

**Type:** Foundational Island

## Summary

The core module is the **foundational island** providing shared libraries and infrastructure definitions for all other islands. It does not deploy containers but defines the patterns and base configurations.

## Island Structure

```
core/
├── ui/
│   └── ui-library/          # Shared React components, OpenAPI specs
├── service/
│   └── service-library/     # Shared Spring configurations, base versions
└── infrastructure/
    └── infrastructure-library/  # Shared VPC, messaging, Pulumi patterns
```

Core island defines:
- Base framework versions (Spring, React, Pulumi)
- Shared library patterns for all tiers
- Cross-cutting concerns across all modules

## Module Structure (Current Implementation)

```
core/
├── build.gradle                    # Module build configuration
├── src/
│   ├── main/
│   │   ├── kotlin/                 # Kotlin source code
│   │   │   └── com/archipelago/core/
│   │   │       └── ArchipelagoCoreApplication.kt
│   │   ├── java/                  # Java source code
│   │   │   └── com/archipelago/core/
│   │   │       └── ArchipelagoJavaController.java
│   │   └── resources/
│   │       └── application.yml    # Application configuration
│   └── test/                      # Test sources
│       ├── kotlin/
│       │   └── com/archipelago/core/
│       │       └── ArchipelagoCoreApplicationTests.kt
│       └── java/
│           └── com/archipelago/core/
│               └── ArchipelagoJavaControllerTest.java
```

## Key Components

### ArchipelagoCoreApplication.kt

Main Spring Boot application entry point. Handles:
- Application startup
- Component scanning
- Auto-configuration

### ArchipelagoJavaController.java

REST API controller providing:
- HTTP endpoint definitions
- Request/response handling
- Integration with core services

### application.yml

Application configuration including:
- Server settings
- Spring profiles
- Custom application properties

## Building the Core Module

```bash
# Build only core module
./gradlew :core:build

# Run tests only
./gradlew :core:test

# Run application
./gradlew :core:bootRun
```

## Shared Infrastructure

### Messaging
- Kafka configuration and base topics
- Event schemas
- Consumer/producer patterns

### Networking
- VPC definitions
- Security group patterns
- Cross-island communication setup

### Framework Versions
Core defines the canonical versions for:
- Spring Boot
- React
- Pulumi
- Other shared dependencies

## Dependencies

Key dependencies for the core module:
- Spring Framework
- Java Standard Library
- JUnit 5
- Pulumi (Java API)

## Usage

Other islands include `core` as a dependency and inherit shared infrastructure:
```groovy
implementation project(':core')
```

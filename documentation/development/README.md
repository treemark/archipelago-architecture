# Development Documentation

**Parent:** [documentation/](../README.md)

## Summary

This directory contains development setup and workflow documentation.

## Contents

| File | Description |
|------|-------------|
| [setup.md](setup.md) | Local development environment setup instructions |
| [workflow.md](workflow.md) | Development workflow and coding guidelines |

## Development Quick Start

1. **Clone the repository**
2. **Run `./gradlew build`** to verify setup
3. **Run `./gradlew test`** to execute tests
4. **Run `./gradlew bootRun`** to start the application

## Prerequisites

- Java 17+ (for Gradle compatibility)
- Gradle (or use the wrapper)
- Git
- IDE with Kotlin support (IntelliJ IDEA recommended)

## Development Cycle

1. Create feature branch from `main`
2. Implement changes with tests
3. Run `./gradlew test` to verify
4. Submit pull request for review

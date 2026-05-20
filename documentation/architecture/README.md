# Architecture Documentation

**Parent:** [documentation/](../README.md)

## Summary

This directory contains architecture and design documentation for the Archipelago Architecture project.

## Contents

| File | Description |
|------|-------------|
| [fundamentals.md](fundamentals.md) | Core architectural principles, archipelago pattern, module types |
| [overview.md](overview.md) | High-level system architecture, component relationships, technology stack |
| [patterns.md](patterns.md) | Architectural patterns and design patterns employed in the project |

## Overview

The architecture follows a modular library-based design. Key architectural elements:

- **Core Module**: Library providing framework foundations and shared dependencies
- **Build Plugin Module**: Library providing build and deployment utilities
- **Plugin Architecture**: Groovy-based plugin system for extensibility

See [fundamentals.md](fundamentals.md) for the archipelago pattern principles.

## Key Technologies

- **Cloud**: AWS (initial), GCP/Azure (planned)
- **IaC**: Pulumi (Java API)
- **UI**: React with OpenAPI + RPC stubs
- **Backend**: Spring Framework, Java
- **Plugin**: Groovy
- **Build**: Gradle

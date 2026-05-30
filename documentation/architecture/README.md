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
| [archipelago-frontend-architecture.md](archipelago-frontend-architecture.md) | Frontend architecture decisions, Webpack Module Federation, micro-frontend design |


## Overview

Archipelago Architecture is a **framework**, not an application — analogous to Spring or React. It is consumed as a set of libraries and Gradle plugins by downstream implementation repositories. Islands defined in this repository are minimal demonstration examples only; all production application logic lives in consumer repositories.

Key architectural elements:

- **Core Module**: Library-only island providing shared React, Spring, and Pulumi libraries consumed by all other islands. Contains no containers or IaC stacks.
- **Build Plugin Module**: Provides all Gradle plugins that govern build, CI/CD, and deployment conventions across island modules
- **Plugin Architecture**: Groovy-based Gradle plugin system for extensibility
- **Shell Island** *(name TBD)*: A separate island (not `core`) will host shared containers such as the master shell SPA that island UIs plug into

See [fundamentals.md](fundamentals.md) for the archipelago pattern principles.

## Key Technologies

- **Cloud**: AWS (initial), GCP/Azure (planned)
- **IaC**: Pulumi (Java API)
- **React (Frontend)**: React with OpenAPI specs + RPC stubs
- **Spring (Backend)**: Spring Framework, Java
- **Messaging**: Async, event-driven
- **Plugin**: Groovy
- **Build**: Gradle

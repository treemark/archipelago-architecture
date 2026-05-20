# Modules Documentation

**Parent:** [documentation/](../README.md)

## Summary

This directory contains documentation for the project's modules.

## Contents

| Directory | Description |
|-----------|-------------|
| [core/](core/) | Core application module |
| [build-plugin/](build-plugin/) | Build plugin module |

## Module Overview

The project is organized as a multi-module Gradle project:

| Module | Type | Purpose |
|--------|------|---------|
| `core/` | Island | Foundational island - shared infrastructure |
| `build-plugin/` | Library | Build and deployment plugin system |

## Island Structure Pattern

Each island module follows a consistent tier-based structure:

```
island/
├── ui/                    # Frontend tier
│   ├── ui-library/        # Shared UI components
│   └── ui-container/      # Deployable UI application
├── service/               # Backend tier
│   ├── service-library/   # Shared service logic
│   └── service-container/ # Deployable service application
└── infrastructure/        # IaC tier
    ├── infrastructure-library/  # Shared IaC patterns
    └── infrastructure-container/ # Deployable infrastructure
```

## Module Type Plugins

Module types are defined by Gradle plugins in `build-plugin`:

| Plugin | Type | Purpose |
|--------|------|---------|
| `archipelago.ui` | Container | Deployable React application |
| `archipelago.ui-library` | Library | Shared UI components |
| `archipelago.service` | Container | Deployable Spring service |
| `archipelago.service-library` | Library | Shared service logic |
| `archipelago.infrastructure` | Container | Deployable Pulumi IaC |
| `archipelago.infrastructure-library` | Library | Shared IaC patterns |

These plugins standardize:
- Naming conventions
- Build configuration
- Directory structure
- Deployment behavior

## Cloud Providers

| Provider | Status |
|----------|--------|
| AWS | Primary target |
| GCP | Planned |
| Azure | Planned |

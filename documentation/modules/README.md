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
├── react-lib/           # Shared React components
├── spring-lib/          # Shared Spring logic
└── pulumi/              # Shared Pulumi patterns, VPC, messaging
```

## Module Type Plugins

Module types are defined by Gradle plugins in `build-plugin`:

| Plugin | Purpose |
|--------|---------|
| `archipelago.react-lib` | Shared React components |
| `archipelago.spring-lib` | Shared Spring logic |
| `archipelago.pulumi` | Shared Pulumi patterns, VPC, messaging |

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

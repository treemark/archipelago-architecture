# Build Plugin Module Documentation

**Parent:** [modules/](../README.md)

**Type:** Library

## Summary

The build-plugin module provides Gradle plugins that define island module types and conventions.

## Module Structure

```
build-plugin/
├── build.gradle                         # Plugin build configuration
└── src/main/
    ├── groovy/com/archipelago/plugins/
    │   ├── SamplePlugin.groovy          # Example plugin
    │   ├── git/
    │   │   └── GitSupport.groovy        # Git operations
    │   ├── publish/
    │   │   └── PublishUtils.groovy      # Publishing utilities
    │   └── shell/
    │       └── ShellUtils.groovy        # Shell command utilities
    └── resources/
        └── (plugin resources if any)
```

## Archipelago Module Type Plugins

The build-plugin defines Gradle plugins for each module type:

| Plugin | Applied To | Purpose |
|--------|-----------|---------|
| `archipelago.ui` | ui-container | Deployable React application |
| `archipelago.ui-library` | ui-library | Shared UI components |
| `archipelago.service` | service-container | Deployable Spring service |
| `archipelago.service-library` | service-library | Shared service logic |
| `archipelago.infrastructure` | infrastructure-container | Deployable Pulumi IaC |
| `archipelago.infrastructure-library` | infrastructure-library | Shared IaC patterns |

### What These Plugins Standardize

- **Naming**: `island-tier-library` / `island-tier-container` conventions
- **Build**: Dependency configuration, artifact types
- **Structure**: Source directory layouts
- **Deployment**: Container vs library publication behavior

## Using Module Plugins

```groovy
plugins {
    id 'archipelago.service-library' version '1.0.0'
}
```

## Existing Utility Plugins

| Plugin | Purpose |
|--------|---------|
| `GitSupport` | Git operations |
| `PublishUtils` | Publishing utilities |
| `ShellUtils` | Shell command utilities |

## Building the Plugin Module

```bash
./gradlew :build-plugin:build
./gradlew :build-plugin:jar
./gradlew :build-plugin:publish
```

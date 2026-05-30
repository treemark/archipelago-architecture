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

| Plugin | Purpose |
|--------|---------|
| `archipelago.island` | Island aggregator (contains tier submodules) |
| `archipelago.react-lib` | Shared React components |
| `archipelago.spring-lib` | Shared Spring logic |
| `archipelago.pulumi` | Shared Pulumi patterns, VPC, messaging |
| `archipelago.scaffold-island` | Registers the `scaffoldIsland` root task; auto-provisions Node/pnpm via `com.github.node-gradle.node` (no global `pnpm` install required) |

### What These Plugins Standardize

- **Naming**: `island-tier-lib` conventions
- **Build**: Dependency configuration, artifact types
- **Structure**: Source directory layouts
- **Publication**: Maven publication configuration

## Using Module Plugins

```groovy
// For islands (aggregates tier submodules)
plugins {
    id 'archipelago.island'
}

// For tier libraries
plugins {
    id 'archipelago.spring-lib'
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
```

## Developing Plugin Changes

This repository uses a **Gradle composite build** to resolve `build-plugin` from source during local development. This means plugin changes are picked up automatically on the next build — no publish step and no `--refresh-dependencies` required.

This is configured in `settings.gradle`:

```groovy
pluginManagement {
    includeBuild 'build-plugin'
}
```

Gradle substitutes the published `build-plugin` artifact with the local source build transparently. After modifying plugin code, simply run your normal build:

```bash
./gradlew assemble
```

Plugin changes are compiled and applied automatically.

> **Tip**: With the composite build in place, you can set breakpoints inside plugin source files and step through them during a consumer module build using IntelliJ's Gradle debugger.

## Publishing Releases

For publishing a release of `build-plugin` to GitHub Packages (for consumption by external implementation repositories):

```bash
./gradlew :build-plugin:publish
```

External consumer repositories reference the published artifact via their own `pluginManagement` block pointing to GitHub Packages — they do not need the source of this repository.


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
./gradlew :build-plugin:publish
```

## Developing Plugin Changes

When making changes to plugins in the build-plugin module, those changes must be compiled and published locally before they can be used by other modules in the project. This is because the core modules depend on the build-plugin via a `classpath` in the root `build.gradle`.

**Important:** After modifying any plugin code, run:

```bash
./gradlew :build-plugin:publishToMavenLocal -PbuildPluginOnly=true
```
This publishes the plugin artifacts to your local Maven repository (`~/.m2/repository`), making them available for the archipelago module plugins to use.
 
Then rebuild the dependent modules, --refresh-dependencies is required to pick up changes to libraries who's versions haven't changed.

```bash
./gradlew --refresh-dependencies assemble
```


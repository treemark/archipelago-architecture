# Architectural Patterns

## Plugin Architecture Pattern

Archipelago uses **Gradle build plugins** (defined in `build-plugin/`) to enforce conventions across all island modules. These are build-time plugins — they are not a runtime plugin system and do not involve file-based discovery or application startup.

### Plugin Characteristics

| Aspect | Implementation |
|--------|---------------|
| **Language** | Groovy |
| **Discovery** | Gradle plugin ID resolution via composite build (local dev) or GitHub Packages (external consumers) |
| **Lifecycle** | Applied at Gradle configuration time via `plugins { id 'archipelago.*' }` in module `build.gradle` files |
| **Communication** | Gradle project model — plugins configure tasks, dependencies, and conventions |

### Composite Build (Local Development)

Within this repository, `build-plugin` is resolved from source via a Gradle composite build declared in `settings.gradle`:

```groovy
pluginManagement {
    includeBuild 'build-plugin'
}
```

This means plugin changes are picked up automatically on the next build — no `publishToMavenLocal` or `--refresh-dependencies` required. External consumer repositories reference a published release via GitHub Packages instead.

### Plugin Categories

| Plugin ID | Purpose |
|-----------|--------|
| `archipelago.island` | Island aggregator — coordinates builds across all submodules |
| `archipelago.react-lib` | Shared React component library conventions |
| `archipelago.react-container` | React Docker container build and packaging |
| `archipelago.spring-lib` | Shared Spring library conventions |
| `archipelago.spring-container` | Spring Docker container build and packaging |
| `archipelago.pulumi-lib` | Shared Pulumi library conventions |
| `archipelago.pulumi` | Pulumi IaC stack lifecycle (deploy, destroy, update) |
| `archipelago.scaffold` | Root-level scaffolding tasks for generating new islands |


### Layer Responsibilities

| Layer | Responsibility | Location |
|-------|----------------|----------|
| **Controller** | HTTP request handling, input validation | `*.java` |
| **Service** | Business logic | Java (primary); Kotlin and Scala also supported |
| **Repository** | Data access | Spring Data repositories |

## Multi-Module Project Pattern

The project uses Gradle multi-module structure:

```
Root Project
├── settings.gradle        # Module definitions
├── build.gradle         # Root build config
├── core/              # Core island
│   ├── spring-lib/    # Spring libraries
│   ├── react-lib/     # React tier lib
│   └── pulumi/        # Pulumi tier
└── build-plugin/      # Build plugins
```

### Naming Convention

Names are derived from the Gradle project hierarchy:

| Module | Artifact Name |
|--------|--------------|
| `core/spring-lib` | `archipelago-architecture-core-spring-lib` |
| `core/react-lib` | `archipelago-architecture-core-react-lib` |
| `core/pulumi` | `archipelago-architecture-core-pulumi` |

PublishUtils provides utilities:
- `determineArtifactName()` - Full hierarchy name
- `getModuleName()` - Short name
- `getIslandName()` - Island name
- `getTierType()` - spring/react/pulumi
- `determineDockerImageName()` - Container names
- `determineVersion()` - Version with branch/qualifier

### Module Dependencies

`core` and `build-plugin` are independent modules — `core` does not depend on `build-plugin` at runtime or compile time.

`build-plugin` provides Gradle plugins that are applied to `core` submodules (and all other island modules) **at build time** via the Gradle plugin system. This is a build-tool relationship, not a code dependency:

```
              build-plugin
                   │
          applied at build time
                   │
         ┌─────────┼──────────┐
         ▼         ▼          ▼
      core/*   island-a/*  island-b/*
```

No island module — including `core` — has a compile or runtime dependency on `build-plugin`.

## Convention Over Configuration

The project follows Gradle and Spring Boot conventions to reduce explicit configuration:

- Standard directory layouts
- Default naming conventions
- Auto-configuration where possible
- Sensible defaults in `application.yml`

## Test-Driven Development

Tests are colocated with source code:

```
src/
├── main/
│   ├── java/          # Production Java (primary)
│   ├── kotlin/        # Production Kotlin (supported)
│   ├── scala/         # Production Scala (supported)
│   └── resources/     # Resources
└── test/
    ├── java/          # Java tests (primary)
    ├── kotlin/        # Kotlin tests (supported)
    └── scala/         # Scala tests (supported)
```

# Architectural Patterns

## Plugin Architecture Pattern

The project implements a plugin-based architecture that allows for dynamic extension of functionality.

### Plugin Characteristics

| Aspect | Implementation |
|--------|---------------|
| **Language** | Groovy |
| **Discovery** | File-based in `plugins/` directory |
| **Lifecycle** | Initialized on application startup |
| **Communication** | Through shared interfaces/contracts |

### Plugin Categories

1. **Core Plugins** - Essential functionality bundled with the application
2. **Extension Plugins** - Optional modules that extend capabilities
3. **Integration Plugins** - Connect to external systems (Git, Shell, etc.)


### Layer Responsibilities

| Layer | Responsibility | Location |
|-------|----------------|----------|
| **Controller** | HTTP request handling, input validation | `*.java` |
| **Service** | Business logic | Kotlin services |
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

```
core ──────────────┐
     depends on    │
                   ▼
              build-plugin
```

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
│   ├── kotlin/        # Production Kotlin
│   ├── java/          # Production Java
│   └── resources/     # Resources
└── test/
    ├── kotlin/        # Kotlin tests
    └── java/          # Java tests
```

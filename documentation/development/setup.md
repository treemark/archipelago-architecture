# Development Setup

## Prerequisites

### Required Tools

| Tool | Version | Purpose |
|------|---------|---------|
| Java | 17+ | Runtime and build |
| Gradle | 8.x | Build automation |
| Git | Latest | Version control |

### Optional but Recommended

| Tool | Purpose |
|------|---------|
| IntelliJ IDEA Ultimate | IDE with Kotlin/Java/Groovy support |
| VS Code | Lightweight alternative with extensions |
| Docker | Containerization support |

## Initial Setup

### 1. Clone the Repository

```bash
git clone https://github.com/treemark/archipelago-architecture.git
cd archipelago-architecture
```

### 2. Verify Java Installation

```bash
java -version
# Should show Java 17 or higher
```

### 3. Build the Project

```bash
./gradlew build
```

This will:
- Download Gradle dependencies
- Compile Kotlin and Java sources
- Run unit tests
- Create build artifacts

### 4. IDE Configuration

#### IntelliJ IDEA

1. Open the project root directory
2. IntelliJ will detect Gradle wrapper and import the project
3. Enable annotation processing if prompted
4. Configure Kotlin compiler to use Kotlin 1.9.x

#### VS Code

1. Install extensions:
   - Kotlin Language
   - Gradle for Java
   - Java Extension Pack
2. Open the project folder
3. Use Gradle Tasks panel for build operations

## Project Structure

```
archipelago-architecture/
├── core/                    # Main application module
│   ├── src/main/kotlin/     # Kotlin source files
│   ├── src/main/java/       # Java source files
│   ├── src/main/resources/  # Configuration files
│   └── src/test/            # Test files
├── build-plugin/            # Plugin development module
│   └── src/main/groovy/     # Groovy plugin sources
├── documentation/           # This documentation
├── gradle/                  # Gradle wrapper files
├── build.gradle             # Root build configuration
└── settings.gradle          # Project structure definition
```

## Running the Application

### Development Mode

```bash
./gradlew :core:bootRun
```

The application will start on `http://localhost:8080`

### Running Tests

```bash
# All tests
./gradlew test

# Specific module
./gradlew :core:test
./gradlew :build-plugin:test

# With coverage
./gradlew test jacocoTestReport
```

### Building Artifacts

```bash
# Build JAR files
./gradlew jar

# Build distribution
./gradlew assemble
```

## Environment Variables

| Variable | Default | Description |
|----------|---------|-------------|
| `JAVA_HOME` | (system) | Java installation path |
| `GRADLE_USER_HOME` | `~/.gradle` | Gradle cache location |

## Troubleshooting

### Gradle Wrapper Issues

If `./gradlew` fails:
```bash
chmod +x gradlew
```

### Dependency Resolution

Clear Gradle cache and rebuild:
```bash
./gradlew clean build --refresh-dependencies
```

# Development Workflow

## Branching Strategy

### Branch Types

| Branch | Naming Convention | Purpose |
|--------|-------------------|---------|
| Main | `main` | Production-ready code |
| Feature | `feature/description` | New feature development |
| Bugfix | `bugfix/description` | Bug fixes |
| Release | `release/x.y.z` | Release preparation |

### Branch Lifecycle

```
feature/description ──► main ──► release/x.y.z
        │                    │
        └────────────────────┘
              (merge back)
```

## Coding Standards

### Kotlin Conventions

- Use 4 spaces for indentation
- Follow [Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html)
- Document public APIs with KDoc comments
- Use meaningful variable names

### Java Conventions

- Follow standard Java naming conventions
- Use Javadoc for public APIs
- Keep methods focused and small (< 30 lines)

### Groovy Conventions (Plugins)

- Use lowercaseCamelCase for methods
- Use UpperCamelCase for classes
- Include doc comments for public methods
- Keep scripts concise

### Code Style Checklist

- [ ] No TODO comments left in code
- [ ] All public methods have documentation
- [ ] Unit tests cover new functionality
- [ ] Code follows naming conventions
- [ ] No hardcoded values (use configuration)

## Git Workflow

### Making Changes

```bash
# 1. Ensure main is up to date
git checkout main
git pull origin main

# 2. Create feature branch
git checkout -b feature/my-feature

# 3. Make changes and commit
git add .
git commit -m "feat: add new functionality"

# 4. Push to remote
git push origin feature/my-feature
```

### Commit Message Format

```
type: short description

Optional detailed explanation.

Fixes #123
```

**Types:** `feat`, `fix`, `docs`, `style`, `refactor`, `test`, `chore`

### Code Review Process

1. Create pull request against `main`
2. Ensure CI checks pass
3. Address review feedback
4. Squash and merge when approved

## Testing Guidelines

### Test Structure

```
src/test/
├── kotlin/com/archipelago/
│   └── core/
│       └── ArchipelagoCoreApplicationTests.kt
└── java/com/archipelago/
    └── core/
        └── ArchipelagoJavaControllerTest.java
```

### Test Naming

| Test Type | Naming Pattern | Example |
|-----------|----------------|---------|
| Unit | `ClassNameTest` | `ArchipelagoCoreApplicationTests` |
| Integration | `ClassNameIntegrationTest` | `ControllerIntegrationTest` |

### Test Best Practices

- One assertion per test when practical
- Use descriptive test names
- Test behavior, not implementation
- Mock external dependencies
- Keep tests independent

## Build Commands Reference

| Command | Purpose |
|---------|---------|
| `./gradlew build` | Full build including tests |
| `./gradlew test` | Run all tests |
| `./gradlew clean` | Clean build artifacts |
| `./gradlew bootRun` | Run application |
| `./gradlew tasks` | List available tasks |
| `./gradlew dependencies` | Show dependency tree |

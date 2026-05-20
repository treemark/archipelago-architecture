# Architecture Fundamentals

## The Archipelago Metaphor

The architecture takes its name from the **archipelago pattern** - a collection of islands (deployable containers) that are operationally independent yet part of a coherent whole.

## Module Types

| Type | Description |
|------|-------------|
| **Islands** | Full-stack deployable containers containing UI, service tier, and backend components (databases, caches) |
| **Libraries** | Shared code that runs inside islands, providing common business logic |

### Island Structure

Each island module contains three tier submodules:

```
island/
├── ui/                    # Frontend tier
│   ├── ui-library/        # Shared UI components (declarative)
│   └── ui-container/      # Deployable UI application
├── service/               # Backend tier
│   ├── service-library/   # Shared service logic (declarative)
│   └── service-container/ # Deployable service application
└── infrastructure/       # IaC tier
    ├── infrastructure-library/  # Shared infra patterns
    └── infrastructure-container/ # Deployable infrastructure
```

**Module types are defined by Gradle plugins** (defined in build-plugin):
- `ui` / `ui-library`
- `service` / `service-library`
- `infrastructure` / `infrastructure-library`

These plugins standardize naming, build, structure, and deployment conventions.

## Key Principles

### Operational Independence
Each island is a complete, independently deployable sub-application. Islands do not depend on other islands being available to function.

### Async Communication Over Sync Coupling
Avoids the "distributed monolith" anti-pattern by:
- **Change Data Capture (CDC)** for data replication
- **Async messaging** for inter-island communication
- No synchronous REST/RPC calls between islands

### Shared Logic = Libraries
When business logic must be shared across islands, it lives in **libraries** that are bundled into the island containers - not as shared services.

### Core as the Foundational Island
The `core` project is the **foundational island** establishing shared infrastructure:
- Messaging services (Kafka, etc.)
- VPC/network definitions
- Base framework versions (Spring, React)
- Cross-cutting concerns

Other islands extend and consume shared infrastructure from core.

## Why This Architecture?

| Problem | Solution |
|---------|----------|
| Distributed monolith | Islands with async boundaries |
| Tight coupling via REST | CDC + async messaging |
| Shared services become bottlenecks | Libraries embedded in containers |
| Deployment dependencies | Independent island deployment |
| Manual infrastructure | Pulumi IaC in each island |
| Version drift across teams | Core island defines base versions |

## Key Technologies

| Category | Technology |
|----------|------------|
| **Cloud** | AWS (initial), GCP, Azure (planned) |
| **IaC** | Pulumi (Java API) |
| **UI** | React with OpenAPI specs + RPC stubs |
| **Backend** | Spring Framework, Java |
| **Messaging** | Async, event-driven |

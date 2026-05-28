# Architecture Fundamentals

## The Archipelago Metaphor

The architecture takes its name from the **archipelago pattern** - a collection of islands (deployable containers) that are operationally independent yet part of a coherent whole.

## Module Types

| Type | Description                                                                                                                                    |
|------|------------------------------------------------------------------------------------------------------------------------------------------------|
| **Islands** | A collection of deployable containers containing React frontend, Spring backend, and infrastructure components (databases, caches, etc)            |
| **Libraries** | Shared code that runs inside islands, providing business logic that needs to be shared between islands. These can be React, Spring or Pulumi IaC. |
| **Containers** | Deployable Docker containers - can be frontend (React) or backend (Spring) applications.                                                        |
| **Infrastructure as Code** | Pulumi IaC code that defines cloud infrastructure (VPC, clusters, services) for other containers.                                              |

Each module type has a corresponding **Gradle plugin** (defined in build-plugin) that handles:
- **Build**: Compilation, testing, packaging conventions
- **CI/CD**: Release workflows, version management, Docker image publishing
- **Deployment**: Infrastructure provisioning, container deployment, health checks

### Container Structure

Each container is a deployable Docker image. The architecture supports two types of containers:

- **React Container**: Frontend application (React) - built with the `archipelago.react-container` plugin
- **Spring Container**: Backend application (Spring/Java) - built with a corresponding Spring container plugin

**Gradle plugins for containers** (defined in build-plugin):
- `archipelago.react-container` - Builds and packages React applications for Docker deployment
- Spring container plugins follow the same pattern

These plugins handle Node/npm builds, Docker image naming, and packaging for deployment.

### Library Structure

Libraries contain shared code that runs inside containers. They can be:
- **React Libraries**: Shared React components and hooks
- **Spring Libraries**: Shared Java business logic
- **Pulumi Libraries**: Shared Pulumi patterns

**Gradle plugins for libraries** (defined in build-plugin):
- `archipelago.react-lib` - Standardizes React library structure and publication
- `archipelago.spring-lib` - Standardizes Spring library structure and publication
- `archipelago.pulumi` - Used for Pulumi library patterns

These plugins handle versioning, publication, and consumption conventions.

### Infrastructure as Code Structure

Infrastructure as Code (IaC) modules contain Pulumi code that defines cloud infrastructure for other containers. This includes:

- **VPC and networking**: Virtual networks, subnets, security groups
- **Container orchestration**: ECS/Fargate clusters, Kubernetes clusters
- **Managed services**: Databases (RDS, DynamoDB), caches (ElastiCache), queues (SQS)
- **Public endpoints**: ALB, API Gateway, CloudFront distributions

**Gradle plugin for IaC** (defined in build-plugin):
- `archipelago.pulumi` - Handles Pulumi stack management, deployments, and infrastructure definitions

This plugin enables each island to define its own infrastructure alongside its containers, providing self-contained deployment.

### Island Structure

Each island module contains one or more submodules.

```
island/
├── react-lib/            # Shared React components
├── react-container/      # React Container
├── spring-lib/           # Shared Spring logic
├── spring-container/      # Spring Container
└── pulumi/              # Shared Pulumi patterns, VPC, messaging (deploys containers)
```

**Gradle plugins for Islands** (defined in build-plugin):
- `archipelago.island` - Organizes island structure with all component types, coordinates builds across all submodules

This plugin orchestrates builds across all submodule types (react-lib, spring-lib, react-container, spring-container, pulumi) to create a complete island package.

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

### Backend for Frontend (BFF) / Interface Segregation
The architecture embraces the **Backend for Frontend** pattern ([also known as interface segregation](https://en.wikipedia.org/wiki/Interface_segregation_principle)), which states that it is better to have many consumer-specific APIs than one general-purpose API. Each island exposes its own curated API surface tailored to its specific consumers rather than aggregating everything into monolithic backends.

This pattern is facilitated in Archipelago through:
- **[OpenAPI Specifications](https://learn.microsoft.com/en-us/azure/architecture/patterns/backends-for-frontends)**: Each service container generates and publishes its OpenAPI spec, documenting the exact API contract
- **Client-side RPC Stubs**: UI containers consume these OpenAPI specs to generate type-safe RPC-style stubs, enabling seamless communication without manual API integration

By treating each island as its own BFF, the architecture prevents:
- Bloated APIs with endpoints that don't belong to any consumer
- Coordination overhead between teams owning different UI experiences
- Tight coupling through shared general-purpose endpoints

See also:
- [Backends for Frontends Pattern - Azure Architecture Center](https://learn.microsoft.com/en-us/azure/architecture/patterns/backends-for-frontends)
- [Backend for Frontend in Monorepos: Best Practices](https://www.paulserban.eu/blog/post/backend-for-frontend-in-monorepos-best-practices-and-strategies/)
- [Interface Segregation for Microservices APIs](https://www.nilus.be/blog/interface_segregation_for_microservices_apis/)

### Functional Organization: Type at High Levels, Feature at Low Levels
Code should be organized by **type** at high levels (to provide orientation and context) but by **feature/function** at deeper levels (to keep related code together).[^1]

This hybrid approach combines the best of both worlds:
- **High-level structure by type**: At the top level, directories like `react-lib/`, `spring-lib/`, `pulumi/` quickly convey what categories of components exist
- **Deep-level structure by feature**: Within each library or container, code groups by feature (e.g., `auth/`, `billing/`, `users/`) rather than technical layer

Research supports this pattern — feature-based organization "scales better in larger codebases" and "tells a story" about the application.[^1] [^2]

**Implementation in Archipelago**:
- Each island contains multiple submodule types (`react-lib`, `spring-lib`, `pulumi`, etc.) — the type-based high level
- Within each submodule, code is organized by feature/domain (e.g., `spring-lib/src/main/java/com/archipelago/spring/billing/`, `spring-lib/src/main/java/com/archipelago/spring/auth/`)
- This keeps all code for a feature — models, services, validators — bundled together, while still providing type-based conventions at the appropriate level

The alternative (organizing exclusively by type like `controllers/`, `models/`, `validators/`) scatters feature-related code across many directories, making navigation harder as the codebase grows.[^1] [^3]

See also:
- [Structure by Type vs Feature](https://dev.to/jesterxl/code-organization-in-functional-programming-vs-object-oriented-programming-79i)
- [The Life-changing Magic of Feature-Focused Code Organization](https://dev.to/jamesmh/the-life-changing-and-time-saving-magic-of-feature-focused-code-organization-1708)
- [Package-by-Feature, Not Layer](http://www.javapractices.com/topic/TopicAction.do?Id=205)
- [A Front-End Application Folder Structure that Makes Sense](https://fadamakis.com/a-front-end-application-folder-structure-that-makes-sense-ecc0b690968b)

### Core as the Foundational Island

The `core` project is the **foundational island** establishing shared infrastructure:
- Messaging services (Kafka, etc.)
- VPC/network definitions
- Base framework versions (Spring, React)
- Cross-cutting concerns

Other islands extend and consume shared infrastructure from core.

## Why This Architecture?

| Problem | Solution                                                          |
|---------|-------------------------------------------------------------------|
| Distributed monolith | Operationally independent islands of related functions            |
| Tight coupling via REST | Change data capture, async messaging and locally replicated data. |
| Shared services become bottlenecks | Libraries embedded in containers                                  |
| Deployment dependencies | Independent island deployment                                     |
| Manual infrastructure | Pulumi IaC in each island                                         |
| Version drift across teams | Core island defines base versions                                 |

## Key Technologies

| Category | Technology |
|----------|------------|
| **Cloud** | AWS (initial), GCP, Azure (planned) |
| **IaC** | Pulumi (Java API) |
| **UI** | React with OpenAPI specs + RPC stubs |
| **Backend** | Spring Framework, Java |
| **Messaging** | Async, event-driven |

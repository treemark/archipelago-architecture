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
- `archipelago.pulumi-lib` - Standardizes Pulumi library structure and publication (shared Pulumi patterns, reusable components)

These plugins handle versioning, publication, and consumption conventions.

### Infrastructure as Code Structure

Infrastructure as Code (IaC) modules contain Pulumi code that defines cloud infrastructure for other containers. This includes:

- **VPC and networking**: Virtual networks, subnets, security groups
- **Container orchestration**: ECS/Fargate clusters, Kubernetes clusters
- **Managed services**: Databases (RDS, DynamoDB), caches (ElastiCache), queues (SQS)
- **Public endpoints**: ALB, API Gateway, CloudFront distributions

**Gradle plugin for IaC** (defined in build-plugin):
- `archipelago.pulumi` - Handles Pulumi stack lifecycle: stack management, deployments, and infrastructure definitions

This plugin enables each island to define its own infrastructure alongside its containers, providing self-contained deployment.

### Island Structure

Each island module contains one or more submodules.

```
island/
├── react-lib/            # Shared React components (archipelago.react-lib)
├── react-container/      # React Container (archipelago.react-container)
├── spring-lib/           # Shared Spring logic (archipelago.spring-lib)
├── spring-container/     # Spring Container (archipelago.spring-container)
├── pulumi-lib/           # Shared Pulumi patterns and reusable components (archipelago.pulumi-lib)
└── pulumi/               # IaC stack: VPC, messaging, deploys containers (archipelago.pulumi)
```

**Gradle plugins for Islands** (defined in build-plugin):
- `archipelago.island` - Organizes island structure with all component types, coordinates builds across all submodules

This plugin orchestrates builds across all submodule types (react-lib, spring-lib, react-container, spring-container, pulumi-lib, pulumi) to create a complete island package.

These plugins standardize naming, build, structure, and deployment conventions.

## Key Principles

### Framework, Not an Application

This repository is a **framework** — analogous to Spring or React — intended to be consumed as a set of libraries and plugins by downstream implementation repositories. It is not an application.

- **Islands in this repository are for demonstration only.** Any island defined here must be a minimal "hello world" or reference example that illustrates framework usage, not a real feature implementation.
- **No implementation-specific logic belongs here.** Business domains, product features, RBAC rules, authentication strategies, and other application concerns must live in consumer repositories that depend on this framework.
- **All libraries and plugins must remain generic.** Future framework capabilities (e.g., authentication, RBAC, multi-tenancy) should be designed as modular, configurable libraries — not opinionated implementations. Consumer repositories provide the implementation specifics.
- **The shell island (TBD) and any other shared-infrastructure islands in this repo follow the same rule** — they exist only to demonstrate the framework pattern, not to serve as production shared infrastructure.

This boundary ensures the framework remains reusable across diverse implementations without accumulating application-specific coupling.

### Operational Independence
Each island is a complete, independently deployable sub-application. Islands do not depend on other islands being available to function.

### Async Communication Over Sync Coupling
This rule applies at the **island-to-island boundary**. Synchronous communication within an island (e.g., a React container calling its own Spring container via the BFF pattern) is permitted and expected.

For cross-island communication, avoids the "distributed monolith" anti-pattern by:
- **Change Data Capture (CDC)** for data replication
- **Async messaging** for inter-island communication
- No synchronous REST/RPC calls between islands

### Shared Logic = Libraries
When business logic must be shared across islands, it lives in **libraries** that are bundled into the island containers - not as shared services.

### Backend for Frontend (BFF) / Interface Segregation
The architecture embraces the **Backend for Frontend** pattern ([also known as interface segregation](https://en.wikipedia.org/wiki/Interface_segregation_principle)), which states that it is better to have many consumer-specific APIs than one general-purpose API. Each island exposes its own curated API surface tailored to its specific consumers rather than aggregating everything into monolithic backends.

Within an island, the React container communicates synchronously with its own Spring container via the BFF API. This is a standard tiered architecture (Controller → Service → Repository) scoped to the island boundary.

#### RPC-Style APIs over REST/CRUD

BFF APIs in Archipelago favor **RPC-style operations** over generic REST/CRUD endpoints. This is a deliberate design choice:

- **REST/CRUD endpoints leak backend structure.** A `GET /users/{id}` or `PUT /orders/{id}` exposes the shape of backend data models directly to the frontend, creating tight coupling between the UI and the persistence layer.
- **RPC-style endpoints express intent.** Operations like `submitOrder`, `approveExpense`, or `getUserDashboard` describe *what the consumer needs to do*, not *how the backend stores data*. This decouples the UI from backend implementation details and makes APIs easier to evolve independently.
- **Generated stubs reinforce this.** OpenAPI specs describe operations and their contracts; generated TypeScript RPC stubs on the frontend consume these as typed function calls, not raw HTTP verbs.

This pattern is facilitated in Archipelago through:
- **[OpenAPI Specifications](https://learn.microsoft.com/en-us/azure/architecture/patterns/backends-for-frontends)**: Each service container generates and publishes its OpenAPI spec, documenting the exact API contract
- **Client-side RPC Stubs**: React containers consume these OpenAPI specs to generate type-safe RPC-style stubs, enabling seamless communication without manual API integration

By treating each island as its own BFF, the architecture prevents:
- Bloated APIs with endpoints that don't belong to any consumer
- Coordination overhead between teams owning different UI experiences
- Tight coupling through shared general-purpose endpoints
- Backend data model leakage through CRUD-style endpoints

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

> **Note**: Some advocate for flat directory structures, relying on editor search and symbol navigation over folder hierarchy. This works well in small codebases but doesn't scale to the module complexity of an archipelago implementation, where clear type-and-feature organization is essential for navigating across many islands and submodules.

See also:
- [Structure by Type vs Feature](https://dev.to/jesterxl/code-organization-in-functional-programming-vs-object-oriented-programming-79i)
- [The Life-changing Magic of Feature-Focused Code Organization](https://dev.to/jamesmh/the-life-changing-and-time-saving-magic-of-feature-focused-code-organization-1708)
- [Package-by-Feature, Not Layer](http://www.javapractices.com/topic/TopicAction.do?Id=205)
- [A Front-End Application Folder Structure that Makes Sense](https://fadamakis.com/a-front-end-application-folder-structure-that-makes-sense-ecc0b690968b)

### Core as the Foundational Library Island

The `core` project is the **foundational library island** — it contains only library modules (`react-lib`, `spring-lib`, `pulumi-lib`) and does not define any containers or IaC stacks. It publishes shared libraries consumed by all other islands, establishing:

- Base framework versions (Spring, React, Pulumi)
- Shared domain models and cross-cutting concerns
- Common messaging contracts and event definitions

Shared runtime concerns that require containers (e.g., the master shell SPA that island UIs plug into) will be defined in a separate island, name to be determined.

Other islands consume `core` libraries as dependencies but do not depend on `core` being deployed or running.

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
| **React (Frontend)** | React with OpenAPI specs + RPC stubs |
| **Spring (Backend)** | Spring Framework, Java |
| **Messaging** | Async, event-driven |

> **Note on naming**: Module and submodule names explicitly reflect the technology they use (e.g., `react-container`, `spring-lib`, `pulumi`). This is intentional — as the framework is designed to support additional technologies (e.g., Node.js backends, Angular frontends), technology-specific names prevent naming conflicts and make the tech stack of each module unambiguous.

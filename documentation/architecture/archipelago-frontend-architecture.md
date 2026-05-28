# Archipelago Architecture — Frontend Implementation Guide

## Overview

This document captures the frontend architecture decisions for the Archipelago framework, a modular, operationally independent, full-stack application architecture designed to unify independent "islands" of functionality under a single master SPA. It establishes the technology choices, integration patterns, and development workflow that implement Archipelago's core principles on the frontend.

---

## Core Principles (Frontend Context)

Archipelago's architectural philosophy applies directly to its frontend layer:

- **Modularity**: Each island of functionality is an independently developed, independently deployed React application.
- **Operational Independence**: No island depends on another island's runtime or build process. Failure or redeployment of one island does not affect others.
- **Avoiding the Distributed Monolith**: Islands do not share a build pipeline, a monolithic state store, or tightly coupled APIs. Communication flows through well-defined contracts.
- **Backend for Frontend (BFF) / Interface Segregation**: Each island owns its own BFF — a dedicated API gateway or aggregation layer that exposes only the data and operations that island requires. No island accesses another island's backend directly.
- **Generated Contracts**: All communication between the frontend and service tier is mediated through generated OpenAPI specifications and generated TypeScript client stubs, ensuring type safety and contract consistency.

---

## Architecture Decision: Webpack Module Federation

### Why Module Federation

After evaluating candidate approaches — Radix Primitives (component primitive layer), Minimals/MUI (full application framework), Ariakit (accessibility toolkit), Headless UI, and single-spa — the Archipelago frontend layer is built on **Webpack Module Federation** (Webpack 5) as its micro-frontend orchestration mechanism.[cite:93][cite:87]

Module Federation allows each Archipelago island to be:

- A **separate Webpack build**, deployed independently to its own origin or CDN path[cite:93]
- A **remote module provider**, exposing React components and utilities to the shell via a `remoteEntry.js` manifest[cite:93]
- **Runtime composable**, meaning the shell SPA loads island code at runtime without rebuilding[cite:93][cite:109]

This maps directly to Archipelago's operational independence principle: an island can be updated, redeployed, or scaled independently without rebuilding or redeploying the master shell SPA.[cite:93][cite:92]

### Why Not single-spa

single-spa was considered as an alternative. While it provides framework-agnostic lifecycle orchestration (`bootstrap`, `mount`, `unmount`) and explicit route-based mounting, it introduces more runtime complexity and is better suited to environments where multiple frontend frameworks (React, Vue, Angular) must coexist.[cite:82][cite:88][cite:108] Archipelago is a React-homogeneous stack; Module Federation is the preferred choice for that context.[cite:108][cite:102]

### Module Federation vs. single-spa Summary

| Factor | Module Federation | single-spa |
|---|---|---|
| Integration model | Runtime module sharing via Webpack builds[cite:93] | Framework-agnostic lifecycle orchestration[cite:94] |
| Independent deployment | Yes — each island deploys its own `remoteEntry.js`[cite:93] | Yes — each micro-app served separately[cite:94] |
| React-first ergonomics | Natural fit[cite:93][cite:100] | Supported, higher ceremony[cite:88] |
| Multi-framework support | Possible, not primary use case[cite:93] | First-class feature[cite:82] |
| Shared dependency management | `shared` config in Webpack[cite:93] | SystemJS import maps[cite:94] |
| Storybook integration | Official `@module-federation/storybook-addon`[cite:99] | Manual wiring required[cite:97] |
| Complexity (React-only stack) | Lower[cite:108] | Higher[cite:94] |

---

## UI Primitive Layer: Radix Primitives

The shared UI component library for Archipelago is built on **Radix Primitives** — unstyled, accessible React primitives that provide behavior and keyboard interaction without imposing a visual design system.[cite:33][cite:78]

### Why Radix Over MUI/Minimals

- **Minimals** is a full application starter kit built on MUI. It introduces Material Design theming, layout conventions, and a large component catalog — appropriate for assembling a dashboard quickly, but too opinionated for a micro-UI foundation.[cite:43][cite:42]
- **MUI** is a comprehensive styled component framework. It can coexist with Radix but creates overlap and friction in focus handling, portal management, and keyboard behavior when used alongside Radix for the same interaction types.[cite:62][cite:64]
- **Radix** provides only the behavior contract. Each island team supplies its own styling, keeping visual identity fully under control.[cite:33][cite:74]

### Radix Primitives Used

The following primitives are foundational to Archipelago's micro-UI components:

- `@radix-ui/react-toolbar` — persistent action strips, button groups[cite:31]
- `@radix-ui/react-dropdown-menu` — contextual action menus[cite:11]
- `@radix-ui/react-menubar` — desktop-style top command surfaces[cite:36]
- Additional candidates: `@radix-ui/react-dialog`, `@radix-ui/react-popover`, `@radix-ui/react-tabs`, `@radix-ui/react-select`[cite:33]

### Radix and MUI Coexistence

Radix does not leverage MUI internally and is an independent library.[cite:78][cite:33] MUI may be used selectively for components outside Radix's scope (e.g., data grids, date pickers), but the two should not be used for the same interaction primitive within the same island. The `asChild` composition pattern in Radix allows attaching Radix behavior to custom-styled elements, including those styled with MUI's `sx` prop or emotion, as long as refs and props are forwarded correctly.[cite:80]

---

## OpenAPI and TypeScript Client Generation

### Principle

All communication between an island's frontend and its BFF is mediated through a **generated contract layer**:

1. Each BFF exposes an **OpenAPI 3.x specification** (YAML or JSON), either authored by hand or generated from server-side annotations (e.g., Java with Springdoc/OpenAPI, or similar).
2. A **TypeScript client** is generated from the OpenAPI spec using a code generation tool (e.g., `openapi-typescript`, `openapi-generator`, or `@hey-api/openapi-ts`).
3. The generated client is consumed directly by the island's React components and hooks — no handwritten `fetch` calls against service endpoints.

### Benefits

- **Type safety at the boundary**: API contracts are reflected in TypeScript types, catching mismatches at compile time rather than runtime.
- **Contract-first development**: Frontend and BFF teams can work in parallel once the OpenAPI spec is agreed upon.
- **Interface segregation enforced**: Each island generates a client only for its own BFF. No island imports or instantiates a client for another island's backend.
- **Regeneration on change**: When the BFF's API evolves, re-running the generator surfaces breaking changes immediately in the TypeScript layer.

### Recommended Toolchain

| Tool | Role |
|---|---|
| `openapi-typescript` or `@hey-api/openapi-ts` | Generates TypeScript types and fetch-based clients from OpenAPI specs |
| `openapi-generator` (Java CLI) | Alternative generator with more language targets; useful if BFF is Java-authored |
| `zod-openapi` or `typebox` | Optional runtime validation layer aligning with generated types |
| Nx `@nx/js` library generator | Hosts generated client stubs as a shared or island-scoped Nx library |

### Workflow per Island

```
BFF (Java / Spring / etc.)
  └── Exposes: openapi.yaml
        └── CI step: openapi-typescript generates → src/api/generated/
              └── Island React hooks consume generated client types
```

Each island's generated client lives inside its own Nx library or `src/api/generated/` directory. It is **never shared across island boundaries** — sharing a client would couple islands to each other's BFF contracts, violating interface segregation.

---

## Monorepo and Project Structure

An Nx monorepo is recommended to host the shell, all islands, the shared Radix primitive library, and Storybook under unified tooling without coupling their deployment pipelines.[cite:98][cite:100]

### Recommended Structure

```
archipelago/
├── apps/
│   ├── shell/                    # Master SPA (Module Federation host)
│   │   └── webpack.config.ts     # Declares remotes for each island
│   ├── island-[name]/            # One per Archipelago island (Module Federation remotes)
│   │   ├── src/
│   │   │   ├── components/       # Island-specific React components (Radix-based)
│   │   │   ├── api/
│   │   │   │   └── generated/    # OpenAPI-generated TypeScript stubs (BFF-specific)
│   │   │   └── module-entry.ts   # Exposes components to shell via Module Federation
│   │   └── webpack.config.ts     # Declares exposes and shared singletons
├── libs/
│   ├── ui-primitives/            # Shared Radix wrapper components, tokens, theme
│   │   └── .storybook/           # Storybook for shared primitive components
│   └── ui-tokens/                # Design tokens (CSS variables, spacing, color)
└── nx.json
```

### Key Rules

- `ui-primitives` is the **only** shared UI library. Islands may not share components with each other directly — they consume from `ui-primitives` only.
- Each island's `api/generated/` is **not exported** from the island. It is internal to that island's runtime boundary.
- `react`, `react-dom`, and `ui-primitives` must be declared as `singleton: true` in every island's Module Federation `shared` config to prevent duplicate React instances at runtime.[cite:93][cite:96]

---

## Storybook Integration

Storybook is used to develop and document all components in `ui-primitives` in isolation. The official `@module-federation/storybook-addon` enables Storybook to consume federated remote components for live cross-island documentation when needed.[cite:99][cite:97]

### Setup

- Storybook lives in `libs/ui-primitives/.storybook/`
- Uses the same Webpack config as the primitive library to ensure parity between Storybook and production builds[cite:100][cite:98]
- Each island may optionally maintain its own Storybook instance for island-specific component stories

### Component Development Workflow

1. Define the component API and Radix primitive composition in `ui-primitives`
2. Write a Story for each variant (default, focused, disabled, loading)
3. Validate accessibility (keyboard, focus ring, ARIA roles) in Storybook
4. Import the primitive into the target island and apply island-level styling

---

## Critical Implementation Notes

### Shared Singleton Guard

The single greatest operational risk in Module Federation is version mismatch on shared singletons. If two islands load different copies of React, the application will throw runtime errors. Every island's `webpack.config.ts` must include:

```typescript
shared: {
  react: { singleton: true, requiredVersion: deps['react'] },
  'react-dom': { singleton: true, requiredVersion: deps['react-dom'] },
  '@radix-ui/react-toolbar': { singleton: true },
  // ... other Radix primitives used across islands
}
```

### BFF Contract Versioning

OpenAPI specs should be versioned (e.g., `/api/v1/openapi.yaml`). A breaking BFF change requires a new spec version, regeneration of the TypeScript client, and a coordinated island release — but does not require touching any other island or the shell.

### No Cross-Island State Sharing

Islands must not share a Redux store, Zustand store, or React context across the Module Federation boundary. Inter-island communication, when necessary, should flow through:
- URL/router state (shallow, serializable)
- Custom browser events (`window.dispatchEvent`)
- A shell-level message bus (defined in the shell, injected into islands via Module Federation `shared`)

---

## Technology Stack Summary

| Layer | Technology | Role |
|---|---|---|
| Master SPA shell | React + Webpack Module Federation (host) | Orchestrates island mounting and routing[cite:93] |
| Islands | React + Webpack Module Federation (remotes) | Independently deployed full-stack application slices[cite:93][cite:87] |
| UI primitives | Radix Primitives (`@radix-ui/*`) | Accessible, unstyled behavior layer[cite:33][cite:78] |
| Component development | Storybook + `@module-federation/storybook-addon` | Isolated component authoring and documentation[cite:99][cite:100] |
| API contracts | OpenAPI 3.x (per BFF) | Machine-readable service contracts per island[cite:84] |
| API client layer | Generated TypeScript stubs (`openapi-typescript` / `@hey-api/openapi-ts`) | Type-safe, contract-bound service communication |
| Monorepo tooling | Nx workspace | Unified build graph, library management, Storybook presets[cite:98][cite:100] |
| Shared dependency control | Module Federation `shared` singleton config | Prevents duplicate React/Radix instances[cite:93][cite:96] |


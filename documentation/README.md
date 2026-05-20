# Archipelago Architecture Documentation

This documentation is structured for efficient AI agent consumption while remaining human readable.

## Documentation Structure

```
documentation/
├── README.md              # This file - documentation overview and navigation
├── architecture/          # System architecture and design decisions
│   ├── README.md          # Architecture summary
│   ├── overview.md        # High-level architecture description
│   └── patterns.md        # Architectural patterns used
├── development/           # Development setup and guidelines
│   ├── README.md          # Development guide summary
│   ├── setup.md           # Local development setup
│   └── workflow.md        # Development workflow
└── modules/               # Module-specific documentation
    ├── README.md          # Modules overview
    ├── core/              # Core module documentation
    └── build-plugin/      # Build plugin documentation
```

## AI Optimization Strategy

This documentation follows a hierarchical summarization pattern:

1. **Parent readme.md files** contain summaries of their child directories
2. **Leaf documentation files** contain detailed content
3. **Context conservation**: AI agents can read just parent readmes for overview or drill down for specifics
4. **Bidirectional navigation**: Each readme links to its children and parent

## Quick Navigation

| Area | Description |
|------|-------------|
| [Architecture](architecture/) | System architecture, design decisions, patterns |
| [Development](development/) | Setup guides, workflows, coding standards |
| [Modules](modules/) | Module-specific documentation |

## Reading This Documentation

- **For quick overview**: Read only this file and top-level category readmes
- **For detailed information**: Navigate into specific directories
- **For AI context efficiency**: Each readme summarizes its children to minimize token usage

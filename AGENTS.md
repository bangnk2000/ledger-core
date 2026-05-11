<!-- SPECKIT START -->
For additional context about technologies to be used, project structure,
shell commands, and other important information, read
specs/001-core-ledger-foundation/plan.md
<!-- SPECKIT END -->

# ledger-core

Follow:
- constitution.md
- ADRs

Core Rules:
- Ledger entries are immutable
- Double-entry accounting mandatory
- APIs backward compatible
- Zero downtime migration required
- Prefer simplicity over abstraction
- Explicit transaction boundaries only

Architecture:
- Hexagonal Architecture
- Pragmatic DDD
- Modular monolith first

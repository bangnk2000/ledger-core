<!-- SPECKIT START -->
For additional context about technologies to be used, project structure,
shell commands, and other important information, read
specs/003-tech-debt-compliance/plan.md
<!-- SPECKIT END -->

# ledger-core

Follow:
- .specify/memory/constitution.md
- docs/ADR/

Core Rules:
- Ledger entries are immutable
- Double-entry accounting mandatory
- APIs backward compatible
- Zero downtime migration required
- Prefer simplicity over abstraction
- Explicit transaction boundaries only
- Any task-list implementation follows: worktree -> TDD (red-green-refactor) -> subagent-driven execution -> code review -> finish-branch

Architecture:
- Hexagonal Architecture
- Pragmatic DDD
- Modular monolith first

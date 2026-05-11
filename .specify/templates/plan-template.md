# Implementation Plan: [FEATURE]

**Branch**: `[###-feature-name]` | **Date**: [DATE] | **Spec**: [link]
**Input**: Feature specification from `/specs/[###-feature-name]/spec.md`

**Note**: This template is filled in by the `/speckit-plan` command. See `.specify/templates/plan-template.md` for the execution workflow.

## Summary

[Extract from feature spec: primary requirement + technical approach from research]

## Technical Context

<!--
  ACTION REQUIRED: Replace the content in this section with the technical details
  for the project. The structure here is presented in advisory capacity to guide
  the iteration process.
-->

**Language/Version**: Java [version] or NEEDS CLARIFICATION  
**Primary Dependencies**: Spring Boot [version], [dependencies] or NEEDS CLARIFICATION  
**Storage**: PostgreSQL [version/schema area] or N/A  
**Testing**: [JUnit/Testcontainers/Spring Boot Test/etc.] or NEEDS CLARIFICATION  
**Target Platform**: Kubernetes-hosted backend service or NEEDS CLARIFICATION
**Project Type**: backend service / library / infrastructure component or NEEDS CLARIFICATION  
**Performance Goals**: [p95 latency, throughput, batch size, concurrency target] or NEEDS CLARIFICATION  
**Constraints**: [zero-downtime migration, graceful shutdown, compatibility, security] or NEEDS CLARIFICATION  
**Scale/Scope**: [tenants, accounts, ledgers, transactions, integrations] or NEEDS CLARIFICATION

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

- **Simplicity and explicitness**: Is the simplest viable design chosen, with
  any new abstraction, dependency, async flow, CQRS split, or distributed
  transaction explicitly justified?
- **Financial ledger correctness**: Are ledger entries immutable, balance
  mutations double-entry balanced, and all ledger effects traceable and
  auditable?
- **Hexagonal architecture**: Are domain and application layers isolated from
  Spring, PostgreSQL, HTTP, messaging, and external SDK concerns?
- **Pragmatic DDD**: Are business invariants, aggregate boundaries, and
  ubiquitous language represented where they add clarity?
- **API and data compatibility**: Are API changes backward compatible, and do
  PostgreSQL migrations support zero-downtime deployment with rollback or
  roll-forward strategy?
- **Testing**: Are critical business flows covered by integration tests through
  real or production-equivalent adapters?
- **Observability**: Are structured logs, metrics, and traces defined for the
  important business and operational events?
- **Security**: Are authentication, authorization, secrets handling, and input
  validation impacts identified and reviewable?
- **Reliability and integrations**: Are retry, timeout, idempotency, failure
  handling, and consistency guarantees documented for external integrations,
  async flows, and distributed transactions?
- **Transaction safety**: Are transaction boundaries explicit, and are
  concurrency, locking, scaling bottlenecks, and rollback strategy identified?
- **Operations**: Does the design preserve Terraform/Docker reproducibility,
  Kubernetes rolling updates, and graceful shutdown?
- **ADRs and dependencies**: Are major architectural decisions, architecture
  violations, and new dependency justifications documented?

## Project Structure

### Documentation (this feature)

```text
specs/[###-feature]/
├── plan.md              # This file (/speckit-plan command output)
├── research.md          # Phase 0 output (/speckit-plan command)
├── data-model.md        # Phase 1 output (/speckit-plan command)
├── quickstart.md        # Phase 1 output (/speckit-plan command)
├── contracts/           # Phase 1 output (/speckit-plan command)
└── tasks.md             # Phase 2 output (/speckit-tasks command - NOT created by /speckit-plan)
```

### Source Code (repository root)
<!--
  ACTION REQUIRED: Replace the placeholder tree below with the concrete layout
  for this feature. Delete unused options and expand the chosen structure with
  real paths (e.g., apps/admin, packages/something). The delivered plan must
  not include Option labels.
-->

```text
src/main/java/[base/package]/
├── domain/              # Entities, value objects, domain services
├── application/         # Use cases, commands/queries, ports
├── adapter/
│   ├── in/              # REST/controllers/listeners/schedulers
│   └── out/             # Persistence, clients, messaging, external systems
└── config/              # Spring configuration wiring adapters to ports

src/main/resources/
└── db/migration/        # PostgreSQL migrations

src/test/java/[base/package]/
├── unit/
├── integration/
└── contract/

infra/
├── terraform/
└── docker/
```

**Structure Decision**: [Document the selected structure and reference the real
directories captured above]

## Complexity Tracking

> **Fill ONLY if Constitution Check has violations that must be justified**

| Violation | Why Needed | Simpler Alternative Rejected Because |
|-----------|------------|-------------------------------------|
| [e.g., 4th project] | [current need] | [why 3 projects insufficient] |
| [e.g., Repository pattern] | [specific problem] | [why direct DB access insufficient] |

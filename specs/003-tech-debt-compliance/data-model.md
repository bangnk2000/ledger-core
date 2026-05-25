# Data Model: Technical Debt Compliance Artifacts

**Feature**: `003-tech-debt-compliance`  
**Date**: 2026-05-19  

This initiative is documentation-driven: the “data model” describes the
artifacts that govern refactor execution, validation, and quality-gate policy.

## Entities

### 1) Module

Represents a code area used for prioritization and wave boundaries.

- `id`: stable identifier (e.g., `ledger.foundation`, `ledger.balance`)
- `path_prefixes`: list of source roots that define the module boundary
- `owner`: team/role (optional if unknown)
- `criticality`: `transaction_critical | replay_sensitive | normal`
- `notes`: determinism/transaction/concurrency notes

### 2) RefactorBacklogItem

A unit of incremental refactor work that can be delivered safely.

- `id`: stable item id (e.g., `SQ-LEDGER-001`, `QODANA-LEDGER-004`)
- `source`: `sonar | qodana | manual`
- `module_id`: references `Module.id`
- `category`: `duplication | long_methods | large_classes | naming_cleanup | transaction_consistency | concurrency_safety | logging_cleanup | query_optimization`
- `risk`: `low | medium | high`
- `touches`: `transaction_boundary | concurrency | replay | none` (exactly one per wave for this feature)
- `description`: what will change
- `acceptance_checks`: list of required validations
- `links`: optional references to report ids/paths

### 3) RiskMatrixEntry

Risk record used to drive wave sizing and validation requirements.

- `category`: same set as `RefactorBacklogItem.category`
- `likelihood`: `low | medium | high`
- `impact`: `low | medium | high`
- `detection_signals`: what indicates a regression (tests, metrics, diffs)
- `mitigation`: what to do to reduce likelihood/impact
- `rollback_trigger`: explicit “stop” signal and rollback condition

### 4) ExecutionWave

The delivery unit for this feature: one module group + one risk category.

- `id`: `W0`, `W1`, ...
- `module_ids`: list of modules in scope for this wave (small set)
- `category`: one backlog category for the wave
- `touches`: one of `transaction_boundary | concurrency | replay | none`
- `entry_criteria`: what must be true before starting the wave
- `exit_criteria`: what must pass before merging
- `rollback_plan`: how to revert safely

### 5) QualityGatePolicy

Defines enforcement rules and exceptions for Sonar and Qodana.

- `tools`: `{ sonar, qodana }`
- `blocking_rules`: what fails CI by default
- `exception_policy`: who can approve, time-bound limits, required evidence
- `transaction_critical_overrides`: additional checks for critical modules

### 6) ExceptionRecord

Time-bound permission to merge despite a new or unresolved critical issue.

- `id`: unique record id
- `tool`: `sonar | qodana`
- `finding_id`: finding key from the tool
- `scope`: module + file path(s)
- `rationale`: why it cannot be fixed in-wave
- `expires_on`: date (must be explicit)
- `required_validations`: mandatory replay/concurrency validations when applicable
- `approver`: role/person

## Relationships

- `Module` 1..n `RefactorBacklogItem`
- `RefactorBacklogItem` 0..1 `ExceptionRecord` (exceptions are exceptional)
- `ExecutionWave` 1..n `RefactorBacklogItem`
- `RiskMatrixEntry` maps to backlog categories and shapes wave exit criteria
- `QualityGatePolicy` governs `ExecutionWave.exit_criteria`


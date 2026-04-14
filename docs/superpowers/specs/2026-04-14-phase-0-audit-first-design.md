# Internal Termux Phase 0 Audit-First Design

> **Scope:** This spec covers only the first implementation slice for `internal-termux`: Phase 0 audit work plus the repository docs that make later phases safe. It intentionally excludes any Android skeleton work under `main/`.

## Goal

Produce the surgery map and repo guardrails that will let later phases fork Termux safely:

- `AUDIT.md`
- `CONTRIBUTING.md`
- `tasks/todo.md`

`main/` remains empty by design until the audit reveals the real coupling points that should inform the library/module structure.

## Why This Slice Is First

The roadmap already identified obvious hardcoded assumptions such as `com.termux`, `/data/data/com.termux`, and `$PREFIX`. Initial inspection confirmed that these assumptions are spread across resources, constants, manifests, and code paths in `upstream/termux-app`. The risk is not the obvious hits; it is the non-obvious coupling that would leak into a premature Android skeleton and force rework in Phase 1.

This slice therefore treats audit work as a prerequisite for architecture, not as documentation after the fact.

## Rejected Approaches

### 1. Audit and scaffold `main/` in the same slice

Rejected because it bakes assumptions into the future module structure before the audit is complete.

### 2. Scaffold first and clean up after the audit

Rejected because it creates churn in a repo that is intentionally still blank and increases the chance of preserving bad boundaries.

### 3. Audit only, with no repo guidance docs

Rejected because the roadmap’s scope rules and merge discipline need to exist before future implementation starts.

## Approved Approach

Complete a strict Phase 0 slice with three required outputs:

1. `AUDIT.md` as the complete inventory of hardcoded assumptions and coupling seams.
2. `CONTRIBUTING.md` as the standing project rules for scope, tagging, and upstream hygiene.
3. `tasks/todo.md` as the phase-by-phase execution tracker for the roadmap.

No code or module scaffolding is created under `main/` during this slice.
Phase 1 must not start until these three outputs exist and the Phase 0 review notes are recorded.

## Audit Boundaries

### In scope

- `upstream/termux-app` as the primary fork source
- references in `upstream/termux-packages` and `upstream/proot-distro` that materially affect later phases
- hardcoded package-name assumptions
- hardcoded app-data and prefix paths
- `$PREFIX` setup, propagation, and implicit assumptions
- launcher/UI entry points that will matter when the app becomes an embeddable library

### Out of scope

- editing upstream sources
- building the Android library skeleton
- implementing prefix parameterization
- creating the embed API
- packaging bootstrap assets
- starting Phase 1 before Phase 0 deliverables are complete

## Deliverable Shape

### `AUDIT.md`

The audit document should be organized by category and by file, with enough detail to drive later edits without re-running discovery:

- path literals
- package-name references
- environment/bootstrap assumptions
- UI and standalone app entry points
- packaging/build/publishing assumptions
- upstream notes from `termux-packages` and `proot-distro`

Each entry should record:

- file path
- line number or region
- exact assumption
- why it matters to the fork
- expected future phase

### `CONTRIBUTING.md`

This document should encode the roadmap’s scope boundaries on day one:

- until `supported-packages.txt` exists in Phase 4, no package beyond the minimal bootstrap set needed for shell startup and IDE-critical tooling may be assumed supported
- once `supported-packages.txt` exists, any package outside that file is out of scope unless the file is explicitly updated in the same change
- proot is optional and isolated from core
- `$PREFIX` must always be derived at runtime
- all fork divergences must be tagged
- the library owns runtime, not host UI layout
- upstream clones are reference-only and must remain untouched

### `tasks/todo.md`

This file should turn the roadmap into actionable tracked work, with:

- a first section for the current Phase 0 slice
- later sections for Phases 1 through 6
- explicit verification steps
- a short review section updated at the end of the slice

## Verification

This slice is documentation-heavy, so verification is evidence-based rather than build-based:

- confirm `main/` is still empty
- confirm `AUDIT.md` inventories the discovered assumptions rather than summarizing vaguely
- confirm `CONTRIBUTING.md` codifies the roadmap boundaries
- confirm `tasks/todo.md` tracks the current slice and later phases

## Exit Criteria

This slice is complete when:

- `AUDIT.md` contains a complete first-pass inventory for the upstream sources relevant to the roadmap
- `CONTRIBUTING.md` exists with scope boundaries and tagging rules
- `tasks/todo.md` exists with Phase 0 marked and later phases queued
- `main/` remains untouched
- no Phase 1 scaffolding has started
- the repo is ready for the next slice: Android skeleton work informed by the audit

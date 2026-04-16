# Boundary Check

## Phase 7-8 Audit

### Bootstrap lifecycle surface

`core/` owns `iTermuxBootstrapState`, bootstrap failure causes, ABI selection,
and the extraction state machine because those pieces describe the runtime's
actual condition and are required to keep callers from entering sessions before
the prefix is valid. This stays on the runtime side of the boundary because it
exposes state and enforces integrity rules already promised by the library
contract; it does not decide what the host should show or which recovery UX to
apply after `FAILED`.

### Environment validation

`iTermuxEnvironmentValidator` belongs in `core/` because checking for expected
directories, core binaries, executable bits, and prefix coherence is runtime
fact gathering. The host receives `DEGRADED` plus a named `iTermuxDegradedCause`
and decides whether to retry, warn, or block the user. No host policy or DS
fallback logic is encoded in the validator.

### Lifecycle callbacks and config

`iTermuxRuntimeLifecycleListener`, callback-thread selection, ABI overrides,
prefix overrides, and proot enablement stay in `core/` because they define the
shape of the library contract and how the host can subscribe to state changes.
These APIs expose state and delivery mechanics only; they do not normalize
states into DS concepts or choose host behavior in response.

### Session controller

`iTermuxSessionController` belongs in `core/` because one restart attempt after
`KILLED_BY_OS` is part of the runtime contract from the Phase 7 roadmap rather
than host UX policy. The controller owns only mechanical transitions:
`STARTING`, `RUNNING`, `SUSPENDED`, `KILLED_BY_OS`, `RECOVERING`, `READY`, and
`DEAD`, along with named failure causes when start or recovery fails. It does
not notify users, escalate to proot automatically, or decide whether a dead
session should be reopened.

### Proot plugin

The optional proot launcher stays outside `core/` because proot is a secondary
execution path, not part of the native runtime contract. The plugin exposes the
same host-facing session shape and named failure causes, while `core/` remains
free of distro-specific policy. Host interpretation of `PROOT_UNAVAILABLE`
still belongs outside the library.

## Result

No boundary drift was found in the current Phase 7-8 surface. The remaining
host-side work is normalization and orchestration in the DS/sample layer, not
more policy inside `core/`.

# internal-termux todo

## Roadmap cross-check

- [x] Phase 0 audit and repo rules are in place.
- [x] Phase 1 runtime-derived prefix, environment, and shell seams are in place.
- [x] Phase 2 namespace migration, merge markers, and literal checks are in place.
- [x] Phase 3 library-module conversion is in place: `core/` and `proot-plugin/` are Android libraries, and `sample-app/` is the host app.
- [x] Phase 4 packaged bootstrap install and refresh flow are in place.
- [x] Phase 5 shared native/proot session contract is in place.
- [x] Phase 6 merge-protection tooling and docs are in place.

## Phase 7-9 implementation plan

### Phase 7 - Gap closure foundation

- [ ] Replace the boolean bootstrap readiness model with an explicit lifecycle surface:
  `BootstrapState`, bootstrap failure causes, and runtime fields that make
  `EXTRACTING`, `PARTIAL`, `VERIFYING`, `FAILED`, `READY`, and `DEGRADED`
  first-class state instead of derived guesswork.
- [x] Implement a bootstrap state machine in `core/` that performs full
  re-extraction on the single allowed retry, enforces the 30-second retry gate,
  and makes `VERIFYING` mandatory before `READY`.
- [x] Add ABI-aware bootstrap selection with `BootstrapResolver`, supported ABI
  overrides in config, and a named unsupported-ABI failure path before any
  extraction work begins.
- [ ] Add fast-path environment validation on `init()` via
  `EnvironmentValidator`, including `DegradedCause` coverage for missing
  binaries, executable-bit drift, corrupted installs, ABI mismatch, and sandbox
  invalidation.
- [ ] Convert bootstrap failures from unchecked exceptions to named runtime
  failure causes so the host sees state transitions instead of surprise throws.

### Phase 7 - Session lifecycle hardening

- [ ] Extend the host-facing session model with `SessionState`, recovery
  bookkeeping, and restart semantics that allow exactly one self-restoration
  attempt after `KILLED_BY_OS`.
- [ ] Implement the session transition controller in `core/` without pushing
  policy decisions into the runtime boundary.
- [ ] Add verified kill-and-recovery tests that prove `RUNNING` ->
  `KILLED_BY_OS` -> `RECOVERING` -> `READY` on success and `RUNNING` ->
  `KILLED_BY_OS` -> `DEAD` on exhausted recovery.

### Phase 8 - Contract surface polish

- [x] Define the lifecycle listener contract in `core/`:
  `onBootstrapState`, `onSessionState`, and `onEnvironmentValidation`, with
  callback-thread delivery controlled by config.
- [ ] Expand `iTermuxConfig` to cover prefix override, supported ABI override,
  bootstrap asset key/variant selection, callback-thread selection, and proot
  enablement without leaking host policy into `core/`.
- [ ] Close the failure taxonomy around the runtime contract so bootstrap,
  environment, and session failures all use named causes.
- [ ] Write the Phase 7-8 boundary audit into `BOUNDARY_CHECK.md`, one
  component at a time, and move any policy logic out of `core/` if the audit
  finds drift.
- [ ] Update `AUDIT.md` with the supported ABI matrix and any new bootstrap
  assumptions introduced by the ABI split.

### Phase 9 - DS integration and discovery

- [ ] Define and commit the DS normalization table that maps high-resolution
  iTermux states onto DS runtime states before integration code starts.
- [ ] Add a minimal DS integration spike in `sample-app/` that exercises the
  full lifecycle contract, including bootstrap load and session recovery paths.
- [ ] Add failure-injection seams and documented test procedures for interrupted
  extraction, repeated failure within the retry window, corrupted bootstrap,
  unsupported ABI, recoverable degradation, structural degradation, and session
  kill recovery.
- [ ] Record the multi-device test matrix and capture a first performance
  baseline for cold init, warm init, shell prompt, idle memory, and recovery
  latency.
- [ ] Re-run the boundary audit after DS integration stabilizes and record the
  findings in `BOUNDARY_CHECK.md` before closing Phase 9.

## Current execution slice

- [x] Start with Phase 7 bootstrap contract work in this order:
  1. failing tests for bootstrap lifecycle state + failure cause surfacing
  2. runtime model changes
  3. state-machine extraction flow
  4. focused verification
  5. graph rebuild
  6. detailed commit

## Review

- Roadmap reality on 2026-04-16: Phase 7 is the next true implementation phase.
  The current `core/` surface still models bootstrap readiness as
  `isBootstrapRequired: Boolean` and session creation as static metadata, so the
  roadmap work starts with runtime contract reshaping rather than DS-facing UI.
- Phase 9 remains intentionally blocked on a stable Phase 8 contract surface.
  The sample app can be used as the DS integration spike once lifecycle events,
  failure causes, and normalization seams exist.
- First implementation slice: bootstrap state machine and failure-cause
  surfacing in `core/`, driven test-first from the existing runtime initializer
  and bootstrap installer seams.
- First Phase 7 slice landed on 2026-04-16: `core/` now exposes explicit
  bootstrap state and named runtime failure causes, and auto-bootstrap
  extraction failures return `FAILED` with
  `BOOTSTRAP_EXTRACTION_FAILED` instead of throwing through the library
  boundary.
- Second Phase 7 slice landed on 2026-04-16: refresh-time environment validation
  now distinguishes valid extracted runtimes from degraded ones, surfacing
  `DEGRADED` with a named `DegradedCause` when the prefix structure exists but
  the core shell contract is broken.
- Third Phase 7 slice landed on 2026-04-16: bootstrap initialization now
  resolves ABI-specific packaged payloads, records the selected bootstrap
  variant on the runtime, fails early with `UNSUPPORTED_ABI` when no variant
  matches, and ships mirrored per-ABI bootstrap assets for `arm64-v8a`,
  `armeabi-v7a`, and `x86_64`.
- Fourth Phase 7 slice landed on 2026-04-16: auto-bootstrap now runs through an
  explicit bootstrap state machine with persisted failure tracking, one allowed
  retry outside the 30-second window, mandatory `VERIFYING` before `READY`, and
  `CORRUPTED`/`FAILED` outcomes when extraction verification does not hold.
- First Phase 8 slice landed on 2026-04-16: `core/` now exposes a lifecycle
  listener contract for bootstrap, session, and environment events, dispatches
  callbacks through a configurable callback-thread policy, and forwards
  bootstrap/environment notifications from runtime initialization plus
  `STARTING`/`RUNNING` session events from the public session facade.
- Focused verification on 2026-04-16:
  `./gradlew.bat :core:testDebugUnitTest --tests "com.darkian.itermux.core.iTermuxRuntimeInitializerTest" --tests "com.darkian.itermux.core.iTermuxAutoBootstrapTest" --console=plain`
  and `./gradlew.bat :core:testDebugUnitTest --console=plain`.
- Additional focused verification on 2026-04-16:
  `./gradlew.bat :core:testDebugUnitTest --tests "com.darkian.itermux.core.iTermuxRuntimeRefreshTest" --console=plain`
  and `./gradlew.bat :core:testDebugUnitTest --console=plain`.
- ABI slice verification on 2026-04-16:
  `./gradlew.bat :core:testDebugUnitTest --tests "com.darkian.itermux.core.iTermuxBootstrapResolverTest" --tests "com.darkian.itermux.core.iTermuxBootstrapAssetsTest" --tests "com.darkian.itermux.core.iTermuxAutoBootstrapTest" --console=plain`
  and `./gradlew.bat :core:testDebugUnitTest --console=plain`.
- State-machine slice verification on 2026-04-16:
  `./gradlew.bat :core:testDebugUnitTest --tests "com.darkian.itermux.core.iTermuxBootstrapStateMachineTest" --console=plain`,
  `./gradlew.bat :core:testDebugUnitTest --tests "com.darkian.itermux.core.iTermuxAutoBootstrapTest" --console=plain`,
  and `./gradlew.bat :core:testDebugUnitTest --console=plain`.
- Lifecycle slice verification on 2026-04-16:
  `./gradlew.bat :core:testDebugUnitTest --tests "com.darkian.itermux.core.iTermuxLifecycleListenerTest" --console=plain`
  and `./gradlew.bat :core:testDebugUnitTest --console=plain`.

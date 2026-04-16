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

- [x] Replace the boolean bootstrap readiness model with an explicit lifecycle surface:
  `BootstrapState`, bootstrap failure causes, and runtime fields that make
  `EXTRACTING`, `PARTIAL`, `VERIFYING`, `FAILED`, `READY`, and `DEGRADED`
  first-class state instead of derived guesswork.
- [x] Implement a bootstrap state machine in `core/` that performs full
  re-extraction on the single allowed retry, enforces the 30-second retry gate,
  and makes `VERIFYING` mandatory before `READY`.
- [x] Add ABI-aware bootstrap selection with `BootstrapResolver`, supported ABI
  overrides in config, and a named unsupported-ABI failure path before any
  extraction work begins.
- [x] Add fast-path environment validation on `init()` via
  `EnvironmentValidator`, including `DegradedCause` coverage for missing
  binaries, executable-bit drift, corrupted installs, ABI mismatch, and sandbox
  invalidation.
- [x] Convert bootstrap failures from unchecked exceptions to named runtime
  failure causes so the host sees state transitions instead of surprise throws.

### Phase 7 - Session lifecycle hardening

- [x] Extend the host-facing session model with `SessionState`, recovery
  bookkeeping, and restart semantics that allow exactly one self-restoration
  attempt after `KILLED_BY_OS`.
- [x] Implement the session transition controller in `core/` without pushing
  policy decisions into the runtime boundary.
- [x] Add verified kill-and-recovery tests that prove `RUNNING` ->
  `KILLED_BY_OS` -> `RECOVERING` -> `READY` on success and `RUNNING` ->
  `KILLED_BY_OS` -> `DEAD` on exhausted recovery.

### Phase 8 - Contract surface polish

- [x] Define the lifecycle listener contract in `core/`:
  `onBootstrapState`, `onSessionState`, and `onEnvironmentValidation`, with
  callback-thread delivery controlled by config.
- [x] Expand `iTermuxConfig` to cover prefix override, supported ABI override,
  bootstrap asset key/variant selection, callback-thread selection, and proot
  enablement without leaking host policy into `core/`.
- [x] Close the failure taxonomy around the runtime contract so bootstrap,
  environment, and session failures all use named causes.
- [x] Write the Phase 7-8 boundary audit into `BOUNDARY_CHECK.md`, one
  component at a time, and move any policy logic out of `core/` if the audit
  finds drift.
- [x] Update `AUDIT.md` with the supported ABI matrix and any new bootstrap
  assumptions introduced by the ABI split.

### Phase 9 - DS integration and discovery

- [x] Define and commit the DS normalization table that maps high-resolution
  iTermux states onto DS runtime states before integration code starts.
- [x] Add a minimal DS integration spike in `sample-app/` that exercises the
  full lifecycle contract, including bootstrap load and session recovery paths.
- [x] Add failure-injection seams and documented test procedures for interrupted
  extraction, repeated failure within the retry window, corrupted bootstrap,
  unsupported ABI, recoverable degradation, structural degradation, and session
  kill recovery.
- [x] Record the multi-device test matrix and capture a first performance
  baseline for cold init, warm init, shell prompt, idle memory, and recovery
  latency.
- [x] Re-run the boundary audit after DS integration stabilizes and record the
  findings in `BOUNDARY_CHECK.md` before closing Phase 9.

## Current execution slice

- [x] Close the remaining Phase 7 environment-validation gap by covering all
  named degraded causes in `EnvironmentValidator` and refresh-time tests.
- [x] Commit the DS normalization table, implement the mapper in `sample-app/`,
  and keep raw iTermux states out of DS-facing code outside that layer.
- [x] Turn `sample-app/` into the minimal DS spike by wiring a lifecycle
  recorder, bootstrap/session normalization, and a simulated session recovery
  path through the real listener contract.
- [x] Add the Phase 9 discovery artifacts: failure-injection seams plus
  documented procedures, a multi-device matrix, and an initial performance
  baseline.
- [x] Re-run the boundary audit for Phase 9, run focused verification, rebuild
  `graphify-out/`, and commit the remaining slice with a detailed message.

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
- Second Phase 8 slice landed on 2026-04-16: `iTermuxConfig` now carries
  prefix override, ABI override, bootstrap variant selection inputs, callback
  thread selection, and proot enablement, while path resolution and runtime
  metadata preserve those choices without pushing interpretation into `core/`.
- Third Phase 7 slice on 2026-04-16 closed the session hardening gap:
  `iTermuxSession` now carries lifecycle state, recovery bookkeeping, and named
  failure causes; `iTermuxSessionController` owns start/suspend/resume/recovery
  transitions; native/proot session starts now fail with named causes instead
  of surfacing unchecked exceptions through the host boundary.
- The Phase 7-8 boundary audit now lives in `BOUNDARY_CHECK.md`, and the stale
  unchecked items for bootstrap lifecycle surface, ABI matrix documentation, and
  the failure taxonomy have been corrected to match the current repo state.
- Final Phase 7 validation slice landed on 2026-04-16: `EnvironmentValidator`
  now covers missing binaries, executable-bit drift, corrupted installs, ABI
  mismatch, and sandbox invalidation through an explicit file-access seam, and
  refresh-time tests prove those degraded causes surface as named runtime state
  instead of silent breakage.
- Phase 9 normalization landed on 2026-04-16:
  `docs/ds-normalization.md` commits the DS table, and `sample-app/` now keeps
  raw iTermux states inside `iTermuxDsStateMapper` plus
  `iTermuxDsLifecycleRecorder`.
- The minimal DS spike landed on 2026-04-16: `sample-app/MainActivity` now
  registers the lifecycle listener, displays normalized DS runtime state,
  exercises a native session recovery path, and records a proot session snapshot
  without pushing DS policy back into `core/`.
- Phase 9 discovery artifacts landed on 2026-04-16:
  `docs/failure-injection.md` records the injected scenarios and their test
  seams, `docs/phase9-discovery.md` records the device matrix plus the first
  host-side baseline, and `BOUNDARY_CHECK.md` now includes the Phase 9 audit.
- Focused verification on 2026-04-16:
  `./gradlew.bat :core:testDebugUnitTest --tests "com.darkian.itermux.core.iTermuxRuntimeInitializerTest" --tests "com.darkian.itermux.core.iTermuxAutoBootstrapTest" --console=plain`
  and `./gradlew.bat :core:testDebugUnitTest --console=plain`.
- Additional focused verification on 2026-04-16:
  `./gradlew.bat :core:testDebugUnitTest --tests "com.darkian.itermux.core.iTermuxRuntimeRefreshTest" --console=plain`
  and `./gradlew.bat :core:testDebugUnitTest --console=plain`.
- ABI slice verification on 2026-04-16:
  `./gradlew.bat :core:testDebugUnitTest --tests "com.darkian.itermux.core.iTermuxBootstrapResolverTest" --tests "com.darkian.itermux.core.iTermuxBootstrapAssetsTest" --tests "com.darkian.itermux.core.iTermuxAutoBootstrapTest" --console=plain`
- Final Phase 7-9 verification on 2026-04-16:
  `./gradlew.bat :core:testDebugUnitTest --tests "com.darkian.itermux.core.iTermuxEnvironmentValidatorTest" --tests "com.darkian.itermux.core.iTermuxRuntimeRefreshTest" --console=plain`,
  `./gradlew.bat :sample-app:testDebugUnitTest --tests "com.darkian.itermux.sample.iTermuxDsStateMapperTest" --tests "com.darkian.itermux.sample.iTermuxDsLifecycleRecorderTest" --console=plain`,
  `./gradlew.bat :sample-app:testDebugUnitTest --tests "com.darkian.itermux.sample.iTermuxPerformanceBaselineProbeTest" --rerun-tasks --console=plain --info`,
  `./gradlew.bat :sample-app:testDebugUnitTest --console=plain`,
  `./gradlew.bat :core:testDebugUnitTest --console=plain`,
  `./gradlew.bat :proot-plugin:testDebugUnitTest --console=plain`,
  `./gradlew.bat assembleDebug --console=plain`,
  `powershell -ExecutionPolicy Bypass -File tools/verify-no-termux-literals.ps1`,
  `powershell -ExecutionPolicy Bypass -File tools/verify-supported-packages-sync.ps1`,
  `bash -x tools/merge-check.sh origin/main HEAD` (`SAFE`),
  and `python -c "from graphify.watch import _rebuild_code; from pathlib import Path; _rebuild_code(Path('.'))"`.
  and `./gradlew.bat :core:testDebugUnitTest --console=plain`.
- State-machine slice verification on 2026-04-16:
  `./gradlew.bat :core:testDebugUnitTest --tests "com.darkian.itermux.core.iTermuxBootstrapStateMachineTest" --console=plain`,
  `./gradlew.bat :core:testDebugUnitTest --tests "com.darkian.itermux.core.iTermuxAutoBootstrapTest" --console=plain`,
  and `./gradlew.bat :core:testDebugUnitTest --console=plain`.
- Lifecycle slice verification on 2026-04-16:
  `./gradlew.bat :core:testDebugUnitTest --tests "com.darkian.itermux.core.iTermuxLifecycleListenerTest" --console=plain`
  and `./gradlew.bat :core:testDebugUnitTest --console=plain`.
- Config slice verification on 2026-04-16:
  `./gradlew.bat :core:testDebugUnitTest --tests "com.darkian.itermux.core.iTermuxPathResolverTest" --tests "com.darkian.itermux.core.iTermuxRuntimeInitializerTest" --console=plain`
  and `./gradlew.bat :core:testDebugUnitTest --console=plain`.
- Session slice verification on 2026-04-16:
  `./gradlew.bat :core:testDebugUnitTest --tests "com.darkian.itermux.core.iTermuxSessionControllerTest" --tests "com.darkian.itermux.core.iTermuxNativeSessionTest" --tests "com.darkian.itermux.core.iTermuxLifecycleListenerTest" --console=plain`,
  `./gradlew.bat :proot-plugin:testDebugUnitTest --tests "com.darkian.itermux.proot.iTermuxProotPluginTest" --console=plain`,
  and `./gradlew.bat :core:testDebugUnitTest --tests "com.darkian.itermux.core.iTermuxBootstrapInstallerTest" --console=plain`.
- Full slice verification on 2026-04-16:
  `./gradlew.bat :core:testDebugUnitTest --console=plain`,
  `./gradlew.bat :proot-plugin:testDebugUnitTest --console=plain`,
  `./gradlew.bat assembleDebug --console=plain`,
  `powershell -ExecutionPolicy Bypass -File tools/verify-no-termux-literals.ps1`,
  `powershell -ExecutionPolicy Bypass -File tools/verify-supported-packages-sync.ps1`,
  `bash -x tools/merge-check.sh origin/main HEAD`,
  and `python -c "from graphify.watch import _rebuild_code; from pathlib import Path; _rebuild_code(Path('.'))"`.

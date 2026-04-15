# internal-termux todo

## Roadmap cross-check

- [x] Phase 0 audit and repo rules are in place.
- [x] Phase 1 runtime-derived prefix, environment, and shell seams are in place.
- [x] Phase 2 namespace migration, merge markers, and literal checks are in place.
- [x] Phase 3 library-module conversion is in place: `core/` and `proot-plugin/` are Android libraries, and `sample-app/` is the host app.

## One remaining task list

- [x] Commit a real minimal bootstrap payload under `core` assets instead of only exposing the expected asset path.
- [x] Make `iTermux.initialize()` install the packaged bootstrap automatically on cold start and refresh runtime state before returning.
- [x] Add serial unit coverage for offline bootstrap-on-init behavior and packaged asset expectations.
- [x] Replace the placeholder `proot-plugin/` surface with a real optional session launcher built on the shared host-facing session contract.
- [x] Generalize session metadata so native and plugin-provided sessions share one contract without hardwiring proot into `core`.
- [x] Verify native and proot session creation from the same host API surface while keeping environment bleed-through opt-in only.
- [x] Replace the placeholder `tools/merge-check.sh` with tagged-file upstream diff detection and actionable `SAFE` / `REVIEW` / `CONFLICT` output.
- [x] Document the upstream sync and merge-protection workflow and align stale repo docs with the current implementation reality.
- [x] Delete stale planning notes and other redundant docs that no longer reflect the active roadmap state.
- [x] Run serial verification across unit tests, app assembly, literal checks, package sync checks, and merge-check behavior.

## Review

- Roadmap reality on 2026-04-15: the remaining work is concentrated in Phase 4
  auto-bootstrap, Phase 5 shared proot integration, and Phase 6 merge
  protection. The earlier queue items in the previous todo file were stale and
  have been collapsed into the single checklist above.
- Phase 4 now has a committed offline bootstrap payload at
  `core/src/main/assets/itermux/bootstrap/bootstrap.tar.xz`, and the public
  `iTermux.initialize()` path auto-installs it on cold start before returning
  runtime state.
- Phase 5 now keeps proot optional while sharing the same host-facing session
  contract: native sessions come from `core`, proot sessions come from
  `proot-plugin`, and environment inheritance across that boundary is opt-in.
- Phase 6 now has a real `tools/merge-check.sh`, upstream sync documentation in
  `docs/upstream-sync.md`, refreshed top-level docs, and the stale Phase 0
  planning note has been deleted.
- Verification on 2026-04-15:
  `./gradlew.bat :core:testDebugUnitTest`,
  `./gradlew.bat :proot-plugin:testDebugUnitTest`,
  `./gradlew.bat assembleDebug`,
  `powershell -ExecutionPolicy Bypass -File tools/verify-no-termux-literals.ps1`,
  `powershell -ExecutionPolicy Bypass -File tools/verify-supported-packages-sync.ps1`,
  `bash -x tools/merge-check.sh origin/main HEAD`,
  and `python -c "from graphify.watch import _rebuild_code; from pathlib import Path; _rebuild_code(Path('.'))"`.

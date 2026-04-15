# internal-termux

`internal-termux` is an embeddable Android runtime fork that keeps Termux-style
paths, bootstrap, and session wiring under host-app ownership instead of under
a standalone `com.termux` app shell.

## Modules

- `core/`: embeddable runtime library with host-derived paths, runtime config,
  native session builders, and offline bootstrap installation
- `proot-plugin/`: optional proot launcher that returns the same host-facing
  session contract as `core`
- `sample-app/`: minimal host app for verifying the embed API surface
- `tools/`: verification and merge-protection scripts

## Package convention

Android package ownership follows the project convention:

- `com.darkian.itermux.*` for this repo
- `com.darkian.studio.*` for DDS after its rename

## Current state

- `iTermux.initialize()` derives the runtime from the host app, writes
  `termux.env`, reads `termux.properties`, and auto-installs the packaged
  offline bootstrap on cold start when the payload is available.
- Native sessions come from `core`, while proot sessions stay isolated in the
  optional plugin and share the same `iTermuxSession` contract.
- `sample-app/` is the verification host. There is no standalone Termux
  launcher in the library modules.
- `tools/merge-check.sh` reports `SAFE`, `REVIEW`, and `CONFLICT` states for
  tagged fork files relative to an upstream-tracking ref.

## Verification helpers

- `./gradlew.bat :core:testDebugUnitTest`
- `./gradlew.bat :proot-plugin:testDebugUnitTest`
- `./gradlew.bat assembleDebug`
- `powershell -ExecutionPolicy Bypass -File tools\\verify-no-termux-literals.ps1`
- `powershell -ExecutionPolicy Bypass -File tools\\verify-supported-packages-sync.ps1`
- `bash tools/merge-check.sh [base-ref] [head-ref]`

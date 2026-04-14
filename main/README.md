# internal-termux main

This directory is the actual project root for the Android work. It is scaffolded
from the Phase 0 audit, which means the structure is intentionally organized
around the runtime seams instead of the upstream standalone app shell.

## Modules

- `core/`: future embeddable runtime library
- `proot-plugin/`: optional proot integration module
- `sample-app/`: minimal host app for verifying the embed surface
- `tools/`: project tooling placeholders, including future merge helpers

## Package convention

Android package ownership follows the project convention:

- `com.darkian.itermux.*` for this repo
- `com.darkian.studio.*` for DDS after its rename

## Notes

- This scaffold does not claim the runtime is implemented yet.
- The highest-risk seam remains prefix derivation and environment injection.
- Standalone upstream activities and services are not copied into this skeleton.
- The initial Gradle baseline is aligned to the current upstream toolchain where
  practical, while keeping the modules intentionally minimal.
- The build skeleton is still a scaffold. Runtime extraction, bootstrap wiring,
  and upstream code migration have not started yet.

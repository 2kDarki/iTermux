# internal-termux todo

## Current slice: Phase 1 `iTermux` core seam

- [x] Rename the Android scaffold to the `com.darkian.itermux` convention.
- [x] Replace the placeholder facade with a dedicated `iTermux` path model and resolver.
- [x] Build a baseline shell environment from host-derived runtime paths.
- [x] Add a shell launch specification/builder backed by the same path model.
- [x] Add `$PREFIX` alias expansion/collapse helpers for future upstream file migration.
- [x] Prove the `core` seam with focused `:core:testDebugUnitTest` coverage.
- [x] Add the next file-path migration slice from upstream `TermuxFileUtils`.
- [x] Port `~` alias support and list-based alias helpers onto `iTermux` path utilities.
- [x] Port canonical path normalization for host-owned runtime paths.
- [x] Port working-directory parent matching for app files, storage home, and shared storage roots.
- [x] Port fail-safe shell environment profiles and sourceable `.env` rendering from upstream shell environment behavior.
- [x] Port interpreter-aware shell argument setup for ELF files, shebang scripts, and plain scripts.
- [x] Wire the shell builder to the shared environment profiles and interpreter-aware file command path.
- [x] Turn `iTermux.initialize()` into a real runtime initializer that materializes layout and writes `termux.env`.
- [x] Add a raw `termux.properties` file selection and loading seam with upstream-style precedence.
- [x] Respect `default-working-directory` from `termux.properties` in runtime-backed shell defaults.
- [x] Expose prefix bootstrap-readiness state from the initialized runtime.
- [x] Surface selected properties file and resolved default working directory on the runtime object.
- [x] Add a runtime refresh path for re-reading properties and bootstrap state after disk changes.
- [x] Add runtime-level session factories so callers can create shell specs directly from initialized runtime state.
- [x] Add a host-derived runtime identity seam for package-based authorities, actions, classes, and plugin package names.
- [x] Tag the current core runtime files with the merge marker comment required for upstream-derived work.
- [x] Add a serial namespace verification script for legacy `com.termux` and hardcoded prefix literals in the active project tree.
- [x] Add a host-facing native session object and default `iTermux.createSession()` entry point on top of the shell-spec builders.
- [x] Re-run serial `:core:testDebugUnitTest` verification after each migration slice.
- [x] Run a wider serial project verification pass now that the `core` seam is materially richer.

## Completed slice: scaffold the Android project root

## Completed slice: Phase 0 audit plus repo docs

- [x] Read the roadmap, collaboration protocol, and repo layout constraints.
- [x] Inventory hardcoded `com.termux` assumptions in `upstream/termux-app`.
- [x] Inventory hardcoded `/data/data/com.termux` and related prefix path assumptions.
- [x] Inventory `$PREFIX` setup, injection, and implicit environment assumptions.
- [x] Inventory standalone UI entry points and host-app coupling seams.
- [x] Record relevant reference notes from `upstream/termux-packages` and `upstream/proot-distro`.
- [x] Write `AUDIT.md` as the surgery map for later phases.
- [x] Write `CONTRIBUTING.md` with scope boundaries, tagging rules, and upstream hygiene.
- [x] Verify the future Android project area is still empty after the slice.
- [x] Add a short review section with findings and next-slice trigger.

## Roadmap queue

### Phase 1: Parameterize the prefix

- [ ] Define one runtime-derived prefix source of truth.
- [ ] Replace all hardcoded prefix literals with derived paths.
- [ ] Rewire shell/bootstrap environment injection around the derived prefix.
- [ ] Verify runtime paths under a non-Termux application ID.

### Phase 2: Rename and protect

- [ ] Replace `com.termux` package assumptions with the project namespace.
- [ ] Tag every modified upstream-derived file with the merge marker comment.
- [ ] Verify the fork runs cleanly under the renamed package.

### Phase 3: Convert to a library module

- [ ] Evolve `core/` as an Android library informed by the audit.
- [ ] Remove or stub standalone launcher entry points.
- [ ] Expose the embed API surface and verify it in `sample-app/`.

### Phase 4: Controlled bootstrap

- [x] Define `supported-packages.txt`.
- [ ] Build and integrate the minimal bootstrap payload.
- [ ] Verify offline cold-start initialization via the library API.

### Phase 5: proot plugin

- [ ] Isolate proot support into `proot-plugin/`.
- [ ] Define the shared session abstraction for native and proot sessions.
- [ ] Verify separate native and proot sessions through the same host API.

### Phase 6: Merge protection

- [ ] Implement the upstream diff workflow and `tools/merge-check.sh`.
- [ ] Verify tagged-file review detection against the upstream mirror.
- [ ] Document the upstream sync procedure.

## Review

- Current Phase 1 seam now derives runtime paths, shell environment, shell
  specs, and `$PREFIX` alias handling from the host app instead of hardcoding
  repo-owned paths.
- The resolver now derives app-scoped paths such as the `termux-am` socket from
  the host package name, which keeps the seam aligned with the roadmap's
  runtime-derived ownership rule.
- The seam now also carries upstream-style `~` and `$PREFIX` alias expansion,
  list-based alias helpers, POSIX canonicalization, working-directory parent
  matching, fail-safe environment profiles, sourceable `.env` rendering, and
  interpreter-aware shell argument setup for script execution.
- `iTermux.initialize()` now returns initialized runtime state instead of only
  raw paths, and it materializes the host-owned layout plus the derived
  `termux.env` file as part of initialization.
- The core module now also understands the `termux.properties` file locations
  and precedence order, which gives later runtime slices a reusable config seam
  without importing the heavier upstream app property stack yet.
- Runtime-backed shell specs now respect the configured
  `default-working-directory` property when it points at a readable directory,
  otherwise they fall back safely to the host-owned home directory.
- The runtime now exposes whether the prefix still needs bootstrap content,
  using an upstream-style emptiness check that ignores the generated env/tmp
  placeholders but treats real prefix files as installed runtime content.
- The initialized runtime now also surfaces which properties file actually won
  precedence and what default working directory was resolved from it, so callers
  do not need to recompute that metadata from lower-level helpers.
- The public runtime surface can now be refreshed from existing paths after
  properties or prefix contents change, which gives later bootstrap and settings
  work a real reload lifecycle instead of forcing full re-initialization logic.
- The initialized runtime now acts more like a real caller-facing API by
  exposing direct login-shell, command, and file-command factories that reuse
  the runtime's resolved defaults instead of making callers drop back to lower-
  level builder entry points.
- The runtime now also exposes a host-derived identity model adapted from the
  upstream `TermuxConstants` package-name seam, covering authorities, class
  names, service/action strings, and plugin package names so Phase 2 renaming
  work has one source of truth instead of scattered string construction.
- The active `core` runtime files are now tagged with the required
  `INTERNAL-TERMUX MODIFIED - merge carefully` marker, and
  `tools/verify-no-termux-literals.ps1` enforces both the absence of legacy
  `com.termux`/`/data/data/com.termux` literals in the live Android project and
  the presence of those markers across the current runtime seam.
- The embed surface is now one step closer to the roadmap target: callers can
  create a first-class native session object with stable session metadata
  instead of only receiving raw shell specs, and the default
  `iTermux.createSession()` facade now maps directly onto that native session
  abstraction.
- `supported-packages.txt` now exists and starts from the smallest package set
  currently justified by DDS-side runtime behavior: a shell, core userland,
  git, TLS certificates, HTTP fetch support, and `xz` handling for compressed
  payloads.
- Verification: `./gradlew.bat projects` succeeded for the scaffold, and
  repeated serial `./gradlew.bat --stop; ./gradlew.bat :core:testDebugUnitTest`
  runs are passing after the path, environment, shell, and interpreter work.
- Verification: `./gradlew.bat --stop; ./gradlew.bat assembleDebug` now also
  succeeds across `:core`, `:proot-plugin`, and `:sample-app`, so the richer
  `core` seam still fits the full project graph.
- Next migration target: lift the next UI-independent upstream runtime seam
  into `core` now that both unit and project-level verification are green,
  while still avoiding bootstrap and launcher-heavy code until the reusable
  runtime base is stronger.

## Scaffold review

- Scaffolded the repo root as a dedicated Android project with `core`,
  `proot-plugin`, `sample-app`, and `tools`, matching the target repo shape
  without copying the upstream standalone app shell.
- Anchored the first placeholder API in `core` around runtime-owned path
  derivation so the scaffold starts from the prefix seam the audit identified,
  not from `TermuxActivity` or `TermuxService`.
- Kept proot isolated as a module marker only and made `sample-app` a minimal
  host that depends on `core` rather than embedding upstream UI.
- Aligned the scaffold to the working `..\\dds` Gradle baseline and verified the
  module graph with `./gradlew.bat projects`.

## Phase 0 review

- Main finding: package identity, prefix paths, bootstrap behavior, and standalone
  UI ownership are more intertwined than a naive library scaffold would assume,
  with `TermuxConstants.java`, `TermuxShellEnvironment.java`,
  `TermuxFileUtils.java`, `TermuxInstaller.java`, `AndroidManifest.xml`, and
  `TermuxActivity.java` forming the highest-risk seam cluster.
- Main finding: `termux-packages` and `proot-distro` both still carry strong
  `TERMUX_PREFIX` and `/data/data/com.termux/...` assumptions, so Phase 4 and
  Phase 5 must be designed as controlled adaptations rather than direct imports.
- Verification: `AUDIT.md`, `CONTRIBUTING.md`, `tasks/todo.md`, and
  `tasks/lessons.md` exist, and the future Android project area remained empty
  during Phase 0.
- Next-slice trigger: start scaffolding the Android project only after using the audit
  hotspots to define the Android library structure and first Phase 1 touch
  points, beginning with the prefix-derivation seam rather than the UI shell.

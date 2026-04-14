# internal-termux — Roadmap

---

## The Core Principle

Termux is a working runtime. The goal is not to rebuild it — it is to stop it
from owning the environment it runs in. Everything in this roadmap follows from
that single idea.

The roadmap has two natural halves:

- **Phase 0–2** — Make Termux reusable. No hardcoded paths. No assumed package
  name. No fixed prefix. When this is done, Termux can live inside anything.
- **Phase 3–6** — Use that foundation to build the embeddable runtime your IDE
  actually needs.

Do not start Phase 3 until Phase 2 is complete and verified. The second half is
only tractable because the first half was done properly.

---

## Part One — Make Termux Reusable

---

### Phase 0 — Audit Before You Touch Anything

You cannot surgically modify something you have not mapped. This phase produces
no shippable code. It produces the document that makes all future changes safe.

**0.1 Clone the repos**

```
termux-app
termux-packages   # read only — understand the build system, don't mirror it
proot-distro
```

**0.2 Map every hardcoded assumption**

Search the entire codebase for:

- `com.termux` — every package name reference
- `/data/data/com.termux` — every hardcoded prefix path
- `$PREFIX` — every location it is set, assumed, or passed

Document every hit. File name, line number, what it does.

**0.3 Categorize what you find**

| Category | Meaning |
|---|---|
| Path literals | Strings that hardcode the prefix — must be parameterized |
| Package name refs | Application ID assumptions — must be renamed |
| Env var assumptions | Scripts that assume `$PREFIX` is already set correctly |
| UI entry points | Activities and launchers — will be replaced in Part Two |

**Deliverable:** `AUDIT.md` committed to your fork. Every hardcoded assumption
documented. This is your surgery map.

---

### Phase 1 — Parameterize the Prefix

This is the core hard problem. Everything else is downstream of it.

**1.1 Create a single source of truth**

Introduce one place where the runtime prefix is defined:

```java
// TermuxConstants.java (or equivalent)
public static String getPrefix(Context context) {
    return context.getApplicationInfo().dataDir + "/files/usr";
}
```

The prefix is now derived from whichever application is hosting the runtime,
not hardcoded to `com.termux`.

**1.2 Replace all path literals**

Every hardcoded path you found in Phase 0 gets replaced with a call to
`getPrefix()` or a constant derived from it at startup. No exceptions. If you
find yourself thinking "this one is fine to leave," it is not.

**1.3 Rewire `$PREFIX` injection**

Bootstrap scripts and shell sessions must receive `$PREFIX` as an injected
environment variable, set from `getPrefix()`, not from an assumption baked into
the script.

```bash
# Before (assumed)
export PREFIX=/data/data/com.termux/files/usr

# After (injected by the host)
export PREFIX=$INJECTED_PREFIX
```

**1.4 Verify the change is real**

Install the modified app under a different application ID. The terminal must
launch, the shell must work, and no path must reference `com.termux` at
runtime. If it does, you missed something in the audit.

**Deliverable:** Termux runs correctly under any application ID. `$PREFIX`
resolves dynamically to wherever the host app lives on disk.

---

### Phase 2 — Rename and Protect

**2.1 Rename the application ID**

Replace `com.termux` with your namespace — `com.yourname.internaltermux` or
similar — in every location the audit identified. This is mostly mechanical
find-replace, but the audit tells you exactly where.

**2.2 Tag every file you have modified**

Every file changed from upstream gets a header comment:

```java
// INTERNAL-TERMUX MODIFIED — merge carefully
```

This is not optional. This is how the merge protection system in Phase 6 works.
You are planting the markers now so the tooling can find them later.

**2.3 Confirm the seam**

At the end of Phase 2, you have a fork of Termux that:

- Runs under your package name
- Resolves all paths dynamically
- Has every divergence from upstream explicitly tagged

Part One is done. Termux is now reusable.

---

## Part Two — Build the Embeddable Runtime

Part One proved that Termux can run anywhere. Part Two uses that fact to turn
it into a library your IDE imports rather than an app your users install
separately.

---

### Phase 3 — Convert to a Library Module

**3.1 Change the Gradle module type**

```groovy
// Before
apply plugin: 'com.android.application'

// After
apply plugin: 'com.android.library'
```

This is the structural change that makes everything else in Part Two possible.

**3.2 Remove the standalone entry point**

`TermuxActivity` is the app launcher. It has no place in a library. Either
delete it or reduce it to a stub. The host IDE provides the Activity. Termux
provides the runtime.

**3.3 Expose an embed API**

Create the surface your IDE actually calls:

```java
// Initialize the runtime (call once, on app start)
InternalTermux.init(context, config);

// Open a terminal session and get back a View
TerminalEmulatorView view = InternalTermux.createSession(sessionId);

// Attach that View wherever your IDE layout needs it
```

Internally this wires up `TermuxService` and returns a `TerminalEmulatorView`.
The host app never touches Termux internals directly.

**Deliverable:** `internal-termux` builds as an `.aar`. A test Activity in your
IDE imports it, calls the embed API, and gets a working terminal session. No
Termux launcher involved.

---

### Phase 4 — Controlled Bootstrap

Termux normally fetches its bootstrap from the network on first run. You want
that under your control.

**4.1 Define your supported toolchain**

Create `supported-packages.txt` in the repo root. This is the list of Termux
packages the library officially supports. Keep it small. A shell, coreutils,
and whatever your IDE actually needs. Everything outside this list is not your
problem.

```
bash
coreutils
git
openssh
# add only what your IDE actually uses
```

**4.2 Bundle a minimal bootstrap tarball**

Build a bootstrap from your supported package list and commit it as a library
asset. On first `init()`, the library extracts it silently. No network fetch.
No first-run download dialog. The runtime is ready within seconds of install.

**4.3 Bootstrap is handled by the library**

The host app calls `InternalTermux.init()`. By the time that call returns, the
bootstrap is extracted and the runtime is ready. The host app does not think
about this.

**Deliverable:** Cold install works offline. The terminal is usable immediately
after the first `init()` call.

---

### Phase 5 — proot as an Optional Plugin

proot-distro is not a core dependency. It is a secondary execution path for
cases where you need tools that only exist in a glibc environment. Treat it
that way.

**5.1 Separate Gradle module**

```
internal-termux/          ← core library (no proot knowledge)
internal-termux-proot/    ← optional plugin (pulls in proot-distro)
```

The host IDE only includes `internal-termux-proot` if it actually wants proot.
The core library has zero dependency on it.

**5.2 Session abstraction**

The terminal emulator does not know whether it is talking to a native Bionic
shell or a proot environment. Both implement the same session interface:

```java
// Native session
TerminalSession s = InternalTermux.createSession(sessionId);

// proot session — same interface, different runtime
TerminalSession s = InternalTermux.proot().launch("debian", sessionId);
```

**5.3 Hard environment boundary**

Native sessions and proot sessions never share environment variables unless you
explicitly pass them. No implicit bleed-through. The boundary between Bionic
and glibc stays visible.

**5.4 The 90% rule**

If you find yourself routing most operations through proot, something has gone
wrong with scope. proot handles the edge cases. The native Bionic runtime
handles everything else.

**Deliverable:** The host IDE can open a native session and a proot session in
separate terminal tabs through the same API. The core library has no knowledge
of proot.

---

### Phase 6 — Merge Protection

This is what makes the fork survivable long-term. The question is not whether
upstream Termux will change things you have modified — it will. The question is
whether you find out immediately or six months later.

**6.1 Upstream tracking branch**

```
upstream/main   ← clean mirror of Termux, never commit here
internal/main   ← your work lives here
```

Periodically pull upstream changes into `upstream/main`. Never directly into
`internal/main`.

**6.2 `merge-check` script**

Before merging any upstream changes, run a diff against your tagged files:

```bash
#!/usr/bin/env bash
# merge-check: compare upstream/main against internal/main
# for all files tagged INTERNAL-TERMUX MODIFIED

MODIFIED=$(grep -rl "INTERNAL-TERMUX MODIFIED" .)

for file in $MODIFIED; do
    if git diff upstream/main internal/main -- "$file" | grep -q .; then
        echo "REVIEW   $file"
    fi
done

echo "--- unlabeled upstream changes ---"
git diff upstream/main internal/main \
    --name-only | grep -v -f <(echo "$MODIFIED")
```

Output is one of three states:

| State | Meaning |
|---|---|
| `SAFE` | Upstream changed files you have not touched |
| `REVIEW` | Upstream changed a file you modified — read before merging |
| `CONFLICT` | Upstream changed a protected region you depend on |

Most upstream pulls will be entirely `SAFE`. `REVIEW` items need a human
decision. `CONFLICT` items are rare and are handled case by case.

**6.3 The maintenance reality**

The parts of Termux that change frequently are the package ecosystem and
platform compatibility patches. The parts you have modified — path resolution,
the embed API, the session abstraction — change infrequently. In practice,
most upstream merges will require no action from you at all.

**Deliverable:** You can pull several months of upstream commits and know
within a minute which ones actually require your attention.

---

## Scope Boundaries

Write these into `CONTRIBUTING.md` on day one. They are not limitations — they
are what keeps the project maintainable.

1. `internal-termux` supports only the packages in `supported-packages.txt`
2. proot is a plugin, never a dependency of core
3. `$PREFIX` is always derived at runtime, never hardcoded
4. Every divergence from upstream is tagged — no silent modifications
5. The library exposes no UI beyond `TerminalEmulatorView` — layout is the host
   app's responsibility
6. If you find yourself extending the supported package list significantly,
   stop and ask whether you are drifting toward full Termux

---

## What This Is Not

- Not a full Termux mirror
- Not a Debian environment
- Not a general-purpose terminal app

It is a vendored, embeddable terminal runtime with a controlled scope. The
maintenance burden is proportional to how well you hold that line.

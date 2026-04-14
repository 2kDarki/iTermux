# Lessons

## 2026-04-14

- For this repo, do not scaffold `main/` before Phase 0 is complete. Finish the upstream audit first, because the audit findings must shape the Android library skeleton instead of forcing later rework.
- Do not spin up early review/reviewer subagents for planning docs in this repo. Move inline and reserve extra agents for work that materially reduces execution time or context load.
- Do not stop at phase boundaries unless there is a concrete blocker that truly requires user intervention. Finish the approved slice end-to-end, then report the result and the next trigger.
- Prefer the project-facing name `iTermux` for the internal runtime surface. The shorter name is the intended identity and should be used consistently in new code.
- Follow the package convention `com.darkian.<product>` for Android namespaces and source packages. For this repo, use `com.darkian.itermux`.
- Do not end the turn after a successful build or test run unless the user explicitly asked for a status update. Green verification is permission to continue, not a reporting boundary.

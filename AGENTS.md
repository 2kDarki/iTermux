# Agent Rules

## Continuation

- Treat the roadmap as the task. Do not stop at green tests, successful builds, clean git state, completed slices, or commits.
- Continue phase-by-phase unless a real blocker requires user intervention, there is meaningful risk in proceeding without a decision, or the user explicitly asks for a status stop.

## Execution

- Never spawn subagents for work in this repo. Do all planning, implementation, verification, and review inline in the main agent thread.

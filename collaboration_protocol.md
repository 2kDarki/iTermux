Collaboration protocol for this thread

1. You explain the issue (or what you did) in detail, and paste the file contents (meaning no summaries with links)
2. Do not paste back contents of todo.md, lessons.md, and collaboration_protocol.md unless necessary
3. Do not paste full contents of a file unless necessary. Only paste the relevant paths, such as the parts being discussed and/or parts that have been edited/added
4. I analyze and come up with potential fixes
5. You correct, and we brainstorm
6. We agree, and implement
7. Repeat
8. Do not stop just because tests passed, builds passed, the tree is clean, or a commit was created. Those are continuation checkpoints unless I explicitly ask for a status stop.

NB: This approach will undoubtably result in creating docs. So one rule to keep in mind for this: delete all stale and redundant docs (especially the ones that are just plans for already completed phases)

**IMPORTANT**: Never run heavy commands in parallel. Examples of the commands:
  - flutter test
  - flutter analyze
  - flutter build
  - compiling
These will starve the machine and most will just timeout.

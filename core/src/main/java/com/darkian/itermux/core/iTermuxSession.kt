package com.darkian.itermux.core
// INTERNAL-TERMUX MODIFIED - merge carefully

/**
 * Host-facing session metadata for the embeddable runtime.
 */
data class iTermuxSession(
    val id: String,
    val kind: iTermuxSessionKind,
    val mode: iTermuxSessionMode,
    val shellSpec: iTermuxShellSpec,
)

enum class iTermuxSessionKind {
    NATIVE,
}

enum class iTermuxSessionMode {
    LOGIN_SHELL,
    COMMAND,
    FILE_COMMAND,
}

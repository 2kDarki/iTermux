package com.darkian.itermux.core
// INTERNAL-TERMUX MODIFIED - merge carefully

/**
 * Host-facing session metadata for the embeddable runtime.
 */
data class iTermuxSession(
    val id: String,
    val backend: iTermuxSessionBackend,
    val mode: iTermuxSessionMode,
    val shellSpec: iTermuxShellSpec,
)

data class iTermuxSessionBackend(
    val id: String,
)

object iTermuxSessionBackends {
    val NATIVE: iTermuxSessionBackend = iTermuxSessionBackend(id = "native")
}

enum class iTermuxSessionMode {
    LOGIN_SHELL,
    COMMAND,
    FILE_COMMAND,
}

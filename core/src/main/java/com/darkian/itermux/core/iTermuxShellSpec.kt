package com.darkian.itermux.core
// INTERNAL-TERMUX MODIFIED - merge carefully

/**
 * Launch description for a shell process inside the host-owned runtime.
 */
data class iTermuxShellSpec(
    val executable: String,
    val arguments: List<String>,
    val workingDirectory: String,
    val environment: Map<String, String>,
)


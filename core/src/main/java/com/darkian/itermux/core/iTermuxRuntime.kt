package com.darkian.itermux.core

// INTERNAL-TERMUX MODIFIED - merge carefully

/**
 * Fully initialized host-owned runtime state.
 */
data class iTermuxRuntime(
    val identity: iTermuxIdentity,
    val paths: iTermuxPaths,
    val environment: Map<String, String>,
    val properties: Map<String, String>,
    val selectedPropertiesFile: String?,
    val defaultWorkingDirectory: String,
    val isBootstrapRequired: Boolean,
)

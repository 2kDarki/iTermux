package com.darkian.itermux.core

/**
 * Fully initialized host-owned runtime state.
 */
data class iTermuxRuntime(
    val paths: iTermuxPaths,
    val environment: Map<String, String>,
)

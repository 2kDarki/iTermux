package com.darkian.itermux.core

// INTERNAL-TERMUX MODIFIED - merge carefully

/**
 * Bootstrap lifecycle states surfaced by the host-owned runtime.
 */
enum class iTermuxBootstrapState {
    UNINITIALIZED,
    EXTRACTING,
    PARTIAL,
    RECOVERING,
    VERIFYING,
    CORRUPTED,
    READY,
    FAILED,
    DEGRADED,
}

/**
 * Named runtime failure causes that can cross the library boundary.
 */
enum class iTermuxRuntimeFailureCause {
    UNSUPPORTED_ABI,
    BOOTSTRAP_EXTRACTION_FAILED,
    BOOTSTRAP_CORRUPTED,
    BOOTSTRAP_PARTIAL,
    SESSION_START_FAILED,
    SESSION_KILLED_UNRECOVERABLE,
    ENVIRONMENT_DEGRADED,
    PROOT_UNAVAILABLE,
}

/**
 * Named causes for a degraded runtime after bootstrap extraction has completed.
 */
enum class iTermuxDegradedCause {
    MISSING_BINARY,
    PERMISSION_CHANGED,
    CORRUPTED_INSTALL,
    ABI_MISMATCH,
    SANDBOX_INVALIDATED,
}

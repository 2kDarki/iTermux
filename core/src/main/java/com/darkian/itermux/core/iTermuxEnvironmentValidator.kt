package com.darkian.itermux.core

// INTERNAL-TERMUX MODIFIED - merge carefully

import java.nio.file.Files
import java.nio.file.Path

/**
 * Fast-path integrity validation for an already-extracted runtime.
 */
object iTermuxEnvironmentValidator {
    fun validate(
        paths: iTermuxPaths,
        environment: Map<String, String>,
        supportedAbis: List<String> = emptyList(),
        bootstrapVariantAbi: String? = null,
        fileAccess: iTermuxEnvironmentFileAccess = iTermuxEnvironmentFileAccess(),
    ): iTermuxEnvironmentValidation {
        if (environment["PREFIX"] != paths.prefixDir) {
            return iTermuxEnvironmentValidation(
                result = iTermuxEnvironmentValidationResult.DEGRADED,
                degradedCause = iTermuxDegradedCause.CORRUPTED_INSTALL,
            )
        }

        if (bootstrapVariantAbi != null &&
            supportedAbis.isNotEmpty() &&
            bootstrapVariantAbi !in supportedAbis
        ) {
            return iTermuxEnvironmentValidation(
                result = iTermuxEnvironmentValidationResult.DEGRADED,
                degradedCause = iTermuxDegradedCause.ABI_MISMATCH,
            )
        }

        val filesDir = Path.of(paths.filesDir)
        val prefixDir = Path.of(paths.prefixDir)
        val homeDir = Path.of(paths.homeDir)
        if (!fileAccess.isWritable(filesDir) || !fileAccess.isWritable(prefixDir) || !fileAccess.isWritable(homeDir)) {
            return iTermuxEnvironmentValidation(
                result = iTermuxEnvironmentValidationResult.DEGRADED,
                degradedCause = iTermuxDegradedCause.SANDBOX_INVALIDATED,
            )
        }

        val binDir = Path.of(paths.binDir)
        val etcDir = Path.of(paths.etcDir)
        if (!fileAccess.isDirectory(binDir) || !fileAccess.isDirectory(etcDir)) {
            return iTermuxEnvironmentValidation(
                result = iTermuxEnvironmentValidationResult.DEGRADED,
                degradedCause = iTermuxDegradedCause.CORRUPTED_INSTALL,
            )
        }

        val requiredBinaries = listOf(
            binDir.resolve("sh"),
            binDir.resolve("env"),
        )
        if (requiredBinaries.any { binary -> !fileAccess.isRegularFile(binary) }) {
            return iTermuxEnvironmentValidation(
                result = iTermuxEnvironmentValidationResult.DEGRADED,
                degradedCause = iTermuxDegradedCause.MISSING_BINARY,
            )
        }

        if (requiredBinaries.any { binary -> !fileAccess.isExecutable(binary) }) {
            return iTermuxEnvironmentValidation(
                result = iTermuxEnvironmentValidationResult.DEGRADED,
                degradedCause = iTermuxDegradedCause.PERMISSION_CHANGED,
            )
        }

        val profileFile = etcDir.resolve("profile")
        if (!fileAccess.isRegularFile(profileFile)) {
            return iTermuxEnvironmentValidation(
                result = iTermuxEnvironmentValidationResult.DEGRADED,
                degradedCause = iTermuxDegradedCause.CORRUPTED_INSTALL,
            )
        }

        return iTermuxEnvironmentValidation(
            result = iTermuxEnvironmentValidationResult.VALID,
        )
    }

    internal fun isExecutable(path: Path): Boolean {
        val osName = System.getProperty("os.name").orEmpty()
        return if (osName.startsWith("Windows", ignoreCase = true)) {
            Files.exists(path)
        } else {
            Files.isExecutable(path)
        }
    }
}

data class iTermuxEnvironmentValidation(
    val result: iTermuxEnvironmentValidationResult,
    val degradedCause: iTermuxDegradedCause? = null,
)

enum class iTermuxEnvironmentValidationResult {
    VALID,
    DEGRADED,
}

data class iTermuxEnvironmentFileAccess(
    val isDirectory: (Path) -> Boolean = { path -> Files.isDirectory(path) },
    val isRegularFile: (Path) -> Boolean = { path -> Files.isRegularFile(path) },
    val isWritable: (Path) -> Boolean = { path -> Files.isWritable(path) },
    val isExecutable: (Path) -> Boolean = { path -> iTermuxEnvironmentValidator.isExecutable(path) },
)

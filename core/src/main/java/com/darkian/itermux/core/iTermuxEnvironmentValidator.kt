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
    ): iTermuxEnvironmentValidation {
        if (environment["PREFIX"] != paths.prefixDir) {
            return iTermuxEnvironmentValidation(
                result = iTermuxEnvironmentValidationResult.DEGRADED,
                degradedCause = iTermuxDegradedCause.CORRUPTED_INSTALL,
            )
        }

        val binDir = Path.of(paths.binDir)
        val etcDir = Path.of(paths.etcDir)
        if (!Files.isDirectory(binDir) || !Files.isDirectory(etcDir)) {
            return iTermuxEnvironmentValidation(
                result = iTermuxEnvironmentValidationResult.DEGRADED,
                degradedCause = iTermuxDegradedCause.CORRUPTED_INSTALL,
            )
        }

        val shellBinary = binDir.resolve("sh")
        if (!Files.isRegularFile(shellBinary)) {
            return iTermuxEnvironmentValidation(
                result = iTermuxEnvironmentValidationResult.DEGRADED,
                degradedCause = iTermuxDegradedCause.MISSING_BINARY,
            )
        }

        if (!isExecutable(shellBinary)) {
            return iTermuxEnvironmentValidation(
                result = iTermuxEnvironmentValidationResult.DEGRADED,
                degradedCause = iTermuxDegradedCause.PERMISSION_CHANGED,
            )
        }

        return iTermuxEnvironmentValidation(
            result = iTermuxEnvironmentValidationResult.VALID,
        )
    }

    private fun isExecutable(path: Path): Boolean {
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

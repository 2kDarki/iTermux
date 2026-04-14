package com.darkian.itermux.core
// INTERNAL-TERMUX MODIFIED - merge carefully

import java.nio.file.Files
import java.nio.file.Path

/**
 * Detects whether the host-owned prefix still needs bootstrap content.
 */
object iTermuxPrefixState {
    fun isBootstrapRequired(paths: iTermuxPaths): Boolean {
        val prefixPath = Path.of(paths.prefixDir)
        if (!Files.isDirectory(prefixPath)) {
            return true
        }

        Files.walk(prefixPath).use { stream ->
            return stream
                .filter { candidate -> candidate != prefixPath }
                .noneMatch { candidate ->
                    !Files.isDirectory(candidate) && !isIgnoredBootstrapPlaceholder(candidate, paths)
                }
        }
    }

    private fun isIgnoredBootstrapPlaceholder(
        candidate: Path,
        paths: iTermuxPaths,
    ): Boolean {
        val tmpDir = Path.of(paths.tmpDir)
        val envFile = Path.of(paths.envFile)
        val envTempFile = Path.of(paths.envTempFile)

        return candidate == envFile ||
            candidate == envTempFile ||
            candidate.startsWith(tmpDir)
    }
}

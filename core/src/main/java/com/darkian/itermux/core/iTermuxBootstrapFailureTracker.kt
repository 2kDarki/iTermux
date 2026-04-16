package com.darkian.itermux.core

// INTERNAL-TERMUX MODIFIED - merge carefully

import java.nio.file.Files
import java.nio.file.Path
import java.nio.charset.StandardCharsets

/**
 * Tracks bootstrap failures across initialization attempts so retry policy can
 * distinguish transient failures from persistent failure loops.
 */
interface iTermuxBootstrapFailureTracker {
    fun lastFailureAtMillis(paths: iTermuxPaths): Long?

    fun recordFailure(
        paths: iTermuxPaths,
        failedAtMillis: Long,
    )

    fun clearFailure(paths: iTermuxPaths)
}

object iTermuxBootstrapFileFailureTracker : iTermuxBootstrapFailureTracker {
    override fun lastFailureAtMillis(paths: iTermuxPaths): Long? {
        val markerPath = markerPath(paths)
        if (!Files.exists(markerPath)) {
            return null
        }

        return Files.readAllBytes(markerPath)
            .toString(StandardCharsets.UTF_8)
            .trim()
            .toLongOrNull()
    }

    override fun recordFailure(
        paths: iTermuxPaths,
        failedAtMillis: Long,
    ) {
        val markerPath = markerPath(paths)
        markerPath.parent?.let { Files.createDirectories(it) }
        Files.write(markerPath, failedAtMillis.toString().toByteArray(StandardCharsets.UTF_8))
    }

    override fun clearFailure(paths: iTermuxPaths) {
        Files.deleteIfExists(markerPath(paths))
    }

    private fun markerPath(paths: iTermuxPaths): Path {
        return Path.of(paths.filesDir, ".itermux-bootstrap-last-failure")
    }
}

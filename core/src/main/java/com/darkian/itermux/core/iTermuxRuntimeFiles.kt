package com.darkian.itermux.core
// INTERNAL-TERMUX MODIFIED - merge carefully

import java.nio.file.AtomicMoveNotSupportedException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption

/**
 * Materializes the derived runtime layout on disk for the host-owned runtime.
 */
object iTermuxRuntimeFiles {
    fun ensureLayout(paths: iTermuxPaths) {
        directoriesToEnsure(paths).forEach { directory ->
            Files.createDirectories(Path.of(directory))
        }
    }

    fun writeEnvironmentFile(
        paths: iTermuxPaths,
        environment: Map<String, String>,
    ) {
        ensureLayout(paths)

        val tempFile = Path.of(paths.envTempFile)
        val finalFile = Path.of(paths.envFile)
        val dotEnv = iTermuxEnvironment.toDotEnvFile(environment)

        Files.write(tempFile, dotEnv.toByteArray())
        try {
            Files.move(
                tempFile,
                finalFile,
                StandardCopyOption.REPLACE_EXISTING,
                StandardCopyOption.ATOMIC_MOVE,
            )
        } catch (_: AtomicMoveNotSupportedException) {
            Files.move(
                tempFile,
                finalFile,
                StandardCopyOption.REPLACE_EXISTING,
            )
        }
    }

    private fun directoriesToEnsure(paths: iTermuxPaths): List<String> {
        return listOf(
            paths.filesDir,
            paths.prefixDir,
            paths.tmpDir,
            paths.varDir,
            paths.homeDir,
            paths.configHomeDir,
            paths.dataHomeDir,
            paths.storageHomeDir,
            paths.stagingPrefixDir,
            paths.appsDir,
            paths.configPrefixDir,
        )
    }
}

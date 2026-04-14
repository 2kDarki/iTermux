package com.darkian.itermux.core

import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.File
import java.nio.file.Files

class iTermuxWorkingDirectoryTest {
    @Test
    fun usesConfiguredDefaultWorkingDirectoryWhenReadable() {
        val filesDir = Files.createTempDirectory("itermux-workdir").toFile().absolutePath
        val runtime = iTermuxRuntimeInitializer.initialize(
            filesDir = filesDir,
            hostPackageName = "com.darkian.host",
        )
        val configuredDirectory = File(runtime.paths.homeDir, "projects").apply { mkdirs() }

        val workingDirectory = iTermuxWorkingDirectory.resolve(
            paths = runtime.paths,
            properties = mapOf(
                "default-working-directory" to configuredDirectory.absolutePath,
            ),
        )

        assertEquals(configuredDirectory.absolutePath, workingDirectory)
    }

    @Test
    fun fallsBackToHomeDirectoryWhenConfiguredWorkingDirectoryIsMissing() {
        val filesDir = Files.createTempDirectory("itermux-workdir").toFile().absolutePath
        val runtime = iTermuxRuntimeInitializer.initialize(
            filesDir = filesDir,
            hostPackageName = "com.darkian.host",
        )

        val workingDirectory = iTermuxWorkingDirectory.resolve(
            paths = runtime.paths,
            properties = mapOf(
                "default-working-directory" to "${runtime.paths.homeDir}/missing",
            ),
        )

        assertEquals(runtime.paths.homeDir, workingDirectory)
    }
}

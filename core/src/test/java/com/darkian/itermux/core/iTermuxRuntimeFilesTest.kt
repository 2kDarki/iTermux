package com.darkian.itermux.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.nio.file.Files

class iTermuxRuntimeFilesTest {
    @Test
    fun ensuresRuntimeDirectoryLayoutExists() {
        val filesDir = Files.createTempDirectory("itermux-layout").toFile().absolutePath
        val paths = iTermuxPathResolver.resolve(
            filesDir = filesDir,
            hostPackageName = "com.darkian.host",
        )

        iTermuxRuntimeFiles.ensureLayout(paths)

        assertTrue(File(paths.prefixDir).isDirectory)
        assertTrue(File(paths.homeDir).isDirectory)
        assertTrue(File(paths.tmpDir).isDirectory)
        assertTrue(File(paths.stagingPrefixDir).isDirectory)
        assertTrue(File(paths.appsDir).isDirectory)
        assertTrue(File(paths.configHomeDir).isDirectory)
        assertTrue(File(paths.dataHomeDir).isDirectory)
        assertTrue(File(paths.storageHomeDir).isDirectory)
        assertTrue(File(paths.configPrefixDir).isDirectory)
    }

    @Test
    fun writesEnvironmentFileAtomically() {
        val filesDir = Files.createTempDirectory("itermux-env").toFile().absolutePath
        val paths = iTermuxPathResolver.resolve(
            filesDir = filesDir,
            hostPackageName = "com.darkian.host",
        )
        val environment = linkedMapOf(
            "HOME" to paths.homeDir,
            "PREFIX" to paths.prefixDir,
            "PATH" to paths.binDir,
        )

        iTermuxRuntimeFiles.ensureLayout(paths)
        File(paths.envFile).writeText("old content")

        iTermuxRuntimeFiles.writeEnvironmentFile(paths, environment)

        assertEquals(
            iTermuxEnvironment.toDotEnvFile(environment),
            File(paths.envFile).readText(),
        )
        assertFalse(File(paths.envTempFile).exists())
    }
}

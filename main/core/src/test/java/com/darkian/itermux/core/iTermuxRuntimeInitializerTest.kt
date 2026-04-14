package com.darkian.itermux.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.nio.file.Files

class iTermuxRuntimeInitializerTest {
    @Test
    fun initializesRuntimeLayoutAndEnvironmentFile() {
        val filesDir = Files.createTempDirectory("itermux-runtime").toFile().absolutePath

        val runtime = iTermuxRuntimeInitializer.initialize(
            filesDir = filesDir,
            hostPackageName = "com.darkian.host",
        )

        assertEquals("$filesDir/usr", runtime.paths.prefixDir)
        assertEquals("$filesDir/home", runtime.paths.homeDir)
        assertEquals("$filesDir/usr/bin", runtime.environment["PATH"])
        assertTrue(File(runtime.paths.prefixDir).isDirectory())
        assertTrue(File(runtime.paths.homeDir).isDirectory())
        assertEquals(
            iTermuxEnvironment.toDotEnvFile(runtime.environment),
            File(runtime.paths.envFile).readText(),
        )
    }

    @Test
    fun appliesBaseEnvExtraEnvAndFailSafeDuringInitialization() {
        val filesDir = Files.createTempDirectory("itermux-runtime").toFile().absolutePath

        val runtime = iTermuxRuntimeInitializer.initialize(
            filesDir = filesDir,
            hostPackageName = "com.darkian.host",
            baseEnv = mapOf(
                "PATH" to "/system/bin",
                "TMPDIR" to "/tmp",
            ),
            extraEnv = mapOf(
                "LANG" to "en_US.UTF-8",
            ),
            failSafe = true,
        )

        assertEquals("/system/bin", runtime.environment["PATH"])
        assertEquals("/tmp", runtime.environment["TMPDIR"])
        assertEquals("en_US.UTF-8", runtime.environment["LANG"])
        assertEquals("$filesDir/usr", runtime.environment["PREFIX"])
    }
}

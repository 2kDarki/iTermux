package com.darkian.itermux.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.assertNull
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
            supportedPackages = listOf("bash", "coreutils"),
        )

        assertEquals("$filesDir/usr", runtime.paths.prefixDir)
        assertEquals("$filesDir/home", runtime.paths.homeDir)
        assertEquals("com.darkian.host", runtime.identity.packageName)
        assertEquals("com.darkian.host.files", runtime.identity.filesAuthority)
        assertEquals("com.darkian.host.service_execute", runtime.identity.serviceExecuteAction)
        assertEquals("com.darkian.host.widget", runtime.identity.pluginPackageNames.widget)
        assertEquals(listOf("bash", "coreutils"), runtime.supportedPackages)
        assertEquals("$filesDir/usr/bin", runtime.environment["PATH"])
        assertEquals(runtime.paths.homeDir, runtime.defaultWorkingDirectory)
        assertEquals(iTermuxBootstrapState.UNINITIALIZED, runtime.bootstrapState)
        assertNull(runtime.failureCause)
        assertNull(runtime.selectedPropertiesFile)
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

    @Test
    fun surfacesSelectedPropertiesFileAndResolvedWorkingDirectory() {
        val filesDir = Files.createTempDirectory("itermux-runtime").toFile().absolutePath
        val homeDir = "$filesDir/home"
        val configuredDirectory = File(homeDir, "workspace").apply { mkdirs() }
        File("$homeDir/.config/termux").apply { mkdirs() }
        File("$homeDir/.config/termux/termux.properties").writeText(
            "default-working-directory=${configuredDirectory.absolutePath.replace('\\', '/')}\n",
        )

        val runtime = iTermuxRuntimeInitializer.initialize(
            filesDir = filesDir,
            hostPackageName = "com.darkian.host",
        )

        assertEquals(
            File("$homeDir/.config/termux/termux.properties").absolutePath,
            runtime.selectedPropertiesFile,
        )
        assertEquals(configuredDirectory.absolutePath, runtime.defaultWorkingDirectory)
        assertEquals(iTermuxBootstrapState.UNINITIALIZED, runtime.bootstrapState)
    }
}

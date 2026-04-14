package com.darkian.itermux.core

import org.junit.Assert.assertEquals
import org.junit.Test

class iTermuxEnvironmentTest {
    private val paths = iTermuxPaths(
        filesDir = "/app/files",
        prefixDir = "/app/files/usr",
        binDir = "/app/files/usr/bin",
        etcDir = "/app/files/usr/etc",
        tmpDir = "/app/files/usr/tmp",
        varDir = "/app/files/usr/var",
        homeDir = "/app/files/home",
        configHomeDir = "/app/files/home/.config/termux",
        dataHomeDir = "/app/files/home/.termux",
        storageHomeDir = "/app/files/home/storage",
        stagingPrefixDir = "/app/files/usr-staging",
        appsDir = "/app/files/apps",
        configPrefixDir = "/app/files/usr/etc/termux",
        propertiesPrimaryFile = "/app/files/home/.termux/termux.properties",
        propertiesSecondaryFile = "/app/files/home/.config/termux/termux.properties",
        envFile = "/app/files/usr/etc/termux/termux.env",
        envTempFile = "/app/files/usr/etc/termux/termux.env.tmp",
        termuxAmSocketFile = "/app/files/apps/com.darkian.itermux/termux-am/am.sock",
    )

    @Test
    fun buildsBaselineShellEnvironmentFromPaths() {
        val environment = iTermuxEnvironment.baseline(paths)

        assertEquals("/app/files/home", environment["HOME"])
        assertEquals("/app/files/usr", environment["PREFIX"])
        assertEquals("/app/files/usr/tmp", environment["TMPDIR"])
        assertEquals("/app/files/usr/bin", environment["PATH"])
    }

    @Test
    fun letsExtraEnvironmentOverrideDefaults() {
        val paths = iTermuxPathResolver.resolve(
            filesDir = "/app/files",
            hostPackageName = "com.darkian.itermux",
        )

        val environment = iTermuxEnvironment.baseline(
            paths = paths,
            extraEnv = mapOf(
                "PATH" to "/custom/bin",
                "LANG" to "en_US.UTF-8",
            ),
        )

        assertEquals("/custom/bin", environment["PATH"])
        assertEquals("en_US.UTF-8", environment["LANG"])
        assertEquals("/app/files/usr", environment["PREFIX"])
    }

    @Test
    fun buildsNonFailSafeEnvironmentFromBaseEnv() {
        val environment = iTermuxEnvironment.build(
            paths = paths,
            baseEnv = linkedMapOf(
                "PATH" to "/system/bin",
                "TMPDIR" to "/tmp",
                "LD_LIBRARY_PATH" to "/vendor/lib",
                "ANDROID_ROOT" to "/system",
            ),
            failSafe = false,
        )

        assertEquals("/app/files/home", environment["HOME"])
        assertEquals("/app/files/usr", environment["PREFIX"])
        assertEquals("/app/files/usr/tmp", environment["TMPDIR"])
        assertEquals("/app/files/usr/bin", environment["PATH"])
        assertEquals("/system", environment["ANDROID_ROOT"])
        assertEquals(null, environment["LD_LIBRARY_PATH"])
    }

    @Test
    fun keepsBasePathAndTmpDirInFailSafeMode() {
        val environment = iTermuxEnvironment.build(
            paths = paths,
            baseEnv = linkedMapOf(
                "PATH" to "/system/bin",
                "TMPDIR" to "/tmp",
                "LD_LIBRARY_PATH" to "/vendor/lib",
            ),
            failSafe = true,
        )

        assertEquals("/app/files/home", environment["HOME"])
        assertEquals("/app/files/usr", environment["PREFIX"])
        assertEquals("/system/bin", environment["PATH"])
        assertEquals("/tmp", environment["TMPDIR"])
        assertEquals("/vendor/lib", environment["LD_LIBRARY_PATH"])
    }

    @Test
    fun rendersSortedEscapedDotEnvContent() {
        val dotEnv = iTermuxEnvironment.toDotEnvFile(
            linkedMapOf(
                "Z_VAR" to "z",
                "A_VAR" to "quote\" dollar$ backtick` slash\\",
                "BAD-NAME" to "ignored",
            ),
        )

        assertEquals(
            "export A_VAR=\"quote\\\" dollar\\$ backtick\\` slash\\\\\"\n" +
                "export Z_VAR=\"z\"\n",
            dotEnv,
        )
    }

    @Test
    fun returnsDefaultWorkingDirectoryAndBinPathFromResolvedPaths() {
        assertEquals(paths.homeDir, iTermuxEnvironment.defaultWorkingDirectory(paths))
        assertEquals(paths.binDir, iTermuxEnvironment.defaultBinPath(paths))
    }
}

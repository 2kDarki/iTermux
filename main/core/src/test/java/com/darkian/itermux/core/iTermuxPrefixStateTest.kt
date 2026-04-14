package com.darkian.itermux.core

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.nio.file.Files

class iTermuxPrefixStateTest {
    @Test
    fun treatsMissingPrefixDirectoryAsBootstrapRequired() {
        val paths = iTermuxPathResolver.resolve(
            filesDir = Files.createTempDirectory("itermux-prefix").toFile().absolutePath,
            hostPackageName = "com.darkian.host",
        )

        assertTrue(iTermuxPrefixState.isBootstrapRequired(paths))
    }

    @Test
    fun ignoresTmpAndEnvFilesWhenCheckingBootstrapRequirement() {
        val runtime = iTermuxRuntimeInitializer.initialize(
            filesDir = Files.createTempDirectory("itermux-prefix").toFile().absolutePath,
            hostPackageName = "com.darkian.host",
        )

        File(runtime.paths.tmpDir).mkdirs()
        File(runtime.paths.envFile).writeText("export PREFIX=\"${runtime.paths.prefixDir}\"\n")

        assertTrue(iTermuxPrefixState.isBootstrapRequired(runtime.paths))
    }

    @Test
    fun treatsRealPrefixContentAsInstalledRuntime() {
        val runtime = iTermuxRuntimeInitializer.initialize(
            filesDir = Files.createTempDirectory("itermux-prefix").toFile().absolutePath,
            hostPackageName = "com.darkian.host",
        )
        File(runtime.paths.binDir).mkdirs()
        File(runtime.paths.binDir, "bash").writeText("binary")

        assertFalse(iTermuxPrefixState.isBootstrapRequired(runtime.paths))
    }

    @Test
    fun exposesBootstrapRequirementOnInitializedRuntime() {
        val runtime = iTermuxRuntimeInitializer.initialize(
            filesDir = Files.createTempDirectory("itermux-prefix").toFile().absolutePath,
            hostPackageName = "com.darkian.host",
        )

        assertTrue(runtime.isBootstrapRequired)
    }
}

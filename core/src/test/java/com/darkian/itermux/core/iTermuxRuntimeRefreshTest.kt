package com.darkian.itermux.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test
import java.io.File
import java.nio.file.Files

class iTermuxRuntimeRefreshTest {
    @Test
    fun refreshesPropertiesBackedRuntimeMetadata() {
        val runtime = iTermuxRuntimeInitializer.initialize(
            filesDir = Files.createTempDirectory("itermux-refresh").toFile().absolutePath,
            hostPackageName = "com.darkian.host",
        )
        val configuredDirectory = File(runtime.paths.homeDir, "workspace").apply { mkdirs() }
        File(runtime.paths.propertiesSecondaryFile).apply {
            parentFile?.mkdirs()
            writeText("default-working-directory=${configuredDirectory.absolutePath.replace('\\', '/')}\n")
        }

        val refreshed = iTermuxRuntimeInitializer.refresh(runtime.paths)

        assertEquals("com.darkian.host", refreshed.identity.packageName)
        assertEquals("com.darkian.host.files", refreshed.identity.filesAuthority)
        assertEquals(
            File(runtime.paths.propertiesSecondaryFile).absolutePath,
            refreshed.selectedPropertiesFile,
        )
        assertEquals(configuredDirectory.absolutePath, refreshed.defaultWorkingDirectory)
    }

    @Test
    fun refreshesBootstrapReadinessAfterPrefixContentAppears() {
        val runtime = iTermuxRuntimeInitializer.initialize(
            filesDir = Files.createTempDirectory("itermux-refresh").toFile().absolutePath,
            hostPackageName = "com.darkian.host",
        )
        File(runtime.paths.binDir).mkdirs()
        File(runtime.paths.binDir, "bash").writeText("binary")

        val refreshed = iTermuxRuntimeInitializer.refresh(runtime.paths)

        assertEquals("com.darkian.host", refreshed.identity.packageName)
        assertFalse(refreshed.isBootstrapRequired)
    }
}

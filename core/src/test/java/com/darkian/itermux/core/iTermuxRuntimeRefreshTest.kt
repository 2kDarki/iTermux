package com.darkian.itermux.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Test
import java.io.File
import java.nio.file.Files
import java.nio.file.Path

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
    fun refreshSurfacesDegradedBootstrapStateWhenCoreShellIsMissing() {
        val runtime = iTermuxRuntimeInitializer.initialize(
            filesDir = Files.createTempDirectory("itermux-refresh").toFile().absolutePath,
            hostPackageName = "com.darkian.host",
        )
        File(runtime.paths.binDir).mkdirs()
        File(runtime.paths.binDir, "bash").writeText("binary")
        File(runtime.paths.etcDir).mkdirs()
        File(runtime.paths.etcDir, "profile").writeText("export TERM=xterm-256color\n")

        val refreshed = iTermuxRuntimeInitializer.refresh(runtime.paths)

        assertEquals("com.darkian.host", refreshed.identity.packageName)
        assertFalse(refreshed.isBootstrapRequired)
        assertEquals(iTermuxBootstrapState.DEGRADED, refreshed.bootstrapState)
        assertEquals(iTermuxRuntimeFailureCause.ENVIRONMENT_DEGRADED, refreshed.failureCause)
        assertEquals(iTermuxDegradedCause.MISSING_BINARY, refreshed.degradedCause)
    }

    @Test
    fun refreshSurfacesReadyBootstrapStateWhenExpectedStructureExists() {
        val runtime = iTermuxRuntimeInitializer.initialize(
            filesDir = Files.createTempDirectory("itermux-refresh-ready").toFile().absolutePath,
            hostPackageName = "com.darkian.host",
        )
        File(runtime.paths.binDir).mkdirs()
        File(runtime.paths.binDir, "sh").apply {
            writeText("#!/bin/sh\nexit 0\n")
            setExecutable(true, false)
        }
        File(runtime.paths.binDir, "env").apply {
            writeText("#!/bin/sh\nenv\n")
            setExecutable(true, false)
        }
        File(runtime.paths.etcDir).mkdirs()
        File(runtime.paths.etcDir, "profile").writeText("export TERM=xterm-256color\n")

        val refreshed = iTermuxRuntimeInitializer.refresh(runtime.paths)

        assertFalse(refreshed.isBootstrapRequired)
        assertEquals(iTermuxBootstrapState.READY, refreshed.bootstrapState)
        assertNull(refreshed.failureCause)
        assertNull(refreshed.degradedCause)
    }

    @Test
    fun refreshSurfacesAbiMismatchWhenSelectedVariantDriftsFromSupportedAbis() {
        val runtime = readyRuntime("itermux-refresh-abi-mismatch")

        val refreshed = iTermuxRuntimeInitializer.refresh(
            paths = runtime.paths,
            supportedAbis = listOf("x86_64"),
            bootstrapVariantAbi = "arm64-v8a",
        )

        assertFalse(refreshed.isBootstrapRequired)
        assertEquals(iTermuxBootstrapState.DEGRADED, refreshed.bootstrapState)
        assertEquals(iTermuxRuntimeFailureCause.ENVIRONMENT_DEGRADED, refreshed.failureCause)
        assertEquals(iTermuxDegradedCause.ABI_MISMATCH, refreshed.degradedCause)
    }

    @Test
    fun refreshSurfacesSandboxInvalidatedWhenRuntimeStorageStopsBeingWritable() {
        val runtime = readyRuntime("itermux-refresh-sandbox-invalidated")
        val filesDirPath = Path.of(runtime.paths.filesDir)

        val refreshed = iTermuxRuntimeInitializer.refresh(
            paths = runtime.paths,
            supportedAbis = listOf("arm64-v8a"),
            bootstrapVariantAbi = "arm64-v8a",
            environmentFileAccess = iTermuxEnvironmentFileAccess(
                isWritable = { path -> path != filesDirPath },
            ),
        )

        assertFalse(refreshed.isBootstrapRequired)
        assertEquals(iTermuxBootstrapState.DEGRADED, refreshed.bootstrapState)
        assertEquals(iTermuxRuntimeFailureCause.ENVIRONMENT_DEGRADED, refreshed.failureCause)
        assertEquals(iTermuxDegradedCause.SANDBOX_INVALIDATED, refreshed.degradedCause)
    }

    private fun readyRuntime(prefix: String): iTermuxRuntime {
        val runtime = iTermuxRuntimeInitializer.initialize(
            filesDir = Files.createTempDirectory(prefix).toFile().absolutePath,
            hostPackageName = "com.darkian.host",
        )
        File(runtime.paths.binDir).mkdirs()
        File(runtime.paths.binDir, "sh").apply {
            writeText("#!/bin/sh\nexit 0\n")
            setExecutable(true, false)
        }
        File(runtime.paths.binDir, "env").apply {
            writeText("#!/bin/sh\nenv\n")
            setExecutable(true, false)
        }
        File(runtime.paths.etcDir).mkdirs()
        File(runtime.paths.etcDir, "profile").writeText("export TERM=xterm-256color\n")
        return runtime
    }
}

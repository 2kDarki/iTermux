package com.darkian.itermux.core

import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.File
import java.nio.file.Files
import java.nio.file.Path

class iTermuxEnvironmentValidatorTest {
    @Test
    fun surfacesMissingBinaryWhenCoreUtilityDisappears() {
        val runtime = readyRuntime()
        File(runtime.paths.binDir, "env").delete()

        val validation = iTermuxEnvironmentValidator.validate(
            paths = runtime.paths,
            environment = runtime.environment,
            supportedAbis = listOf("arm64-v8a"),
            bootstrapVariantAbi = "arm64-v8a",
        )

        assertEquals(iTermuxEnvironmentValidationResult.DEGRADED, validation.result)
        assertEquals(iTermuxDegradedCause.MISSING_BINARY, validation.degradedCause)
    }

    @Test
    fun surfacesPermissionChangedWhenRequiredBinaryStopsBeingExecutable() {
        val runtime = readyRuntime()

        val validation = iTermuxEnvironmentValidator.validate(
            paths = runtime.paths,
            environment = runtime.environment,
            supportedAbis = listOf("arm64-v8a"),
            bootstrapVariantAbi = "arm64-v8a",
            fileAccess = iTermuxEnvironmentFileAccess(
                isExecutable = { path ->
                    path.fileName.toString() != "env"
                },
            ),
        )

        assertEquals(iTermuxEnvironmentValidationResult.DEGRADED, validation.result)
        assertEquals(iTermuxDegradedCause.PERMISSION_CHANGED, validation.degradedCause)
    }

    @Test
    fun surfacesCorruptedInstallWhenProfileIsMissing() {
        val runtime = readyRuntime()
        File(runtime.paths.etcDir, "profile").delete()

        val validation = iTermuxEnvironmentValidator.validate(
            paths = runtime.paths,
            environment = runtime.environment,
            supportedAbis = listOf("arm64-v8a"),
            bootstrapVariantAbi = "arm64-v8a",
        )

        assertEquals(iTermuxEnvironmentValidationResult.DEGRADED, validation.result)
        assertEquals(iTermuxDegradedCause.CORRUPTED_INSTALL, validation.degradedCause)
    }

    @Test
    fun surfacesAbiMismatchWhenSelectedBootstrapVariantNoLongerMatchesDeviceAbi() {
        val runtime = readyRuntime()

        val validation = iTermuxEnvironmentValidator.validate(
            paths = runtime.paths,
            environment = runtime.environment,
            supportedAbis = listOf("x86_64"),
            bootstrapVariantAbi = "arm64-v8a",
        )

        assertEquals(iTermuxEnvironmentValidationResult.DEGRADED, validation.result)
        assertEquals(iTermuxDegradedCause.ABI_MISMATCH, validation.degradedCause)
    }

    @Test
    fun surfacesSandboxInvalidatedWhenRuntimeStorageStopsBeingWritable() {
        val runtime = readyRuntime()
        val filesDirPath = Path.of(runtime.paths.filesDir)

        val validation = iTermuxEnvironmentValidator.validate(
            paths = runtime.paths,
            environment = runtime.environment,
            supportedAbis = listOf("arm64-v8a"),
            bootstrapVariantAbi = "arm64-v8a",
            fileAccess = iTermuxEnvironmentFileAccess(
                isWritable = { path -> path != filesDirPath },
            ),
        )

        assertEquals(iTermuxEnvironmentValidationResult.DEGRADED, validation.result)
        assertEquals(iTermuxDegradedCause.SANDBOX_INVALIDATED, validation.degradedCause)
    }

    private fun readyRuntime(): iTermuxRuntime {
        val runtime = iTermuxRuntimeInitializer.initialize(
            filesDir = Files.createTempDirectory("itermux-environment-validator").toFile().absolutePath,
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

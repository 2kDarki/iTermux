package com.darkian.itermux.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.io.File
import java.nio.file.Files

class iTermuxLifecycleListenerTest {
    @Test
    fun autoBootstrapNotifiesBootstrapAndEnvironmentLifecycleCallbacks() {
        val bootstrapStates = mutableListOf<Pair<iTermuxBootstrapState, iTermuxRuntimeFailureCause?>>()
        val validations = mutableListOf<Pair<iTermuxEnvironmentValidationResult, iTermuxDegradedCause?>>()

        val runtime = iTermuxRuntimeInitializer.initialize(
            filesDir = Files.createTempDirectory("itermux-lifecycle-bootstrap").toFile().absolutePath,
            hostPackageName = "com.darkian.host",
            supportedAbis = listOf("arm64-v8a"),
            isBootstrapPayloadPackaged = true,
            autoInstallBootstrap = true,
            bootstrapInstaller = { currentRuntime ->
                currentRuntime.copy(
                    isBootstrapRequired = false,
                    bootstrapState = iTermuxBootstrapState.READY,
                    failureCause = null,
                    degradedCause = null,
                )
            },
            bootstrapStateObserver = { state, cause ->
                bootstrapStates += state to cause
            },
            environmentValidationObserver = { result, cause ->
                validations += result to cause
            },
        )

        assertEquals(
            listOf(
                iTermuxBootstrapState.UNINITIALIZED to null,
                iTermuxBootstrapState.EXTRACTING to null,
                iTermuxBootstrapState.VERIFYING to null,
                iTermuxBootstrapState.READY to null,
            ),
            bootstrapStates,
        )
        assertEquals(listOf(iTermuxEnvironmentValidationResult.VALID to null), validations)
        assertEquals(iTermuxBootstrapState.READY, runtime.bootstrapState)
    }

    @Test
    fun refreshNotifiesDegradedEnvironmentValidation() {
        val initial = iTermuxRuntimeInitializer.initialize(
            filesDir = Files.createTempDirectory("itermux-lifecycle-refresh").toFile().absolutePath,
            hostPackageName = "com.darkian.host",
        )
        File(initial.paths.binDir).mkdirs()
        File(initial.paths.binDir, "bash").writeText("binary")
        File(initial.paths.etcDir).mkdirs()
        File(initial.paths.etcDir, "profile").writeText("export TERM=xterm-256color\n")

        val bootstrapStates = mutableListOf<Pair<iTermuxBootstrapState, iTermuxRuntimeFailureCause?>>()
        val validations = mutableListOf<Pair<iTermuxEnvironmentValidationResult, iTermuxDegradedCause?>>()

        val refreshed = iTermuxRuntimeInitializer.refresh(
            identity = initial.identity,
            paths = initial.paths,
            supportedPackages = initial.supportedPackages,
            supportedAbis = initial.supportedAbis,
            bootstrapAssetPath = initial.bootstrapAssetPath,
            bootstrapVariantAbi = initial.bootstrapVariantAbi,
            isBootstrapPayloadPackaged = initial.isBootstrapPayloadPackaged,
            bootstrapStateObserver = { state, cause ->
                bootstrapStates += state to cause
            },
            environmentValidationObserver = { result, cause ->
                validations += result to cause
            },
        )

        assertEquals(listOf(iTermuxBootstrapState.DEGRADED to iTermuxRuntimeFailureCause.ENVIRONMENT_DEGRADED), bootstrapStates)
        assertEquals(
            listOf(iTermuxEnvironmentValidationResult.DEGRADED to iTermuxDegradedCause.MISSING_BINARY),
            validations,
        )
        assertEquals(iTermuxBootstrapState.DEGRADED, refreshed.bootstrapState)
    }

    @Test
    fun createSessionDispatchesStartingAndRunningStatesToRegisteredListener() {
        val runtime = iTermuxRuntimeInitializer.initialize(
            filesDir = Files.createTempDirectory("itermux-lifecycle-session").toFile().absolutePath,
            hostPackageName = "com.darkian.host",
        )
        val sessionStates = mutableListOf<Pair<String, iTermuxSessionState>>()
        val listener = object : iTermuxRuntimeLifecycleListener {
            override fun onBootstrapState(
                state: iTermuxBootstrapState,
                cause: iTermuxRuntimeFailureCause?,
            ) = Unit

            override fun onSessionState(
                sessionId: String,
                state: iTermuxSessionState,
            ) {
                sessionStates += sessionId to state
            }

            override fun onEnvironmentValidation(
                result: iTermuxEnvironmentValidationResult,
                cause: iTermuxDegradedCause?,
            ) = Unit
        }

        iTermux.addLifecycleListener(listener)
        try {
            val session = iTermux.createSession(runtime = runtime, sessionId = "listener-session")

            assertEquals("listener-session", session.id)
            assertEquals(
                listOf(
                    "listener-session" to iTermuxSessionState.STARTING,
                    "listener-session" to iTermuxSessionState.RUNNING,
                ),
                sessionStates,
            )
        } finally {
            iTermux.removeLifecycleListener(listener)
        }
    }
}

package com.darkian.itermux.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.nio.file.Files

class iTermuxBootstrapStateMachineTest {
    @Test
    fun successfulBootstrapEmitsExtractingVerifyingReady() {
        val runtime = baseRuntime()
        val observedStates = mutableListOf<iTermuxBootstrapState>()

        val bootstrapped = iTermuxBootstrapStateMachine.bootstrap(
            runtime = runtime,
            bootstrapInstaller = {
                runtime.copy(
                    isBootstrapRequired = false,
                    bootstrapState = iTermuxBootstrapState.READY,
                    failureCause = null,
                    degradedCause = null,
                )
            },
            failureTracker = InMemoryBootstrapFailureTracker(),
            stateObserver = { state, _ -> observedStates += state },
        )

        assertEquals(
            listOf(
                iTermuxBootstrapState.EXTRACTING,
                iTermuxBootstrapState.VERIFYING,
                iTermuxBootstrapState.READY,
            ),
            observedStates,
        )
        assertEquals(iTermuxBootstrapState.READY, bootstrapped.bootstrapState)
        assertNull(bootstrapped.failureCause)
    }

    @Test
    fun retriesOnceWhenPreviousFailureIsOutsideRetryWindow() {
        val runtime = baseRuntime()
        val observedStates = mutableListOf<iTermuxBootstrapState>()
        val tracker = InMemoryBootstrapFailureTracker(lastFailureAtMillis = 0L)
        var attempts = 0

        val bootstrapped = iTermuxBootstrapStateMachine.bootstrap(
            runtime = runtime,
            bootstrapInstaller = {
                attempts += 1
                if (attempts == 1) {
                    error("first extraction failed")
                }
                runtime.copy(
                    isBootstrapRequired = false,
                    bootstrapState = iTermuxBootstrapState.READY,
                    failureCause = null,
                    degradedCause = null,
                )
            },
            failureTracker = tracker,
            nowProvider = { 31_000L },
            stateObserver = { state, _ -> observedStates += state },
        )

        assertEquals(2, attempts)
        assertEquals(
            listOf(
                iTermuxBootstrapState.EXTRACTING,
                iTermuxBootstrapState.PARTIAL,
                iTermuxBootstrapState.RECOVERING,
                iTermuxBootstrapState.VERIFYING,
                iTermuxBootstrapState.READY,
            ),
            observedStates,
        )
        assertEquals(iTermuxBootstrapState.READY, bootstrapped.bootstrapState)
        assertTrue(tracker.cleared)
    }

    @Test
    fun doesNotRetryWhenPreviousFailureIsInsideRetryWindow() {
        val runtime = baseRuntime()
        val observedStates = mutableListOf<iTermuxBootstrapState>()
        val tracker = InMemoryBootstrapFailureTracker(lastFailureAtMillis = 10_000L)
        var attempts = 0

        val bootstrapped = iTermuxBootstrapStateMachine.bootstrap(
            runtime = runtime,
            bootstrapInstaller = {
                attempts += 1
                error("persistent extraction failure")
            },
            failureTracker = tracker,
            nowProvider = { 35_000L },
            stateObserver = { state, _ -> observedStates += state },
        )

        assertEquals(1, attempts)
        assertEquals(
            listOf(
                iTermuxBootstrapState.EXTRACTING,
                iTermuxBootstrapState.PARTIAL,
                iTermuxBootstrapState.FAILED,
            ),
            observedStates,
        )
        assertEquals(iTermuxBootstrapState.FAILED, bootstrapped.bootstrapState)
        assertEquals(iTermuxRuntimeFailureCause.BOOTSTRAP_EXTRACTION_FAILED, bootstrapped.failureCause)
    }

    @Test
    fun verificationFailureSurfacesCorruptedState() {
        val runtime = baseRuntime()
        val observedStates = mutableListOf<iTermuxBootstrapState>()

        val bootstrapped = iTermuxBootstrapStateMachine.bootstrap(
            runtime = runtime,
            bootstrapInstaller = {
                runtime.copy(
                    isBootstrapRequired = false,
                    bootstrapState = iTermuxBootstrapState.DEGRADED,
                    failureCause = iTermuxRuntimeFailureCause.ENVIRONMENT_DEGRADED,
                    degradedCause = iTermuxDegradedCause.MISSING_BINARY,
                )
            },
            failureTracker = InMemoryBootstrapFailureTracker(),
            stateObserver = { state, _ -> observedStates += state },
        )

        assertEquals(
            listOf(
                iTermuxBootstrapState.EXTRACTING,
                iTermuxBootstrapState.VERIFYING,
                iTermuxBootstrapState.CORRUPTED,
            ),
            observedStates,
        )
        assertEquals(iTermuxBootstrapState.CORRUPTED, bootstrapped.bootstrapState)
        assertEquals(iTermuxRuntimeFailureCause.BOOTSTRAP_CORRUPTED, bootstrapped.failureCause)
    }

    private fun baseRuntime(): iTermuxRuntime {
        return iTermuxRuntimeInitializer.initialize(
            filesDir = Files.createTempDirectory("itermux-bootstrap-state-machine").toFile().absolutePath,
            hostPackageName = "com.darkian.host",
            supportedAbis = listOf("arm64-v8a"),
            isBootstrapPayloadPackaged = true,
        )
    }

    private class InMemoryBootstrapFailureTracker(
        private var lastFailureAtMillis: Long? = null,
    ) : iTermuxBootstrapFailureTracker {
        var cleared: Boolean = false

        override fun lastFailureAtMillis(paths: iTermuxPaths): Long? = lastFailureAtMillis

        override fun recordFailure(paths: iTermuxPaths, failedAtMillis: Long) {
            lastFailureAtMillis = failedAtMillis
        }

        override fun clearFailure(paths: iTermuxPaths) {
            cleared = true
            lastFailureAtMillis = null
        }
    }
}

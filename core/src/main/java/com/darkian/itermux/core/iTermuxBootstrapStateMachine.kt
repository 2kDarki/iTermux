package com.darkian.itermux.core

// INTERNAL-TERMUX MODIFIED - merge carefully

/**
 * Drives bootstrap retries and verification before a runtime can become ready.
 */
object iTermuxBootstrapStateMachine {
    private const val RETRY_WINDOW_MILLIS = 30_000L

    fun bootstrap(
        runtime: iTermuxRuntime,
        bootstrapInstaller: (iTermuxRuntime) -> iTermuxRuntime,
        failureTracker: iTermuxBootstrapFailureTracker = iTermuxBootstrapFileFailureTracker,
        nowProvider: () -> Long = System::currentTimeMillis,
        stateObserver: (iTermuxBootstrapState, iTermuxRuntimeFailureCause?) -> Unit = { _, _ -> },
    ): iTermuxRuntime {
        val nowMillis = nowProvider()
        val lastFailureAtMillis = failureTracker.lastFailureAtMillis(runtime.paths)

        emitState(
            stateObserver = stateObserver,
            state = iTermuxBootstrapState.EXTRACTING,
        )

        val firstAttempt = runCatching {
            bootstrapInstaller(
                runtime.copy(
                    bootstrapState = iTermuxBootstrapState.EXTRACTING,
                    failureCause = null,
                    degradedCause = null,
                ),
            )
        }
        if (firstAttempt.isSuccess) {
            return verifyInstalledRuntime(
                runtime = firstAttempt.getOrThrow(),
                failureTracker = failureTracker,
                nowMillis = nowMillis,
                stateObserver = stateObserver,
            )
        }

        failureTracker.recordFailure(runtime.paths, nowMillis)
        emitState(
            stateObserver = stateObserver,
            state = iTermuxBootstrapState.PARTIAL,
        )

        val canRetry = lastFailureAtMillis == null || (nowMillis - lastFailureAtMillis) > RETRY_WINDOW_MILLIS
        if (!canRetry) {
            return extractionFailedRuntime(
                runtime = runtime,
                stateObserver = stateObserver,
            )
        }

        emitState(
            stateObserver = stateObserver,
            state = iTermuxBootstrapState.RECOVERING,
        )

        val retryAttempt = runCatching {
            bootstrapInstaller(
                runtime.copy(
                    bootstrapState = iTermuxBootstrapState.RECOVERING,
                    failureCause = null,
                    degradedCause = null,
                ),
            )
        }
        if (retryAttempt.isSuccess) {
            return verifyInstalledRuntime(
                runtime = retryAttempt.getOrThrow(),
                failureTracker = failureTracker,
                nowMillis = nowMillis,
                stateObserver = stateObserver,
            )
        }

        failureTracker.recordFailure(runtime.paths, nowMillis)
        return extractionFailedRuntime(
            runtime = runtime,
            stateObserver = stateObserver,
        )
    }

    private fun verifyInstalledRuntime(
        runtime: iTermuxRuntime,
        failureTracker: iTermuxBootstrapFailureTracker,
        nowMillis: Long,
        stateObserver: (iTermuxBootstrapState, iTermuxRuntimeFailureCause?) -> Unit,
    ): iTermuxRuntime {
        emitState(
            stateObserver = stateObserver,
            state = iTermuxBootstrapState.VERIFYING,
        )

        val verifiedRuntime = when {
            runtime.isBootstrapRequired -> runtime.copy(
                bootstrapState = iTermuxBootstrapState.FAILED,
                failureCause = iTermuxRuntimeFailureCause.BOOTSTRAP_PARTIAL,
                degradedCause = null,
            )

            runtime.bootstrapState == iTermuxBootstrapState.DEGRADED -> runtime.copy(
                bootstrapState = iTermuxBootstrapState.CORRUPTED,
                failureCause = iTermuxRuntimeFailureCause.BOOTSTRAP_CORRUPTED,
            )

            else -> runtime.copy(
                bootstrapState = iTermuxBootstrapState.READY,
                failureCause = null,
                degradedCause = null,
            )
        }

        if (verifiedRuntime.bootstrapState == iTermuxBootstrapState.READY) {
            failureTracker.clearFailure(runtime.paths)
        } else {
            failureTracker.recordFailure(runtime.paths, nowMillis)
        }

        emitState(
            stateObserver = stateObserver,
            state = verifiedRuntime.bootstrapState,
            cause = verifiedRuntime.failureCause,
        )
        return verifiedRuntime
    }

    private fun extractionFailedRuntime(
        runtime: iTermuxRuntime,
        stateObserver: (iTermuxBootstrapState, iTermuxRuntimeFailureCause?) -> Unit,
    ): iTermuxRuntime {
        val failedRuntime = runtime.copy(
            bootstrapState = iTermuxBootstrapState.FAILED,
            failureCause = iTermuxRuntimeFailureCause.BOOTSTRAP_EXTRACTION_FAILED,
            degradedCause = null,
        )
        emitState(
            stateObserver = stateObserver,
            state = failedRuntime.bootstrapState,
            cause = failedRuntime.failureCause,
        )
        return failedRuntime
    }

    private fun emitState(
        stateObserver: (iTermuxBootstrapState, iTermuxRuntimeFailureCause?) -> Unit,
        state: iTermuxBootstrapState,
        cause: iTermuxRuntimeFailureCause? = null,
    ) {
        stateObserver(state, cause)
    }
}

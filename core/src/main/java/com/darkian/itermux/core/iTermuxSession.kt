package com.darkian.itermux.core
// INTERNAL-TERMUX MODIFIED - merge carefully

/**
 * Host-facing session metadata for the embeddable runtime.
 */
data class iTermuxSession(
    val id: String,
    val backend: iTermuxSessionBackend,
    val mode: iTermuxSessionMode,
    val shellSpec: iTermuxShellSpec,
    val state: iTermuxSessionState = iTermuxSessionState.RUNNING,
    val recoveryAttempts: Int = 0,
    val failureCause: iTermuxRuntimeFailureCause? = null,
)

data class iTermuxSessionBackend(
    val id: String,
)

object iTermuxSessionBackends {
    val NATIVE: iTermuxSessionBackend = iTermuxSessionBackend(id = "native")
}

enum class iTermuxSessionMode {
    LOGIN_SHELL,
    COMMAND,
    FILE_COMMAND,
}

enum class iTermuxSessionState {
    STARTING,
    RUNNING,
    SUSPENDED,
    KILLED_BY_OS,
    DEAD,
    RECOVERING,
    READY,
}

object iTermuxSessionController {
    fun start(
        sessionId: String,
        sessionFactory: () -> iTermuxSession,
        failureSessionFactory: () -> iTermuxSession,
        stateObserver: (String, iTermuxSessionState) -> Unit = { _, _ -> },
    ): iTermuxSession {
        stateObserver(sessionId, iTermuxSessionState.STARTING)
        val created = runCatching(sessionFactory).getOrElse {
            val failed = failureSessionFactory()
            failed.copy(
                id = sessionId,
                state = iTermuxSessionState.DEAD,
                failureCause = failed.failureCause ?: iTermuxRuntimeFailureCause.SESSION_START_FAILED,
            )
        }

        return if (created.state == iTermuxSessionState.DEAD || created.failureCause != null) {
            val failed = created.copy(
                id = sessionId,
                state = iTermuxSessionState.DEAD,
                failureCause = created.failureCause ?: iTermuxRuntimeFailureCause.SESSION_START_FAILED,
            )
            stateObserver(sessionId, failed.state)
            failed
        } else {
            val running = created.copy(
                id = sessionId,
                state = iTermuxSessionState.RUNNING,
                failureCause = null,
            )
            stateObserver(sessionId, running.state)
            running
        }
    }

    fun suspend(
        session: iTermuxSession,
        stateObserver: (String, iTermuxSessionState) -> Unit = { _, _ -> },
    ): iTermuxSession {
        val suspended = session.copy(state = iTermuxSessionState.SUSPENDED)
        stateObserver(suspended.id, suspended.state)
        return suspended
    }

    fun resume(
        session: iTermuxSession,
        stateObserver: (String, iTermuxSessionState) -> Unit = { _, _ -> },
    ): iTermuxSession {
        val resumed = session.copy(state = iTermuxSessionState.RUNNING)
        stateObserver(resumed.id, resumed.state)
        return resumed
    }

    fun recoverFromOsKill(
        session: iTermuxSession,
        restartSession: (iTermuxSession) -> iTermuxSession?,
        stateObserver: (String, iTermuxSessionState) -> Unit = { _, _ -> },
    ): iTermuxSession {
        val killed = session.copy(state = iTermuxSessionState.KILLED_BY_OS)
        stateObserver(killed.id, killed.state)

        if (session.recoveryAttempts >= 1) {
            val dead = killed.copy(
                state = iTermuxSessionState.DEAD,
                failureCause = iTermuxRuntimeFailureCause.SESSION_KILLED_UNRECOVERABLE,
            )
            stateObserver(dead.id, dead.state)
            return dead
        }

        stateObserver(killed.id, iTermuxSessionState.RECOVERING)
        val restarted = runCatching { restartSession(killed) }.getOrNull()
        return if (restarted == null) {
            val dead = killed.copy(
                state = iTermuxSessionState.DEAD,
                recoveryAttempts = session.recoveryAttempts + 1,
                failureCause = iTermuxRuntimeFailureCause.SESSION_KILLED_UNRECOVERABLE,
            )
            stateObserver(dead.id, dead.state)
            dead
        } else {
            val ready = restarted.copy(
                id = session.id,
                backend = session.backend,
                mode = session.mode,
                state = iTermuxSessionState.READY,
                recoveryAttempts = session.recoveryAttempts + 1,
                failureCause = null,
            )
            stateObserver(ready.id, ready.state)
            ready
        }
    }
}

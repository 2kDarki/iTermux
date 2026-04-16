package com.darkian.itermux.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class iTermuxSessionControllerTest {
    @Test
    fun startReturnsDeadSessionWithNamedCauseWhenFactoryFails() {
        val observedStates = mutableListOf<Pair<String, iTermuxSessionState>>()

        val session = iTermuxSessionController.start(
            sessionId = "broken",
            stateObserver = { sessionId, state ->
                observedStates += sessionId to state
            },
            sessionFactory = {
                error("boom")
            },
            failureSessionFactory = {
                placeholderSession(id = "broken")
            },
        )

        assertEquals(
            listOf(
                "broken" to iTermuxSessionState.STARTING,
                "broken" to iTermuxSessionState.DEAD,
            ),
            observedStates,
        )
        assertEquals(iTermuxSessionState.DEAD, session.state)
        assertEquals(iTermuxRuntimeFailureCause.SESSION_START_FAILED, session.failureCause)
    }

    @Test
    fun recoversKilledSessionExactlyOnce() {
        val observedStates = mutableListOf<Pair<String, iTermuxSessionState>>()
        val original = placeholderSession(id = "main")

        val recovered = iTermuxSessionController.recoverFromOsKill(
            session = original,
            stateObserver = { sessionId, state ->
                observedStates += sessionId to state
            },
            restartSession = { killed ->
                killed.copy(
                    shellSpec = killed.shellSpec.copy(arguments = listOf("--restarted")),
                )
            },
        )

        assertEquals(
            listOf(
                "main" to iTermuxSessionState.KILLED_BY_OS,
                "main" to iTermuxSessionState.RECOVERING,
                "main" to iTermuxSessionState.READY,
            ),
            observedStates,
        )
        assertEquals(iTermuxSessionState.READY, recovered.state)
        assertEquals(1, recovered.recoveryAttempts)
        assertNull(recovered.failureCause)
        assertEquals(listOf("--restarted"), recovered.shellSpec.arguments)
    }

    @Test
    fun marksSessionDeadWhenRecoveryIsAlreadyExhausted() {
        val observedStates = mutableListOf<Pair<String, iTermuxSessionState>>()
        val original = placeholderSession(
            id = "main",
            recoveryAttempts = 1,
            state = iTermuxSessionState.READY,
        )

        val recovered = iTermuxSessionController.recoverFromOsKill(
            session = original,
            stateObserver = { sessionId, state ->
                observedStates += sessionId to state
            },
            restartSession = {
                error("should not be called")
            },
        )

        assertEquals(
            listOf(
                "main" to iTermuxSessionState.KILLED_BY_OS,
                "main" to iTermuxSessionState.DEAD,
            ),
            observedStates,
        )
        assertEquals(iTermuxSessionState.DEAD, recovered.state)
        assertEquals(1, recovered.recoveryAttempts)
        assertEquals(iTermuxRuntimeFailureCause.SESSION_KILLED_UNRECOVERABLE, recovered.failureCause)
    }

    @Test
    fun marksSessionDeadWhenRecoveryAttemptFails() {
        val observedStates = mutableListOf<Pair<String, iTermuxSessionState>>()
        val original = placeholderSession(id = "main")

        val recovered = iTermuxSessionController.recoverFromOsKill(
            session = original,
            stateObserver = { sessionId, state ->
                observedStates += sessionId to state
            },
            restartSession = {
                null
            },
        )

        assertEquals(
            listOf(
                "main" to iTermuxSessionState.KILLED_BY_OS,
                "main" to iTermuxSessionState.RECOVERING,
                "main" to iTermuxSessionState.DEAD,
            ),
            observedStates,
        )
        assertEquals(iTermuxSessionState.DEAD, recovered.state)
        assertEquals(1, recovered.recoveryAttempts)
        assertEquals(iTermuxRuntimeFailureCause.SESSION_KILLED_UNRECOVERABLE, recovered.failureCause)
    }

    private fun placeholderSession(
        id: String,
        state: iTermuxSessionState = iTermuxSessionState.RUNNING,
        recoveryAttempts: Int = 0,
    ): iTermuxSession {
        return iTermuxSession(
            id = id,
            backend = iTermuxSessionBackends.NATIVE,
            mode = iTermuxSessionMode.LOGIN_SHELL,
            shellSpec = iTermuxShellSpec(
                executable = "/usr/bin/sh",
                arguments = emptyList(),
                workingDirectory = "/tmp",
                environment = emptyMap(),
            ),
            state = state,
            recoveryAttempts = recoveryAttempts,
            failureCause = null,
        )
    }
}

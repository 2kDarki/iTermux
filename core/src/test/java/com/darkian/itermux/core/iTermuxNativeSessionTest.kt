package com.darkian.itermux.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.io.File
import java.nio.file.Files

class iTermuxNativeSessionTest {
    @Test
    fun createsDefaultNativeSessionFromRuntime() {
        val runtime = readyRuntime("itermux-native-session")

        val session = iTermux.createSession(
            runtime = runtime,
            sessionId = "main",
        )

        assertEquals("main", session.id)
        assertEquals(iTermuxSessionBackends.NATIVE, session.backend)
        assertEquals(iTermuxSessionMode.LOGIN_SHELL, session.mode)
        assertEquals(iTermuxSessionState.RUNNING, session.state)
        assertEquals(0, session.recoveryAttempts)
        assertNull(session.failureCause)
        assertEquals("${runtime.paths.binDir}/sh", session.shellSpec.executable)
        assertEquals(runtime.defaultWorkingDirectory, session.shellSpec.workingDirectory)
    }

    @Test
    fun wrapsCommandAndFileSessionsWithSharedMetadata() {
        val runtime = readyRuntime("itermux-native-session")
        val scriptFile = Files.createTempFile("itermux-native-session", ".sh").toFile().apply {
            writeText("echo hello\n")
            deleteOnExit()
        }

        val commandSession = runtime.createCommandSession(
            sessionId = "command",
            executable = "${runtime.paths.binDir}/env",
            arguments = listOf("printenv", "PREFIX"),
        )
        val fileSession = runtime.createFileSession(
            sessionId = "script",
            executable = scriptFile.absolutePath,
            arguments = listOf("arg1"),
        )

        assertEquals("command", commandSession.id)
        assertEquals(iTermuxSessionBackends.NATIVE, commandSession.backend)
        assertEquals(iTermuxSessionMode.COMMAND, commandSession.mode)
        assertEquals(iTermuxSessionState.RUNNING, commandSession.state)
        assertEquals("${runtime.paths.binDir}/env", commandSession.shellSpec.executable)

        assertEquals("script", fileSession.id)
        assertEquals(iTermuxSessionBackends.NATIVE, fileSession.backend)
        assertEquals(iTermuxSessionMode.FILE_COMMAND, fileSession.mode)
        assertEquals(iTermuxSessionState.RUNNING, fileSession.state)
        assertEquals("${runtime.paths.binDir}/sh", fileSession.shellSpec.executable)
        assertEquals(listOf(scriptFile.absolutePath, "arg1"), fileSession.shellSpec.arguments)
    }

    private fun readyRuntime(prefix: String): iTermuxRuntime {
        val initial = iTermuxRuntimeInitializer.initialize(
            filesDir = Files.createTempDirectory(prefix).toFile().absolutePath,
            hostPackageName = "com.darkian.host",
        )
        File(initial.paths.binDir).mkdirs()
        File(initial.paths.binDir, "sh").writeText("#!/bin/sh\necho ready\n")
        File(initial.paths.binDir, "env").writeText("#!/bin/sh\nenv\n")
        File(initial.paths.etcDir).mkdirs()
        File(initial.paths.etcDir, "profile").writeText("export TERM=xterm-256color\n")
        return iTermuxRuntimeInitializer.refresh(
            identity = initial.identity,
            paths = initial.paths,
            supportedPackages = initial.supportedPackages,
            isProotEnabled = initial.isProotEnabled,
            supportedAbis = initial.supportedAbis,
            bootstrapAssetPath = initial.bootstrapAssetPath,
            bootstrapVariantAbi = initial.bootstrapVariantAbi,
            isBootstrapPayloadPackaged = initial.isBootstrapPayloadPackaged,
        )
    }
}

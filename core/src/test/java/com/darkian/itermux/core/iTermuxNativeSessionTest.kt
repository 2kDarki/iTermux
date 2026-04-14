package com.darkian.itermux.core

import org.junit.Assert.assertEquals
import org.junit.Test
import java.nio.file.Files

class iTermuxNativeSessionTest {
    @Test
    fun createsDefaultNativeSessionFromRuntime() {
        val runtime = iTermuxRuntimeInitializer.initialize(
            filesDir = Files.createTempDirectory("itermux-native-session").toFile().absolutePath,
            hostPackageName = "com.darkian.host",
        )

        val session = iTermux.createSession(
            runtime = runtime,
            sessionId = "main",
        )

        assertEquals("main", session.id)
        assertEquals(iTermuxSessionKind.NATIVE, session.kind)
        assertEquals(iTermuxSessionMode.LOGIN_SHELL, session.mode)
        assertEquals("${runtime.paths.binDir}/sh", session.shellSpec.executable)
        assertEquals(runtime.defaultWorkingDirectory, session.shellSpec.workingDirectory)
    }

    @Test
    fun wrapsCommandAndFileSessionsWithSharedMetadata() {
        val runtime = iTermuxRuntimeInitializer.initialize(
            filesDir = Files.createTempDirectory("itermux-native-session").toFile().absolutePath,
            hostPackageName = "com.darkian.host",
        )
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
        assertEquals(iTermuxSessionKind.NATIVE, commandSession.kind)
        assertEquals(iTermuxSessionMode.COMMAND, commandSession.mode)
        assertEquals("${runtime.paths.binDir}/env", commandSession.shellSpec.executable)

        assertEquals("script", fileSession.id)
        assertEquals(iTermuxSessionKind.NATIVE, fileSession.kind)
        assertEquals(iTermuxSessionMode.FILE_COMMAND, fileSession.mode)
        assertEquals("${runtime.paths.binDir}/sh", fileSession.shellSpec.executable)
        assertEquals(listOf(scriptFile.absolutePath, "arg1"), fileSession.shellSpec.arguments)
    }
}

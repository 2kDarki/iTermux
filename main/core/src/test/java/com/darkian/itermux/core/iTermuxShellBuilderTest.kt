package com.darkian.itermux.core

import org.junit.Assert.assertEquals
import org.junit.Test
import java.nio.file.Files

class iTermuxShellBuilderTest {
    @Test
    fun buildsDefaultLoginShellFromRuntimePaths() {
        val paths = iTermuxPathResolver.resolve(
            filesDir = "/app/files",
            hostPackageName = "com.darkian.itermux",
        )

        val spec = iTermuxShellBuilder.loginShell(paths)

        assertEquals("/app/files/usr/bin/sh", spec.executable)
        assertEquals(emptyList<String>(), spec.arguments)
        assertEquals("/app/files/home", spec.workingDirectory)
        assertEquals("/app/files/usr", spec.environment["PREFIX"])
        assertEquals("/app/files/home", spec.environment["HOME"])
    }

    @Test
    fun supportsCustomShellBinaryAndWorkingDirectory() {
        val paths = iTermuxPathResolver.resolve(
            filesDir = "/app/files",
            hostPackageName = "com.darkian.itermux",
        )

        val spec = iTermuxShellBuilder.loginShell(
            paths = paths,
            shellBinary = "bash",
            workingDirectory = "/workspace",
        )

        assertEquals("/app/files/usr/bin/bash", spec.executable)
        assertEquals("/workspace", spec.workingDirectory)
    }

    @Test
    fun buildsArbitraryCommandWithBaselineEnvironment() {
        val paths = iTermuxPathResolver.resolve(
            filesDir = "/app/files",
            hostPackageName = "com.darkian.itermux",
        )

        val spec = iTermuxShellBuilder.command(
            paths = paths,
            executable = "/app/files/usr/bin/env",
            arguments = listOf("printenv", "PREFIX"),
            extraEnv = mapOf("LANG" to "en_US.UTF-8"),
        )

        assertEquals("/app/files/usr/bin/env", spec.executable)
        assertEquals(listOf("printenv", "PREFIX"), spec.arguments)
        assertEquals("en_US.UTF-8", spec.environment["LANG"])
        assertEquals("/app/files/usr/bin", spec.environment["PATH"])
    }

    @Test
    fun supportsFailSafeShellEnvironmentProfiles() {
        val paths = iTermuxPathResolver.resolve(
            filesDir = "/app/files",
            hostPackageName = "com.darkian.itermux",
        )

        val spec = iTermuxShellBuilder.loginShell(
            paths = paths,
            baseEnv = mapOf(
                "PATH" to "/system/bin",
                "TMPDIR" to "/tmp",
            ),
            failSafe = true,
        )

        assertEquals("/system/bin", spec.environment["PATH"])
        assertEquals("/tmp", spec.environment["TMPDIR"])
        assertEquals("/app/files/usr", spec.environment["PREFIX"])
    }

    @Test
    fun buildsFileCommandUsingTermuxInterpreterRules() {
        val paths = iTermuxPathResolver.resolve(
            filesDir = "/app/files",
            hostPackageName = "com.darkian.itermux",
        )
        val scriptFile = Files.createTempFile("itermux-shell-builder", ".sh").toFile().apply {
            writeText("echo hello\n")
            deleteOnExit()
        }

        val spec = iTermuxShellBuilder.fileCommand(
            paths = paths,
            executable = scriptFile.absolutePath,
            arguments = listOf("arg1"),
        )

        assertEquals("/app/files/usr/bin/sh", spec.executable)
        assertEquals(listOf(scriptFile.absolutePath, "arg1"), spec.arguments)
        assertEquals("/app/files/home", spec.workingDirectory)
    }
}

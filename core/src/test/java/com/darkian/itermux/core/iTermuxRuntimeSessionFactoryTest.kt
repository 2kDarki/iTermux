package com.darkian.itermux.core

import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.File
import java.nio.file.Files

class iTermuxRuntimeSessionFactoryTest {
    @Test
    fun createsLoginShellFromRuntimeDefaults() {
        val runtime = iTermuxRuntimeInitializer.initialize(
            filesDir = Files.createTempDirectory("itermux-runtime-session").toFile().absolutePath,
            hostPackageName = "com.darkian.host",
        )

        val spec = runtime.loginShell()

        assertEquals("${runtime.paths.binDir}/sh", spec.executable)
        assertEquals(runtime.defaultWorkingDirectory, spec.workingDirectory)
        assertEquals(runtime.environment["PREFIX"], spec.environment["PREFIX"])
    }

    @Test
    fun createsCommandAndFileCommandFromRuntimeDefaults() {
        val runtime = iTermuxRuntimeInitializer.initialize(
            filesDir = Files.createTempDirectory("itermux-runtime-session").toFile().absolutePath,
            hostPackageName = "com.darkian.host",
        )
        val scriptFile = Files.createTempFile("itermux-runtime-session", ".sh").toFile().apply {
            writeText("echo hello\n")
            deleteOnExit()
        }

        val commandSpec = runtime.command(
            executable = "${runtime.paths.binDir}/env",
            arguments = listOf("printenv", "PREFIX"),
        )
        val fileSpec = runtime.fileCommand(
            executable = scriptFile.absolutePath,
            arguments = listOf("arg1"),
        )

        assertEquals(runtime.defaultWorkingDirectory, commandSpec.workingDirectory)
        assertEquals("${runtime.paths.binDir}/env", commandSpec.executable)
        assertEquals("${runtime.paths.binDir}/sh", fileSpec.executable)
        assertEquals(listOf(scriptFile.absolutePath, "arg1"), fileSpec.arguments)
    }

    @Test
    fun refreshedRuntimeUsesUpdatedWorkingDirectoryForNewSessions() {
        val runtime = iTermuxRuntimeInitializer.initialize(
            filesDir = Files.createTempDirectory("itermux-runtime-session").toFile().absolutePath,
            hostPackageName = "com.darkian.host",
        )
        val configuredDirectory = File(runtime.paths.homeDir, "workspace").apply { mkdirs() }
        File(runtime.paths.propertiesSecondaryFile).apply {
            parentFile?.mkdirs()
            writeText("default-working-directory=${configuredDirectory.absolutePath.replace('\\', '/')}\n")
        }

        val refreshed = iTermux.refresh(runtime)
        val spec = refreshed.loginShell()

        assertEquals(configuredDirectory.absolutePath, spec.workingDirectory)
    }
}

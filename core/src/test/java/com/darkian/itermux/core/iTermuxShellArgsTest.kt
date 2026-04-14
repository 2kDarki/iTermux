package com.darkian.itermux.core

import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.File
import java.nio.file.Files

class iTermuxShellArgsTest {
    private val paths = iTermuxPathResolver.resolve(
        filesDir = "/app/files",
        hostPackageName = "com.darkian.host",
    )

    @Test
    fun keepsElfExecutablesDirect() {
        val executable = createTempFileWithBytes(byteArrayOf(0x7F, 'E'.code.toByte(), 'L'.code.toByte(), 'F'.code.toByte(), 0x02))

        assertEquals(
            listOf(executable, "--version"),
            iTermuxShellArgs.setup(
                executable = executable,
                arguments = listOf("--version"),
                paths = paths,
            ),
        )
    }

    @Test
    fun prependsStandardShellForPlainScripts() {
        val executable = createTempFileWithText("echo hello\n")

        assertEquals(
            listOf("${paths.binDir}/sh", executable, "arg1"),
            iTermuxShellArgs.setup(
                executable = executable,
                arguments = listOf("arg1"),
                paths = paths,
            ),
        )
    }

    @Test
    fun rewritesUsrBinShebangToPrefixInterpreter() {
        val executable = createTempFileWithText("#!/bin/bash\nexit 0\n")

        assertEquals(
            listOf("${paths.binDir}/bash", executable),
            iTermuxShellArgs.setup(
                executable = executable,
                arguments = emptyList(),
                paths = paths,
            ),
        )
    }

    @Test
    fun leavesMissingFilesUntouched() {
        val missingExecutable = File(Files.createTempDirectory("itermux-shell").toFile(), "missing-script").absolutePath

        assertEquals(
            listOf(missingExecutable, "arg1"),
            iTermuxShellArgs.setup(
                executable = missingExecutable,
                arguments = listOf("arg1"),
                paths = paths,
            ),
        )
    }

    private fun createTempFileWithText(content: String): String {
        val file = Files.createTempFile("itermux-shell", ".tmp").toFile()
        file.writeText(content)
        file.deleteOnExit()
        return file.absolutePath
    }

    private fun createTempFileWithBytes(content: ByteArray): String {
        val file = Files.createTempFile("itermux-shell", ".tmp").toFile()
        file.writeBytes(content)
        file.deleteOnExit()
        return file.absolutePath
    }
}

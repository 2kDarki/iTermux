package com.darkian.itermux.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.nio.file.Files

class iTermuxPropertiesTest {
    @Test
    fun exposesPrimaryThenSecondaryPropertiesPaths() {
        val paths = iTermuxPathResolver.resolve(
            filesDir = "/app/files",
            hostPackageName = "com.darkian.host",
        )

        assertEquals(
            listOf(paths.propertiesPrimaryFile, paths.propertiesSecondaryFile),
            iTermuxProperties.propertyFiles(paths),
        )
    }

    @Test
    fun prefersPrimaryPropertiesFileWhenBothExist() {
        val runtime = createRuntime()
        File(runtime.paths.propertiesPrimaryFile).apply {
            parentFile?.mkdirs()
            writeText("bell-character=beep\n")
        }
        File(runtime.paths.propertiesSecondaryFile).apply {
            parentFile?.mkdirs()
            writeText("bell-character=ignore\n")
        }

        val selectedFile = iTermuxProperties.findReadablePropertiesFile(runtime.paths)

        assertEquals(
            File(runtime.paths.propertiesPrimaryFile).absolutePath,
            selectedFile?.absolutePath,
        )
    }

    @Test
    fun fallsBackToSecondaryPropertiesFileWhenPrimaryIsMissing() {
        val runtime = createRuntime()
        File(runtime.paths.propertiesSecondaryFile).apply {
            parentFile?.mkdirs()
            writeText("bell-character=ignore\n")
        }

        val selectedFile = iTermuxProperties.findReadablePropertiesFile(runtime.paths)

        assertEquals(
            File(runtime.paths.propertiesSecondaryFile).absolutePath,
            selectedFile?.absolutePath,
        )
    }

    @Test
    fun loadsPropertiesFromSelectedFile() {
        val runtime = createRuntime()
        File(runtime.paths.propertiesSecondaryFile).apply {
            parentFile?.mkdirs()
            writeText("bell-character=ignore\ndefault-working-directory=/workspace\n")
        }

        val properties = iTermuxProperties.load(runtime.paths)

        assertEquals("ignore", properties["bell-character"])
        assertEquals("/workspace", properties["default-working-directory"])
    }

    @Test
    fun returnsEmptyPropertiesWhenNoReadableFileExists() {
        val runtime = createRuntime()

        assertNull(iTermuxProperties.findReadablePropertiesFile(runtime.paths))
        assertTrue(iTermuxProperties.load(runtime.paths).isEmpty())
    }

    private fun createRuntime(): iTermuxRuntime {
        return iTermuxRuntimeInitializer.initialize(
            filesDir = Files.createTempDirectory("itermux-properties").toFile().absolutePath,
            hostPackageName = "com.darkian.host",
        )
    }
}

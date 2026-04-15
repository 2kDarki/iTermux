package com.darkian.itermux.core

import org.apache.commons.compress.archivers.tar.TarArchiveEntry
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream
import org.apache.commons.compress.compressors.xz.XZCompressorOutputStream
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.nio.charset.StandardCharsets
import java.nio.file.Files

class iTermuxBootstrapInstallerTest {
    @Test
    fun installsPackagedBootstrapIntoPrefixAndRefreshesRuntimeState() {
        val runtime = iTermuxRuntimeInitializer.initialize(
            filesDir = Files.createTempDirectory("itermux-bootstrap-install").toFile().absolutePath,
            hostPackageName = "com.darkian.host",
            supportedPackages = listOf("bash", "coreutils"),
            bootstrapAssetPath = "itermux/bootstrap/bootstrap.tar.xz",
            isBootstrapPayloadPackaged = true,
        )

        val installed = iTermux.installBootstrap(runtime) {
            ByteArrayInputStream(
                bootstrapArchive(
                    "bin/sh" to "#!/bin/sh\necho embedded\n",
                    "etc/motd" to "hello\n",
                ),
            )
        }

        assertFalse(installed.isBootstrapRequired)
        assertEquals(runtime.supportedPackages, installed.supportedPackages)
        assertEquals(
            "#!/bin/sh\necho embedded\n",
            java.io.File(installed.paths.binDir, "sh").readText(),
        )
        assertEquals(
            "hello\n",
            java.io.File(installed.paths.etcDir, "motd").readText(),
        )
    }

    @Test
    fun rejectsTraversalEntriesDuringBootstrapExtraction() {
        val runtime = iTermuxRuntimeInitializer.initialize(
            filesDir = Files.createTempDirectory("itermux-bootstrap-traversal").toFile().absolutePath,
            hostPackageName = "com.darkian.host",
            isBootstrapPayloadPackaged = true,
        )

        val error = runCatching {
            iTermux.installBootstrap(runtime) {
                ByteArrayInputStream(
                    bootstrapArchive(
                        "../escape" to "nope\n",
                    ),
                )
            }
        }.exceptionOrNull()

        checkNotNull(error)
        assertTrue(error is IllegalStateException)
        assertTrue(error.message!!.contains("outside"))
    }

    @Test
    fun requiresPackagedPayloadWhenBootstrapIsStillNeeded() {
        val runtime = iTermuxRuntimeInitializer.initialize(
            filesDir = Files.createTempDirectory("itermux-bootstrap-missing").toFile().absolutePath,
            hostPackageName = "com.darkian.host",
            isBootstrapPayloadPackaged = false,
        )

        val error = runCatching {
            iTermux.installBootstrap(runtime) {
                ByteArrayInputStream(ByteArray(0))
            }
        }.exceptionOrNull()

        checkNotNull(error)
        assertTrue(error is IllegalStateException)
        assertTrue(error.message!!.contains("not packaged"))
    }

    private fun bootstrapArchive(vararg entries: Pair<String, String>): ByteArray {
        val output = ByteArrayOutputStream()
        XZCompressorOutputStream(output).use { xzOutput ->
            TarArchiveOutputStream(xzOutput).use { tarOutput ->
                tarOutput.setLongFileMode(TarArchiveOutputStream.LONGFILE_POSIX)
                entries.forEach { (name, contents) ->
                    val bytes = contents.toByteArray(StandardCharsets.UTF_8)
                    val entry = TarArchiveEntry(name).apply {
                        size = bytes.size.toLong()
                        mode = if (name.startsWith("bin/")) 0b111_101_101 else 0b110_100_100
                    }
                    tarOutput.putArchiveEntry(entry)
                    tarOutput.write(bytes)
                    tarOutput.closeArchiveEntry()
                }
                tarOutput.finish()
            }
        }
        return output.toByteArray()
    }
}

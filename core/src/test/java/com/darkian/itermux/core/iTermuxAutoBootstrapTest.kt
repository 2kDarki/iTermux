package com.darkian.itermux.core

import org.apache.commons.compress.archivers.tar.TarArchiveEntry
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream
import org.apache.commons.compress.compressors.xz.XZCompressorOutputStream
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.assertNull
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.nio.charset.StandardCharsets
import java.nio.file.Files

class iTermuxAutoBootstrapTest {
    @Test
    fun initializeCanAutoInstallPackagedBootstrap() {
        val runtime = iTermuxRuntimeInitializer.initialize(
            filesDir = Files.createTempDirectory("itermux-auto-bootstrap").toFile().absolutePath,
            hostPackageName = "com.darkian.host",
            isBootstrapPayloadPackaged = true,
            autoInstallBootstrap = true,
            bootstrapInstaller = { currentRuntime ->
                iTermux.installBootstrap(currentRuntime) {
                    ByteArrayInputStream(
                        bootstrapArchive(
                            "bin/sh" to "#!/bin/sh\necho embedded\n",
                            "etc/profile" to "export TERM=xterm-256color\n",
                        ),
                    )
                }
            },
        )

        assertFalse(runtime.isBootstrapRequired)
        assertEquals(iTermuxBootstrapState.READY, runtime.bootstrapState)
        assertNull(runtime.failureCause)
        assertEquals(
            "#!/bin/sh\necho embedded\n",
            java.io.File(runtime.paths.binDir, "sh").readText(),
        )
    }

    @Test
    fun initializeSkipsAutoInstallWhenNoPayloadIsPackaged() {
        var installerInvoked = false

        val runtime = iTermuxRuntimeInitializer.initialize(
            filesDir = Files.createTempDirectory("itermux-auto-bootstrap-skip").toFile().absolutePath,
            hostPackageName = "com.darkian.host",
            isBootstrapPayloadPackaged = false,
            autoInstallBootstrap = true,
            bootstrapInstaller = { currentRuntime ->
                installerInvoked = true
                currentRuntime
            },
        )

        assertTrue(runtime.isBootstrapRequired)
        assertEquals(iTermuxBootstrapState.UNINITIALIZED, runtime.bootstrapState)
        assertFalse(installerInvoked)
    }

    @Test
    fun initializeConvertsAutoInstallFailuresIntoFailedBootstrapState() {
        val runtime = iTermuxRuntimeInitializer.initialize(
            filesDir = Files.createTempDirectory("itermux-auto-bootstrap-failed").toFile().absolutePath,
            hostPackageName = "com.darkian.host",
            isBootstrapPayloadPackaged = true,
            autoInstallBootstrap = true,
            bootstrapInstaller = {
                error("simulated bootstrap extraction failure")
            },
        )

        assertTrue(runtime.isBootstrapRequired)
        assertEquals(iTermuxBootstrapState.FAILED, runtime.bootstrapState)
        assertEquals(iTermuxRuntimeFailureCause.BOOTSTRAP_EXTRACTION_FAILED, runtime.failureCause)
    }

    @Test
    fun initializeFailsEarlyWhenNoBootstrapVariantMatchesSupportedAbis() {
        var installerInvoked = false

        val runtime = iTermuxRuntimeInitializer.initialize(
            filesDir = Files.createTempDirectory("itermux-auto-bootstrap-unsupported-abi").toFile().absolutePath,
            hostPackageName = "com.darkian.host",
            supportedAbis = listOf("x86", "mips"),
            isBootstrapPayloadPackaged = true,
            autoInstallBootstrap = true,
            bootstrapInstaller = { currentRuntime ->
                installerInvoked = true
                currentRuntime
            },
        )

        assertEquals(iTermuxBootstrapState.FAILED, runtime.bootstrapState)
        assertEquals(iTermuxRuntimeFailureCause.UNSUPPORTED_ABI, runtime.failureCause)
        assertEquals(listOf("x86", "mips"), runtime.supportedAbis)
        assertEquals(null, runtime.bootstrapVariantAbi)
        assertFalse(installerInvoked)
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

package com.darkian.itermux.proot

import com.darkian.itermux.core.iTermuxRuntimeInitializer
import com.darkian.itermux.core.iTermuxSessionMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.nio.file.Files

class iTermuxProotPluginTest {
    @Test
    fun createsProotSessionUsingSharedHostFacingContract() {
        val runtime = iTermuxRuntimeInitializer.initialize(
            filesDir = Files.createTempDirectory("itermux-proot-session").toFile().absolutePath,
            hostPackageName = "com.darkian.host",
            extraEnv = mapOf("HOST_ONLY" to "present"),
        )

        val session = runtime.createProotSession(
            distribution = iTermuxProotDistribution(
                name = "debian",
                rootfsPath = "/var/lib/proot-distro/debian",
            ),
            sessionId = "debian-main",
        )

        assertEquals("debian-main", session.id)
        assertEquals(iTermuxProotPlugin.BACKEND, session.backend)
        assertEquals(iTermuxSessionMode.LOGIN_SHELL, session.mode)
        assertEquals("${runtime.paths.binDir}/proot", session.shellSpec.executable)
        assertEquals(
            listOf(
                "--link2symlink",
                "-0",
                "-r",
                "/var/lib/proot-distro/debian",
                "-b",
                "/dev",
                "-b",
                "/proc",
                "-b",
                "/sys",
                "-w",
                "/root",
                "/bin/sh",
            ),
            session.shellSpec.arguments,
        )
        assertEquals("proot", session.shellSpec.environment["ITERMUX_SESSION_BACKEND"])
        assertEquals("debian", session.shellSpec.environment["PROOT_DISTRO_NAME"])
        assertFalse(session.shellSpec.environment.containsKey("HOST_ONLY"))
    }

    @Test
    fun allowsRuntimeEnvironmentInheritanceOnlyWhenExplicitlyRequested() {
        val runtime = iTermuxRuntimeInitializer.initialize(
            filesDir = Files.createTempDirectory("itermux-proot-inherit").toFile().absolutePath,
            hostPackageName = "com.darkian.host",
            extraEnv = mapOf("HOST_ONLY" to "present"),
        )

        val session = runtime.createProotSession(
            distribution = iTermuxProotDistribution(
                name = "debian",
                rootfsPath = "/var/lib/proot-distro/debian",
            ),
            sessionId = "debian-inherit",
            inheritRuntimeEnvironment = true,
        )

        assertEquals("present", session.shellSpec.environment["HOST_ONLY"])
        assertTrue(session.shellSpec.environment.containsKey("PREFIX"))
    }
}

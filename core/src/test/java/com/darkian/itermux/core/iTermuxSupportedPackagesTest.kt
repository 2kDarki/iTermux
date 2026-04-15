package com.darkian.itermux.core

import org.junit.Assert.assertEquals
import org.junit.Test
import java.nio.file.Files

class iTermuxSupportedPackagesTest {
    @Test
    fun parsesPackageListIgnoringBlankLinesAndComments() {
        val packages = iTermuxSupportedPackages.parse(
            """
            # Minimal runtime support
            bash

            coreutils
            # VCS
            git
            """.trimIndent(),
        )

        assertEquals(listOf("bash", "coreutils", "git"), packages)
    }

    @Test
    fun initializedRuntimeSurfacesConfiguredSupportedPackages() {
        val runtime = iTermuxRuntimeInitializer.initialize(
            filesDir = Files.createTempDirectory("itermux-supported").toFile().absolutePath,
            hostPackageName = "com.darkian.host",
            supportedPackages = listOf("bash", "coreutils", "git"),
        )

        assertEquals(listOf("bash", "coreutils", "git"), runtime.supportedPackages)
    }

    @Test
    fun refreshPreservesConfiguredSupportedPackages() {
        val runtime = iTermuxRuntimeInitializer.initialize(
            filesDir = Files.createTempDirectory("itermux-supported-refresh").toFile().absolutePath,
            hostPackageName = "com.darkian.host",
            supportedPackages = listOf("bash", "coreutils"),
        )

        val refreshed = iTermuxRuntimeInitializer.refresh(
            identity = runtime.identity,
            paths = runtime.paths,
            supportedPackages = runtime.supportedPackages,
        )

        assertEquals(listOf("bash", "coreutils"), refreshed.supportedPackages)
    }
}

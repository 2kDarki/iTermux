package com.darkian.itermux.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test
import java.nio.file.Files

class iTermuxBootstrapAssetsTest {
    @Test
    fun initializedRuntimeSurfacesBootstrapPayloadMetadata() {
        val runtime = iTermuxRuntimeInitializer.initialize(
            filesDir = Files.createTempDirectory("itermux-bootstrap-assets").toFile().absolutePath,
            hostPackageName = "com.darkian.host",
            bootstrapAssetPath = "itermux/bootstrap/bootstrap.tar.xz",
            isBootstrapPayloadPackaged = true,
        )

        assertEquals("itermux/bootstrap/bootstrap.tar.xz", runtime.bootstrapAssetPath)
        assertEquals(true, runtime.isBootstrapPayloadPackaged)
    }

    @Test
    fun refreshPreservesBootstrapPayloadMetadata() {
        val runtime = iTermuxRuntimeInitializer.initialize(
            filesDir = Files.createTempDirectory("itermux-bootstrap-assets-refresh").toFile().absolutePath,
            hostPackageName = "com.darkian.host",
            bootstrapAssetPath = "itermux/bootstrap/bootstrap.tar.xz",
            isBootstrapPayloadPackaged = false,
        )

        val refreshed = iTermuxRuntimeInitializer.refresh(
            identity = runtime.identity,
            paths = runtime.paths,
            supportedPackages = runtime.supportedPackages,
            bootstrapAssetPath = runtime.bootstrapAssetPath,
            isBootstrapPayloadPackaged = runtime.isBootstrapPayloadPackaged,
        )

        assertEquals("itermux/bootstrap/bootstrap.tar.xz", refreshed.bootstrapAssetPath)
        assertFalse(refreshed.isBootstrapPayloadPackaged)
    }
}

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
            supportedAbis = listOf("arm64-v8a"),
            isBootstrapPayloadPackaged = true,
        )

        assertEquals("arm64-v8a", runtime.bootstrapVariantAbi)
        assertEquals("itermux/bootstrap/arm64-v8a/bootstrap.tar.xz", runtime.bootstrapAssetPath)
        assertEquals(true, runtime.isBootstrapPayloadPackaged)
    }

    @Test
    fun refreshPreservesBootstrapPayloadMetadata() {
        val runtime = iTermuxRuntimeInitializer.initialize(
            filesDir = Files.createTempDirectory("itermux-bootstrap-assets-refresh").toFile().absolutePath,
            hostPackageName = "com.darkian.host",
            supportedAbis = listOf("x86_64"),
            isBootstrapPayloadPackaged = false,
        )

        val refreshed = iTermuxRuntimeInitializer.refresh(
            identity = runtime.identity,
            paths = runtime.paths,
            supportedPackages = runtime.supportedPackages,
            supportedAbis = runtime.supportedAbis,
            bootstrapAssetPath = runtime.bootstrapAssetPath,
            bootstrapVariantAbi = runtime.bootstrapVariantAbi,
            isBootstrapPayloadPackaged = runtime.isBootstrapPayloadPackaged,
        )

        assertEquals("x86_64", refreshed.bootstrapVariantAbi)
        assertEquals("itermux/bootstrap/x86_64/bootstrap.tar.xz", refreshed.bootstrapAssetPath)
        assertFalse(refreshed.isBootstrapPayloadPackaged)
    }
}

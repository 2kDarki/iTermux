package com.darkian.itermux.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class iTermuxBootstrapResolverTest {
    @Test
    fun resolvesFirstSupportedAbiVariantInDevicePreferenceOrder() {
        val variant = iTermuxBootstrapResolver.resolve(
            supportedAbis = listOf("x86_64", "arm64-v8a"),
            config = iTermuxConfig(),
        )

        checkNotNull(variant)
        assertEquals("x86_64", variant.abi)
        assertEquals("itermux/bootstrap/x86_64/bootstrap.tar.xz", variant.assetPath)
    }

    @Test
    fun returnsNullWhenNoSupportedVariantMatches() {
        val variant = iTermuxBootstrapResolver.resolve(
            supportedAbis = listOf("x86", "mips"),
            config = iTermuxConfig(),
        )

        assertNull(variant)
    }
}

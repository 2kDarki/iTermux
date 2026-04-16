package com.darkian.itermux.core

// INTERNAL-TERMUX MODIFIED - merge carefully

/**
 * Resolves the packaged bootstrap variant for a device ABI list.
 */
object iTermuxBootstrapResolver {
    fun resolve(
        supportedAbis: List<String>,
        config: iTermuxConfig = iTermuxConfig(),
    ): iTermuxBootstrapVariant? {
        if (supportedAbis.isEmpty()) {
            return null
        }

        return supportedAbis.firstNotNullOfOrNull { abi ->
            config.bootstrapVariants.firstOrNull { candidate ->
                candidate.abi.equals(abi, ignoreCase = true)
            }
        }
    }

    fun defaultVariants(): List<iTermuxBootstrapVariant> {
        return listOf(
            iTermuxBootstrapVariant(
                abi = "arm64-v8a",
                assetPath = "itermux/bootstrap/arm64-v8a/bootstrap.tar.xz",
            ),
            iTermuxBootstrapVariant(
                abi = "armeabi-v7a",
                assetPath = "itermux/bootstrap/armeabi-v7a/bootstrap.tar.xz",
            ),
            iTermuxBootstrapVariant(
                abi = "x86_64",
                assetPath = "itermux/bootstrap/x86_64/bootstrap.tar.xz",
            ),
        )
    }
}

data class iTermuxBootstrapVariant(
    val abi: String,
    val assetPath: String,
)

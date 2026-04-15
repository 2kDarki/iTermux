package com.darkian.itermux.core

import android.content.Context

// INTERNAL-TERMUX MODIFIED - merge carefully

/**
 * Public facade for the embeddable host-owned runtime.
 *
 * The initialization path now resolves host-derived paths, materializes the
 * runtime layout, and writes the derived shell environment file. Heavier
 * upstream Termux internals will be wired on top of this base incrementally.
 */
object iTermux {
    fun initialize(
        context: Context,
        config: iTermuxConfig = iTermuxConfig(),
        baseEnv: Map<String, String> = emptyMap(),
        extraEnv: Map<String, String> = emptyMap(),
        failSafe: Boolean = false,
    ): iTermuxRuntime {
        val supportedPackages = loadSupportedPackages(context)
        val bootstrapAssetPath = config.bootstrapAssetPath
        val isBootstrapPayloadPackaged = hasAsset(
            context = context,
            assetPath = bootstrapAssetPath,
        )
        return iTermuxRuntimeInitializer.initialize(
            filesDir = context.filesDir.absolutePath,
            hostPackageName = context.packageName,
            config = config,
            supportedPackages = supportedPackages,
            bootstrapAssetPath = bootstrapAssetPath,
            isBootstrapPayloadPackaged = isBootstrapPayloadPackaged,
            baseEnv = baseEnv,
            extraEnv = extraEnv,
            failSafe = failSafe,
        )
    }

    fun refresh(
        runtime: iTermuxRuntime,
        baseEnv: Map<String, String> = emptyMap(),
        extraEnv: Map<String, String> = emptyMap(),
        failSafe: Boolean = false,
    ): iTermuxRuntime {
        return iTermuxRuntimeInitializer.refresh(
            identity = runtime.identity,
            paths = runtime.paths,
            supportedPackages = runtime.supportedPackages,
            bootstrapAssetPath = runtime.bootstrapAssetPath,
            isBootstrapPayloadPackaged = runtime.isBootstrapPayloadPackaged,
            baseEnv = baseEnv,
            extraEnv = extraEnv,
            failSafe = failSafe,
        )
    }

    fun createSession(
        runtime: iTermuxRuntime,
        sessionId: String,
        shellBinary: String = "sh",
        baseEnv: Map<String, String> = emptyMap(),
        extraEnv: Map<String, String> = emptyMap(),
        workingDirectory: String = runtime.defaultWorkingDirectory,
        failSafe: Boolean = false,
    ): iTermuxSession {
        return runtime.createSession(
            sessionId = sessionId,
            shellBinary = shellBinary,
            baseEnv = baseEnv,
            extraEnv = extraEnv,
            workingDirectory = workingDirectory,
            failSafe = failSafe,
        )
    }

    private fun loadSupportedPackages(context: Context): List<String> {
        return runCatching {
            context.assets.open(iTermuxSupportedPackages.ASSET_PATH).use { stream ->
                iTermuxSupportedPackages.parse(stream)
            }
        }.getOrDefault(emptyList())
    }

    private fun hasAsset(context: Context, assetPath: String): Boolean {
        return runCatching {
            context.assets.open(assetPath).use { }
            true
        }.getOrDefault(false)
    }
}

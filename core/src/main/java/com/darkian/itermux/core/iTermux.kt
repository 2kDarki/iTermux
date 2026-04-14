package com.darkian.itermux.core

import android.content.Context

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
        return iTermuxRuntimeInitializer.initialize(
            filesDir = context.filesDir.absolutePath,
            hostPackageName = context.packageName,
            config = config,
            baseEnv = baseEnv,
            extraEnv = extraEnv,
            failSafe = failSafe,
        )
    }
}

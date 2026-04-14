package com.darkian.itermux.core

import android.content.Context

/**
 * Public facade for the future embeddable runtime.
 *
 * This currently owns only host-derived path resolution. Upstream Termux
 * internals will be wired to these paths incrementally in later slices.
 */
object iTermux {
    fun initialize(
        context: Context,
        config: iTermuxConfig = iTermuxConfig(),
    ): iTermuxPaths {
        return iTermuxPathResolver.resolve(
            filesDir = context.filesDir.absolutePath,
            hostPackageName = context.packageName,
            config = config,
        )
    }
}

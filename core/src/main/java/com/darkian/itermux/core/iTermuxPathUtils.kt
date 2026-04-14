package com.darkian.itermux.core
// INTERNAL-TERMUX MODIFIED - merge carefully

/**
 * POSIX-style path utilities for the host-owned iTermux runtime.
 *
 * These helpers intentionally avoid platform-specific desktop path behavior so
 * unit tests can run on non-Android hosts while preserving Android semantics.
 */
object iTermuxPathUtils {
    fun canonicalize(
        path: String,
        paths: iTermuxPaths,
        prefixForRelativePath: String? = null,
        expandAliases: Boolean = true,
    ): String {
        val aliasedPath = if (expandAliases) iTermuxPathAliases.expand(path, paths) else path
        val absolutePath = toAbsolutePath(
            path = aliasedPath,
            prefixForRelativePath = prefixForRelativePath,
        )

        return normalizePosixPath(absolutePath)
    }

    fun getMatchedAllowedWorkingDirectoryParent(
        path: String?,
        paths: iTermuxPaths,
        externalStorageRoot: String = "/storage/emulated/0",
    ): String {
        if (path.isNullOrEmpty()) {
            return paths.filesDir
        }

        val canonicalPath = canonicalize(
            path = path,
            paths = paths,
            expandAliases = true,
        )

        return when {
            isSameOrChild(canonicalPath, paths.storageHomeDir) -> paths.storageHomeDir
            isSameOrChild(canonicalPath, externalStorageRoot) -> externalStorageRoot
            isSameOrChild(canonicalPath, "/sdcard") -> "/sdcard"
            else -> paths.filesDir
        }
    }

    private fun toAbsolutePath(
        path: String,
        prefixForRelativePath: String?,
    ): String {
        if (path.startsWith("/")) {
            return path
        }

        val basePath = prefixForRelativePath ?: "/"
        return "${basePath.trimEnd('/')}/$path"
    }

    private fun normalizePosixPath(path: String): String {
        val segments = mutableListOf<String>()

        for (segment in path.split('/')) {
            when (segment) {
                "", "." -> Unit
                ".." -> if (segments.isNotEmpty()) {
                    segments.removeAt(segments.lastIndex)
                }
                else -> segments.add(segment)
            }
        }

        return "/" + segments.joinToString("/")
    }

    private fun isSameOrChild(path: String, parent: String): Boolean {
        val normalizedParent = normalizePosixPath(parent)
        return path == normalizedParent || path.startsWith("$normalizedParent/")
    }
}

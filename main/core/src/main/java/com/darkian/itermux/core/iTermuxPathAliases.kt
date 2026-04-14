package com.darkian.itermux.core

/**
 * Helpers for converting between textual Termux-style aliases and concrete paths.
 */
object iTermuxPathAliases {
    fun expandPrefix(path: String, paths: iTermuxPaths): String {
        return when {
            path == "\$PREFIX" -> paths.prefixDir
            path.startsWith("\$PREFIX/") -> "${paths.prefixDir}/${path.removePrefix("\$PREFIX/")}"
            else -> path
        }
    }

    fun expandHome(path: String, paths: iTermuxPaths): String {
        return when {
            path == "~" || path == "~/" -> paths.homeDir
            path.startsWith("~/") -> "${paths.homeDir}/${path.removePrefix("~/")}"
            else -> path
        }
    }

    fun expand(path: String, paths: iTermuxPaths): String {
        return expandHome(expandPrefix(path, paths), paths)
    }

    fun collapsePrefix(path: String, paths: iTermuxPaths): String {
        return when {
            path == paths.prefixDir -> "\$PREFIX"
            path.startsWith("${paths.prefixDir}/") -> "\$PREFIX/${path.removePrefix("${paths.prefixDir}/")}"
            else -> path
        }
    }

    fun collapseHome(path: String, paths: iTermuxPaths): String {
        return when {
            path == paths.homeDir -> "~"
            path.startsWith("${paths.homeDir}/") -> "~/${path.removePrefix("${paths.homeDir}/")}"
            else -> path
        }
    }

    fun collapse(path: String, paths: iTermuxPaths): String {
        return collapseHome(collapsePrefix(path, paths), paths)
    }

    fun expandAll(pathsToExpand: List<String>?, paths: iTermuxPaths): List<String>? {
        return pathsToExpand?.map { expand(it, paths) }
    }

    fun collapseAll(pathsToCollapse: List<String>?, paths: iTermuxPaths): List<String>? {
        return pathsToCollapse?.map { collapse(it, paths) }
    }
}

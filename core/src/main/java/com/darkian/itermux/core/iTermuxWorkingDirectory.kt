package com.darkian.itermux.core
// INTERNAL-TERMUX MODIFIED - merge carefully

import java.io.File

/**
 * Resolves the default shell working directory from raw runtime properties.
 */
object iTermuxWorkingDirectory {
    private const val DEFAULT_WORKING_DIRECTORY_KEY = "default-working-directory"

    fun resolve(
        paths: iTermuxPaths,
        properties: Map<String, String>,
    ): String {
        val configuredPath = properties[DEFAULT_WORKING_DIRECTORY_KEY]
        if (configuredPath.isNullOrBlank()) {
            return paths.homeDir
        }

        val directory = File(configuredPath)
        return if (directory.exists() && directory.isDirectory && directory.canRead()) {
            directory.absolutePath
        } else {
            paths.homeDir
        }
    }
}

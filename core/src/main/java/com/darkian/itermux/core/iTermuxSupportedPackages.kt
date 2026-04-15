package com.darkian.itermux.core
// INTERNAL-TERMUX MODIFIED - merge carefully

import java.io.InputStream

/**
 * Loads the controlled supported package scope for the embedded runtime.
 */
object iTermuxSupportedPackages {
    const val ASSET_PATH: String = "itermux/supported-packages.txt"

    fun parse(rawPackages: String): List<String> {
        return rawPackages
            .lineSequence()
            .map { it.trim() }
            .filter { it.isNotEmpty() && !it.startsWith("#") }
            .toList()
    }

    fun parse(stream: InputStream): List<String> {
        return stream.bufferedReader().use { reader -> parse(reader.readText()) }
    }
}

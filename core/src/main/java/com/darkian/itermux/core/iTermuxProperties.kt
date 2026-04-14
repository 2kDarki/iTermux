package com.darkian.itermux.core
// INTERNAL-TERMUX MODIFIED - merge carefully

import java.io.File
import java.io.FileInputStream
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets
import java.util.Properties

/**
 * Resolves and loads raw termux.properties files from the host-owned runtime.
 *
 * The upstream precedence rule is preserved: the primary file wins if it
 * exists and is readable, otherwise the secondary file is used.
 */
object iTermuxProperties {
    fun propertyFiles(paths: iTermuxPaths): List<String> {
        return listOf(
            paths.propertiesPrimaryFile,
            paths.propertiesSecondaryFile,
        )
    }

    fun findReadablePropertiesFile(paths: iTermuxPaths): File? {
        return propertyFiles(paths)
            .asSequence()
            .map(::File)
            .firstOrNull { file ->
                file.isFile && file.canRead()
            }
    }

    fun load(paths: iTermuxPaths): Map<String, String> {
        val file = findReadablePropertiesFile(paths) ?: return emptyMap()
        val properties = Properties()

        FileInputStream(file).use { input ->
            InputStreamReader(input, StandardCharsets.UTF_8).use { reader ->
                properties.load(reader)
            }
        }

        return buildMap {
            for (name in properties.stringPropertyNames()) {
                put(name, properties.getProperty(name))
            }
        }
    }
}

package com.darkian.itermux.core
// INTERNAL-TERMUX MODIFIED - merge carefully

/**
 * Canonical host-owned runtime paths derived from the embedding app.
 */
data class iTermuxPaths(
    val filesDir: String,
    val prefixDir: String,
    val binDir: String,
    val etcDir: String,
    val tmpDir: String,
    val varDir: String,
    val homeDir: String,
    val configHomeDir: String,
    val dataHomeDir: String,
    val storageHomeDir: String,
    val stagingPrefixDir: String,
    val appsDir: String,
    val configPrefixDir: String,
    val propertiesPrimaryFile: String,
    val propertiesSecondaryFile: String,
    val envFile: String,
    val envTempFile: String,
    val termuxAmSocketFile: String,
)

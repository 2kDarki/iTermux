package com.darkian.itermux.core

/**
 * Configuration for the host-owned runtime layout inside app storage.
 *
 * The defaults intentionally mirror the upstream Termux directory names while
 * deriving the root from the embedding app's files directory.
 */
data class iTermuxConfig(
    val usrDirName: String = "usr",
    val homeDirName: String = "home",
    val stagingUsrDirName: String = "usr-staging",
    val appsDirName: String = "apps",
    val configHomeDirName: String = ".config/termux",
    val dataHomeDirName: String = ".termux",
    val hostPackageNameOverride: String? = null,
)

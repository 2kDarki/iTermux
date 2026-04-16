package com.darkian.itermux.core
// INTERNAL-TERMUX MODIFIED - merge carefully

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
    val bootstrapAssetPath: String = "itermux/bootstrap/bootstrap.tar.xz",
    val supportedAbisOverride: List<String>? = null,
    val bootstrapVariants: List<iTermuxBootstrapVariant> = iTermuxBootstrapResolver.defaultVariants(),
    val hostPackageNameOverride: String? = null,
)

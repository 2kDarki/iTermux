package com.darkian.itermux.core

// INTERNAL-TERMUX MODIFIED - merge carefully

/**
 * Resolves the canonical runtime directories from a host app's files dir.
 */
object iTermuxPathResolver {
    fun resolve(
        filesDir: String,
        hostPackageName: String,
        config: iTermuxConfig = iTermuxConfig(),
    ): iTermuxPaths {
        val identity = iTermuxIdentityResolver.resolve(
            hostPackageName = hostPackageName,
            config = config,
        )
        return resolve(
            filesDir = filesDir,
            identity = identity,
            config = config,
        )
    }

    fun resolve(
        filesDir: String,
        identity: iTermuxIdentity,
        config: iTermuxConfig = iTermuxConfig(),
    ): iTermuxPaths {
        val normalizedFilesDir = filesDir.trimEnd('/', '\\')

        val prefixDir = normalizedFilesDir.child(config.usrDirName)
        val homeDir = normalizedFilesDir.child(config.homeDirName)
        val configHomeDir = homeDir.child(config.configHomeDirName)
        val dataHomeDir = homeDir.child(config.dataHomeDirName)
        val appsDir = normalizedFilesDir.child(config.appsDirName)
        val configPrefixDir = prefixDir.child("etc/termux")

        return iTermuxPaths(
            filesDir = normalizedFilesDir,
            prefixDir = prefixDir,
            binDir = prefixDir.child("bin"),
            etcDir = prefixDir.child("etc"),
            tmpDir = prefixDir.child("tmp"),
            varDir = prefixDir.child("var"),
            homeDir = homeDir,
            configHomeDir = configHomeDir,
            dataHomeDir = dataHomeDir,
            storageHomeDir = homeDir.child("storage"),
            stagingPrefixDir = normalizedFilesDir.child(config.stagingUsrDirName),
            appsDir = appsDir,
            configPrefixDir = configPrefixDir,
            propertiesPrimaryFile = dataHomeDir.child("termux.properties"),
            propertiesSecondaryFile = configHomeDir.child("termux.properties"),
            envFile = configPrefixDir.child("termux.env"),
            envTempFile = configPrefixDir.child("termux.env.tmp"),
            termuxAmSocketFile = appsDir.child("${identity.packageName}/termux-am/am.sock"),
        )
    }

    private fun String.child(name: String): String = "$this/$name"
}

package com.darkian.itermux.core

import org.junit.Assert.assertEquals
import org.junit.Test

class iTermuxPathResolverTest {
    @Test
    fun resolvesCanonicalPathsFromHostFilesDir() {
        val paths = iTermuxPathResolver.resolve(
            filesDir = "C:/data/user/0/com.darkian.itermux/files",
            hostPackageName = "com.darkian.itermux",
        )

        assertEquals("C:/data/user/0/com.darkian.itermux/files", paths.filesDir)
        assertEquals("C:/data/user/0/com.darkian.itermux/files/usr", paths.prefixDir)
        assertEquals("C:/data/user/0/com.darkian.itermux/files/usr/bin", paths.binDir)
        assertEquals("C:/data/user/0/com.darkian.itermux/files/usr/etc", paths.etcDir)
        assertEquals("C:/data/user/0/com.darkian.itermux/files/usr/tmp", paths.tmpDir)
        assertEquals("C:/data/user/0/com.darkian.itermux/files/usr/var", paths.varDir)
        assertEquals("C:/data/user/0/com.darkian.itermux/files/home", paths.homeDir)
        assertEquals("C:/data/user/0/com.darkian.itermux/files/home/.config/termux", paths.configHomeDir)
        assertEquals("C:/data/user/0/com.darkian.itermux/files/home/.termux", paths.dataHomeDir)
        assertEquals("C:/data/user/0/com.darkian.itermux/files/home/storage", paths.storageHomeDir)
        assertEquals("C:/data/user/0/com.darkian.itermux/files/usr-staging", paths.stagingPrefixDir)
        assertEquals("C:/data/user/0/com.darkian.itermux/files/apps", paths.appsDir)
        assertEquals("C:/data/user/0/com.darkian.itermux/files/usr/etc/termux", paths.configPrefixDir)
        assertEquals("C:/data/user/0/com.darkian.itermux/files/home/.termux/termux.properties", paths.propertiesPrimaryFile)
        assertEquals("C:/data/user/0/com.darkian.itermux/files/home/.config/termux/termux.properties", paths.propertiesSecondaryFile)
        assertEquals("C:/data/user/0/com.darkian.itermux/files/usr/etc/termux/termux.env", paths.envFile)
        assertEquals("C:/data/user/0/com.darkian.itermux/files/usr/etc/termux/termux.env.tmp", paths.envTempFile)
        assertEquals("C:/data/user/0/com.darkian.itermux/files/apps/com.darkian.itermux/termux-am/am.sock", paths.termuxAmSocketFile)
    }

    @Test
    fun trimsTrailingSlashesBeforeResolvingChildren() {
        val paths = iTermuxPathResolver.resolve(
            filesDir = "/data/data/com.darkian.itermux/files/",
            hostPackageName = "com.darkian.itermux",
        )

        assertEquals("/data/data/com.darkian.itermux/files", paths.filesDir)
        assertEquals("/data/data/com.darkian.itermux/files/usr", paths.prefixDir)
        assertEquals("/data/data/com.darkian.itermux/files/home", paths.homeDir)
        assertEquals("/data/data/com.darkian.itermux/files/usr/etc/termux/termux.env", paths.envFile)
    }

    @Test
    fun supportsCustomDirectoryNames() {
        val config = iTermuxConfig(
            usrDirName = "runtime-usr",
            homeDirName = "runtime-home",
            stagingUsrDirName = "runtime-stage",
            appsDirName = "runtime-apps",
            configHomeDirName = ".config/runtime",
            dataHomeDirName = ".runtime",
        )

        val paths = iTermuxPathResolver.resolve(
            filesDir = "/app/files",
            hostPackageName = "com.darkian.itermux.host",
            config = config,
        )

        assertEquals("/app/files/runtime-usr", paths.prefixDir)
        assertEquals("/app/files/runtime-home", paths.homeDir)
        assertEquals("/app/files/runtime-home/.config/runtime", paths.configHomeDir)
        assertEquals("/app/files/runtime-home/.runtime", paths.dataHomeDir)
        assertEquals("/app/files/runtime-stage", paths.stagingPrefixDir)
        assertEquals("/app/files/runtime-apps", paths.appsDir)
        assertEquals("/app/files/runtime-home/.runtime/termux.properties", paths.propertiesPrimaryFile)
        assertEquals("/app/files/runtime-home/.config/runtime/termux.properties", paths.propertiesSecondaryFile)
        assertEquals("/app/files/runtime-apps/com.darkian.itermux.host/termux-am/am.sock", paths.termuxAmSocketFile)
    }
}

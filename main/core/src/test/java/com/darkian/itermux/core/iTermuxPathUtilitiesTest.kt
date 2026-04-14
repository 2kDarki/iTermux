package com.darkian.itermux.core

import org.junit.Assert.assertEquals
import org.junit.Test

class iTermuxPathUtilitiesTest {
    private val paths = iTermuxPathResolver.resolve(
        filesDir = "/app/files",
        hostPackageName = "com.darkian.host",
    )

    @Test
    fun canonicalizesExpandedPrefixPaths() {
        assertEquals(
            "/app/files/usr/etc/termux/termux.env",
            iTermuxPathUtils.canonicalize(
                path = "\$PREFIX/bin/../etc/termux/./termux.env",
                paths = paths,
                expandAliases = true,
            ),
        )
    }

    @Test
    fun canonicalizesRelativePathsAgainstProvidedParent() {
        assertEquals(
            "/app/files/home/projects/demo",
            iTermuxPathUtils.canonicalize(
                path = "./projects/demo",
                paths = paths,
                prefixForRelativePath = paths.homeDir,
                expandAliases = false,
            ),
        )
    }

    @Test
    fun matchesStorageHomeAsAllowedWorkingDirectoryParent() {
        assertEquals(
            paths.storageHomeDir,
            iTermuxPathUtils.getMatchedAllowedWorkingDirectoryParent(
                path = "${paths.storageHomeDir}/downloads/archive.tar.xz",
                paths = paths,
            ),
        )
    }

    @Test
    fun matchesExternalStorageAsAllowedWorkingDirectoryParent() {
        assertEquals(
            "/storage/emulated/0",
            iTermuxPathUtils.getMatchedAllowedWorkingDirectoryParent(
                path = "/storage/emulated/0/Download/archive.tar.xz",
                paths = paths,
            ),
        )
    }

    @Test
    fun matchesSdcardAliasAsAllowedWorkingDirectoryParent() {
        assertEquals(
            "/sdcard",
            iTermuxPathUtils.getMatchedAllowedWorkingDirectoryParent(
                path = "/sdcard/Download/archive.tar.xz",
                paths = paths,
            ),
        )
    }

    @Test
    fun fallsBackToFilesDirForDisallowedWorkingDirectoryParent() {
        assertEquals(
            paths.filesDir,
            iTermuxPathUtils.getMatchedAllowedWorkingDirectoryParent(
                path = "/system/bin/sh",
                paths = paths,
            ),
        )
    }
}

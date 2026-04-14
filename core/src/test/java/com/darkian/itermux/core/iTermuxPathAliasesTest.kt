package com.darkian.itermux.core

import org.junit.Assert.assertEquals
import org.junit.Test

class iTermuxPathAliasesTest {
    private val paths = iTermuxPathResolver.resolve(
        filesDir = "/app/files",
        hostPackageName = "com.darkian.itermux",
    )

    @Test
    fun expandsBarePrefixAlias() {
        assertEquals("/app/files/usr", iTermuxPathAliases.expandPrefix("\$PREFIX", paths))
    }

    @Test
    fun expandsChildPrefixAlias() {
        assertEquals(
            "/app/files/usr/bin/bash",
            iTermuxPathAliases.expandPrefix("\$PREFIX/bin/bash", paths),
        )
    }

    @Test
    fun expandsBareHomeAlias() {
        assertEquals("/app/files/home", iTermuxPathAliases.expandHome("~", paths))
    }

    @Test
    fun expandsChildHomeAlias() {
        assertEquals(
            "/app/files/home/.bashrc",
            iTermuxPathAliases.expandHome("~/.bashrc", paths),
        )
    }

    @Test
    fun leavesUnrelatedPathUntouchedOnExpand() {
        assertEquals("/tmp/example", iTermuxPathAliases.expandPrefix("/tmp/example", paths))
    }

    @Test
    fun collapsesConcretePrefixRoot() {
        assertEquals("\$PREFIX", iTermuxPathAliases.collapsePrefix("/app/files/usr", paths))
    }

    @Test
    fun collapsesConcretePrefixChild() {
        assertEquals(
            "\$PREFIX/etc/termux/termux.env",
            iTermuxPathAliases.collapsePrefix("/app/files/usr/etc/termux/termux.env", paths),
        )
    }

    @Test
    fun collapsesConcreteHomeRoot() {
        assertEquals("~", iTermuxPathAliases.collapseHome("/app/files/home", paths))
    }

    @Test
    fun collapsesConcreteHomeChild() {
        assertEquals(
            "~/.config/termux/termux.properties",
            iTermuxPathAliases.collapseHome("/app/files/home/.config/termux/termux.properties", paths),
        )
    }

    @Test
    fun leavesUnrelatedPathUntouchedOnCollapse() {
        assertEquals("/tmp/example", iTermuxPathAliases.collapsePrefix("/tmp/example", paths))
    }

    @Test
    fun expandsAllKnownAliasesInList() {
        assertEquals(
            listOf(
                "/app/files/usr/bin/bash",
                "/app/files/home/.bashrc",
                "/tmp/example",
            ),
            iTermuxPathAliases.expandAll(
                listOf("\$PREFIX/bin/bash", "~/.bashrc", "/tmp/example"),
                paths,
            ),
        )
    }

    @Test
    fun collapsesAllKnownAliasesInList() {
        assertEquals(
            listOf(
                "\$PREFIX/bin/bash",
                "~/.bashrc",
                "/tmp/example",
            ),
            iTermuxPathAliases.collapseAll(
                listOf("/app/files/usr/bin/bash", "/app/files/home/.bashrc", "/tmp/example"),
                paths,
            ),
        )
    }
}

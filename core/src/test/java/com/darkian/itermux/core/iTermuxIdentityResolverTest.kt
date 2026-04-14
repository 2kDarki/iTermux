package com.darkian.itermux.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class iTermuxIdentityResolverTest {
    @Test
    fun derivesAuthoritiesClassesActionsAndPluginPackagesFromHostPackage() {
        val identity = iTermuxIdentityResolver.resolve("com.darkian.host")

        assertEquals("com.darkian.host", identity.packageName)
        assertEquals("com.darkian.host.documents", identity.documentsAuthority)
        assertEquals("com.darkian.host.files", identity.filesAuthority)
        assertEquals("com.darkian.host.BuildConfig", identity.buildConfigClassName)
        assertEquals(
            "com.darkian.host.app.api.file.FileShareReceiverActivity",
            identity.fileShareReceiverActivityClassName,
        )
        assertEquals(
            "com.darkian.host.app.api.file.FileViewReceiverActivity",
            identity.fileViewReceiverActivityClassName,
        )
        assertEquals("com.darkian.host.app.TermuxActivity", identity.termuxActivityName)
        assertEquals(
            "com.darkian.host.app.activities.SettingsActivity",
            identity.settingsActivityName,
        )
        assertEquals("com.darkian.host.app.TermuxService", identity.serviceName)
        assertEquals("com.darkian.host.app.failsafe_session", identity.failSafeSessionExtra)
        assertEquals("com.darkian.host.app.notify_app_crash", identity.notifyAppCrashAction)
        assertEquals("com.darkian.host.app.reload_style", identity.reloadStyleAction)
        assertEquals(
            "com.darkian.host.app.TermuxActivity.EXTRA_RECREATE_ACTIVITY",
            identity.recreateActivityExtra,
        )
        assertEquals(
            "com.darkian.host.app.request_storage_permissions",
            identity.requestPermissionsAction,
        )
        assertEquals("com.darkian.host.service_stop", identity.stopServiceAction)
        assertEquals("com.darkian.host.service_execute", identity.serviceExecuteAction)
        assertEquals("com.darkian.host.file", identity.serviceExecuteUriScheme)
        assertEquals("com.darkian.host.execute.arguments", identity.executeArgumentsExtra)
        assertEquals("com.darkian.host.execute.stdin", identity.executeStdinExtra)
        assertEquals("com.darkian.host.execute.cwd", identity.executeWorkingDirectoryExtra)
        assertEquals("com.darkian.host.api", identity.pluginPackageNames.api)
        assertEquals("com.darkian.host.boot", identity.pluginPackageNames.boot)
        assertEquals("com.darkian.host.window", identity.pluginPackageNames.float)
        assertEquals("com.darkian.host.styling", identity.pluginPackageNames.styling)
        assertEquals("com.darkian.host.tasker", identity.pluginPackageNames.tasker)
        assertEquals("com.darkian.host.widget", identity.pluginPackageNames.widget)

        identity.allValues().forEach { value ->
            assertFalse(value.contains("com.termux"))
        }
    }

    @Test
    fun honorsConfiguredPackageOverride() {
        val identity = iTermuxIdentityResolver.resolve(
            hostPackageName = "com.darkian.host",
            config = iTermuxConfig(hostPackageNameOverride = "com.darkian.override"),
        )

        assertEquals("com.darkian.override", identity.packageName)
        assertEquals("com.darkian.override.files", identity.filesAuthority)
        assertEquals("com.darkian.override.app.TermuxService", identity.serviceName)
        assertEquals("com.darkian.override.widget", identity.pluginPackageNames.widget)
    }
}

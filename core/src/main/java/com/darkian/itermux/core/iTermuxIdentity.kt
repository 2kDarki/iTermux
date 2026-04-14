package com.darkian.itermux.core

// INTERNAL-TERMUX MODIFIED - merge carefully

/**
 * Host-derived runtime identity adapted from the upstream TermuxConstants
 * package and action naming seams.
 */
data class iTermuxIdentity(
    val packageName: String,
    val documentsAuthority: String,
    val filesAuthority: String,
    val buildConfigClassName: String,
    val fileShareReceiverActivityClassName: String,
    val fileViewReceiverActivityClassName: String,
    val termuxActivityName: String,
    val settingsActivityName: String,
    val serviceName: String,
    val failSafeSessionExtra: String,
    val notifyAppCrashAction: String,
    val reloadStyleAction: String,
    val recreateActivityExtra: String,
    val requestPermissionsAction: String,
    val stopServiceAction: String,
    val serviceExecuteAction: String,
    val serviceExecuteUriScheme: String,
    val executeArgumentsExtra: String,
    val executeStdinExtra: String,
    val executeWorkingDirectoryExtra: String,
    val pluginPackageNames: iTermuxPluginPackageNames,
) {
    fun allValues(): List<String> {
        return buildList {
            add(packageName)
            add(documentsAuthority)
            add(filesAuthority)
            add(buildConfigClassName)
            add(fileShareReceiverActivityClassName)
            add(fileViewReceiverActivityClassName)
            add(termuxActivityName)
            add(settingsActivityName)
            add(serviceName)
            add(failSafeSessionExtra)
            add(notifyAppCrashAction)
            add(reloadStyleAction)
            add(recreateActivityExtra)
            add(requestPermissionsAction)
            add(stopServiceAction)
            add(serviceExecuteAction)
            add(serviceExecuteUriScheme)
            add(executeArgumentsExtra)
            add(executeStdinExtra)
            add(executeWorkingDirectoryExtra)
            addAll(pluginPackageNames.allValues())
        }
    }
}

data class iTermuxPluginPackageNames(
    val api: String,
    val boot: String,
    val float: String,
    val styling: String,
    val tasker: String,
    val widget: String,
) {
    fun allValues(): List<String> {
        return listOf(api, boot, float, styling, tasker, widget)
    }
}

object iTermuxIdentityResolver {
    fun resolve(
        hostPackageName: String,
        config: iTermuxConfig = iTermuxConfig(),
    ): iTermuxIdentity {
        val packageName = config.hostPackageNameOverride ?: hostPackageName

        return iTermuxIdentity(
            packageName = packageName,
            documentsAuthority = "$packageName.documents",
            filesAuthority = "$packageName.files",
            buildConfigClassName = "$packageName.BuildConfig",
            fileShareReceiverActivityClassName = "$packageName.app.api.file.FileShareReceiverActivity",
            fileViewReceiverActivityClassName = "$packageName.app.api.file.FileViewReceiverActivity",
            termuxActivityName = "$packageName.app.TermuxActivity",
            settingsActivityName = "$packageName.app.activities.SettingsActivity",
            serviceName = "$packageName.app.TermuxService",
            failSafeSessionExtra = "$packageName.app.failsafe_session",
            notifyAppCrashAction = "$packageName.app.notify_app_crash",
            reloadStyleAction = "$packageName.app.reload_style",
            recreateActivityExtra = "$packageName.app.TermuxActivity.EXTRA_RECREATE_ACTIVITY",
            requestPermissionsAction = "$packageName.app.request_storage_permissions",
            stopServiceAction = "$packageName.service_stop",
            serviceExecuteAction = "$packageName.service_execute",
            serviceExecuteUriScheme = "$packageName.file",
            executeArgumentsExtra = "$packageName.execute.arguments",
            executeStdinExtra = "$packageName.execute.stdin",
            executeWorkingDirectoryExtra = "$packageName.execute.cwd",
            pluginPackageNames = iTermuxPluginPackageNames(
                api = "$packageName.api",
                boot = "$packageName.boot",
                float = "$packageName.window",
                styling = "$packageName.styling",
                tasker = "$packageName.tasker",
                widget = "$packageName.widget",
            ),
        )
    }

    fun resolve(paths: iTermuxPaths): iTermuxIdentity {
        val packageName = resolvePackageName(paths)
        return resolve(packageName)
    }

    private fun resolvePackageName(paths: iTermuxPaths): String {
        val appsPrefix = paths.appsDir.replace('\\', '/').trimEnd('/') + "/"
        val socketPath = paths.termuxAmSocketFile.replace('\\', '/')

        require(socketPath.startsWith(appsPrefix)) {
            "Unable to derive runtime package name from socket path: $socketPath"
        }

        return socketPath.removePrefix(appsPrefix).substringBefore('/')
    }
}

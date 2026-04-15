package com.darkian.itermux.sample

import android.app.Activity
import android.os.Bundle
import android.widget.TextView
import com.darkian.itermux.core.iTermux
import com.darkian.itermux.proot.createProotSession
import com.darkian.itermux.proot.iTermuxProotDistribution
import java.io.File

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val runtime = iTermux.initialize(this)
        val nativeSession = iTermux.createSession(runtime, sessionId = "sample")
        val prootSession = runtime.createProotSession(
            distribution = iTermuxProotDistribution(
                name = "debian",
                rootfsPath = File(runtime.paths.filesDir, "proot/debian-rootfs").absolutePath,
            ),
            sessionId = "sample-proot",
        )
        val message = buildString {
            append("internal-termux sample host\n\n")
            append("Initialized host-owned runtime.\n")
            append("filesDir: ")
            append(runtime.paths.filesDir)
            append("\npackageName: ")
            append(runtime.identity.packageName)
            append("\nprefixDir: ")
            append(runtime.paths.prefixDir)
            append("\nhomeDir: ")
            append(runtime.paths.homeDir)
            append("\nfilesAuthority: ")
            append(runtime.identity.filesAuthority)
            append("\nenvFile: ")
            append(runtime.paths.envFile)
            append("\npropertiesFile: ")
            append(runtime.selectedPropertiesFile ?: "<none>")
            append("\ndefaultWorkingDirectory: ")
            append(runtime.defaultWorkingDirectory)
            append("\nbootstrapRequired: ")
            append(runtime.isBootstrapRequired)
            append("\nserviceExecuteAction: ")
            append(runtime.identity.serviceExecuteAction)
            append("\nsupportedPackages: ")
            append(runtime.supportedPackages.joinToString(", ").ifEmpty { "<none>" })
            append("\nbootstrapAssetPath: ")
            append(runtime.bootstrapAssetPath)
            append("\nbootstrapPayloadPackaged: ")
            append(runtime.isBootstrapPayloadPackaged)
            append("\nnativeSessionId: ")
            append(nativeSession.id)
            append("\nnativeBackend: ")
            append(nativeSession.backend.id)
            append("\nnativeSessionMode: ")
            append(nativeSession.mode)
            append("\nnativeExecutable: ")
            append(nativeSession.shellSpec.executable)
            append("\nprootSessionId: ")
            append(prootSession.id)
            append("\nprootBackend: ")
            append(prootSession.backend.id)
            append("\nprootExecutable: ")
            append(prootSession.shellSpec.executable)
            append("\nprootArguments: ")
            append(prootSession.shellSpec.arguments.joinToString(" "))
            append("\nPATH: ")
            append(runtime.environment["PATH"])
        }

        setContentView(
            TextView(this).apply {
                text = message
                textSize = 16f
                setPadding(48, 48, 48, 48)
            }
        )
    }
}

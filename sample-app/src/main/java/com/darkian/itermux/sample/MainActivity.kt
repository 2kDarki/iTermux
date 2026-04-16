package com.darkian.itermux.sample

import android.app.Activity
import android.os.Bundle
import android.widget.TextView
import com.darkian.itermux.core.iTermux
import com.darkian.itermux.core.iTermuxConfig
import com.darkian.itermux.core.iTermuxSessionState
import com.darkian.itermux.proot.createProotSession
import com.darkian.itermux.proot.iTermuxProotDistribution
import java.io.File

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val lifecycleRecorder = iTermuxDsLifecycleRecorder()
        iTermux.addLifecycleListener(lifecycleRecorder.listener)

        val runtime = iTermux.initialize(
            this,
            config = iTermuxConfig(prootEnabled = true),
        )
        val nativeSession = iTermux.createSession(runtime, sessionId = "sample")
        val recoveredNativeSession = iTermux.recoverSessionFromOsKill(nativeSession) { killed ->
            killed.copy(
                state = iTermuxSessionState.READY,
                recoveryAttempts = killed.recoveryAttempts + 1,
                failureCause = null,
            )
        }
        val prootSession = runtime.createProotSession(
            distribution = iTermuxProotDistribution(
                name = "debian",
                rootfsPath = File(runtime.paths.filesDir, "proot/debian-rootfs").absolutePath,
            ),
            sessionId = "sample-proot",
        )
        lifecycleRecorder.recordSessionSnapshot(prootSession)
        iTermux.removeLifecycleListener(lifecycleRecorder.listener)

        val message = buildString {
            append("internal-termux sample host\n\n")
            append("Minimal DS spike over the iTermux lifecycle contract.\n")
            append("normalizedRuntimeState: ")
            append(lifecycleRecorder.normalizedState)
            append("\nlastBootstrapState: ")
            append(lifecycleRecorder.lastBootstrapState ?: "<none>")
            append("\nlastBootstrapFailure: ")
            append(lifecycleRecorder.lastBootstrapFailureCause ?: "<none>")
            append("\nlastValidationResult: ")
            append(lifecycleRecorder.lastEnvironmentValidation ?: "<none>")
            append("\nlastDegradedCause: ")
            append(lifecycleRecorder.lastDegradedCause ?: "<none>")
            append("\n\nInitialized host-owned runtime.\n")
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
            append(recoveredNativeSession.id)
            append("\nnativeBackend: ")
            append(recoveredNativeSession.backend.id)
            append("\nnativeSessionMode: ")
            append(recoveredNativeSession.mode)
            append("\nnativeSessionState: ")
            append(recoveredNativeSession.state)
            append("\nnativeRecoveryAttempts: ")
            append(recoveredNativeSession.recoveryAttempts)
            append("\nnativeFailureCause: ")
            append(recoveredNativeSession.failureCause ?: "<none>")
            append("\nnativeExecutable: ")
            append(recoveredNativeSession.shellSpec.executable)
            append("\nprootSessionId: ")
            append(prootSession.id)
            append("\nprootBackend: ")
            append(prootSession.backend.id)
            append("\nprootSessionState: ")
            append(prootSession.state)
            append("\nprootFailureCause: ")
            append(prootSession.failureCause ?: "<none>")
            append("\nprootExecutable: ")
            append(prootSession.shellSpec.executable)
            append("\nprootArguments: ")
            append(prootSession.shellSpec.arguments.joinToString(" "))
            append("\nPATH: ")
            append(runtime.environment["PATH"])
            append("\n\nLifecycle timeline:\n")
            lifecycleRecorder.eventLines().forEachIndexed { index, line ->
                append(index + 1)
                append(". ")
                append(line)
                append('\n')
            }
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

package com.darkian.itermux.core

import android.content.Context
import android.os.Build
import android.util.Log
import java.io.InputStream

// INTERNAL-TERMUX MODIFIED - merge carefully

/**
 * Public facade for the embeddable host-owned runtime.
 *
 * The initialization path now resolves host-derived paths, materializes the
 * runtime layout, and writes the derived shell environment file. Heavier
 * upstream Termux internals will be wired on top of this base incrementally.
 */
object iTermux {
    private const val TAG = "iTermux"

    fun addLifecycleListener(listener: iTermuxRuntimeLifecycleListener) {
        iTermuxLifecycleRegistry.addListener(listener)
    }

    fun removeLifecycleListener(listener: iTermuxRuntimeLifecycleListener) {
        iTermuxLifecycleRegistry.removeListener(listener)
    }

    fun initialize(
        context: Context,
        config: iTermuxConfig = iTermuxConfig(),
        baseEnv: Map<String, String> = emptyMap(),
        extraEnv: Map<String, String> = emptyMap(),
        failSafe: Boolean = false,
    ): iTermuxRuntime {
        val supportedPackages = loadSupportedPackages(context)
        val supportedAbis = config.supportedAbisOverride ?: Build.SUPPORTED_ABIS.toList()
        val bootstrapVariant = iTermuxBootstrapResolver.resolve(
            supportedAbis = supportedAbis,
            config = config,
        )
        val bootstrapAssetPath = bootstrapVariant?.assetPath ?: config.bootstrapAssetPath
        if (bootstrapVariant != null) {
            Log.i(
                TAG,
                "Resolved bootstrap variant ${bootstrapVariant.abi} to $bootstrapAssetPath for device ABIs $supportedAbis",
            )
        } else {
            Log.w(
                TAG,
                "No packaged bootstrap variant matched device ABIs $supportedAbis",
            )
        }
        iTermuxLifecycleRegistry.configure(config.lifecycleCallbackThread)
        val isBootstrapPayloadPackaged = hasAsset(
            context = context,
            assetPath = bootstrapAssetPath,
        )
        return iTermuxRuntimeInitializer.initialize(
            filesDir = context.filesDir.absolutePath,
            hostPackageName = context.packageName,
            config = config,
            supportedPackages = supportedPackages,
            supportedAbis = supportedAbis,
            bootstrapAssetPath = bootstrapAssetPath,
            isBootstrapPayloadPackaged = isBootstrapPayloadPackaged,
            baseEnv = baseEnv,
            extraEnv = extraEnv,
            failSafe = failSafe,
            autoInstallBootstrap = true,
            bootstrapInstaller = { runtime ->
                installBootstrap(runtime) {
                    context.assets.open(runtime.bootstrapAssetPath)
                }
            },
            bootstrapStateObserver = iTermuxLifecycleRegistry::dispatchBootstrapState,
            environmentValidationObserver = iTermuxLifecycleRegistry::dispatchEnvironmentValidation,
        )
    }

    fun refresh(
        runtime: iTermuxRuntime,
        baseEnv: Map<String, String> = emptyMap(),
        extraEnv: Map<String, String> = emptyMap(),
        failSafe: Boolean = false,
    ): iTermuxRuntime {
        return iTermuxRuntimeInitializer.refresh(
            identity = runtime.identity,
            paths = runtime.paths,
            supportedPackages = runtime.supportedPackages,
            isProotEnabled = runtime.isProotEnabled,
            supportedAbis = runtime.supportedAbis,
            bootstrapAssetPath = runtime.bootstrapAssetPath,
            bootstrapVariantAbi = runtime.bootstrapVariantAbi,
            isBootstrapPayloadPackaged = runtime.isBootstrapPayloadPackaged,
            baseEnv = baseEnv,
            extraEnv = extraEnv,
            failSafe = failSafe,
            bootstrapStateObserver = iTermuxLifecycleRegistry::dispatchBootstrapState,
            environmentValidationObserver = iTermuxLifecycleRegistry::dispatchEnvironmentValidation,
        )
    }

    fun createSession(
        runtime: iTermuxRuntime,
        sessionId: String,
        shellBinary: String = "sh",
        baseEnv: Map<String, String> = emptyMap(),
        extraEnv: Map<String, String> = emptyMap(),
        workingDirectory: String = runtime.defaultWorkingDirectory,
        failSafe: Boolean = false,
    ): iTermuxSession {
        return iTermuxSessionController.start(
            sessionId = sessionId,
            stateObserver = iTermuxLifecycleRegistry::dispatchSessionState,
            sessionFactory = {
                runtime.createSession(
                    sessionId = sessionId,
                    shellBinary = shellBinary,
                    baseEnv = baseEnv,
                    extraEnv = extraEnv,
                    workingDirectory = workingDirectory,
                    failSafe = failSafe,
                )
            },
            failureSessionFactory = {
                iTermuxSession(
                    id = sessionId,
                    backend = iTermuxSessionBackends.NATIVE,
                    mode = iTermuxSessionMode.LOGIN_SHELL,
                    shellSpec = iTermuxShellSpec(
                        executable = "${runtime.paths.binDir}/$shellBinary",
                        arguments = emptyList(),
                        workingDirectory = workingDirectory,
                        environment = runtime.environment,
                    ),
                    state = iTermuxSessionState.DEAD,
                    failureCause = iTermuxRuntimeFailureCause.SESSION_START_FAILED,
                )
            },
        )
    }

    fun suspendSession(session: iTermuxSession): iTermuxSession {
        return iTermuxSessionController.suspend(
            session = session,
            stateObserver = iTermuxLifecycleRegistry::dispatchSessionState,
        )
    }

    fun resumeSession(session: iTermuxSession): iTermuxSession {
        return iTermuxSessionController.resume(
            session = session,
            stateObserver = iTermuxLifecycleRegistry::dispatchSessionState,
        )
    }

    fun recoverSessionFromOsKill(
        session: iTermuxSession,
        restartSession: (iTermuxSession) -> iTermuxSession?,
    ): iTermuxSession {
        return iTermuxSessionController.recoverFromOsKill(
            session = session,
            restartSession = restartSession,
            stateObserver = iTermuxLifecycleRegistry::dispatchSessionState,
        )
    }

    fun installBootstrap(
        runtime: iTermuxRuntime,
        openPayload: () -> InputStream,
    ): iTermuxRuntime {
        val result = runCatching {
            iTermuxBootstrapInstaller.install(
                runtime = runtime,
                openPayload = openPayload,
            )
        }.getOrElse {
            runtime.copy(
                bootstrapState = iTermuxBootstrapState.FAILED,
                failureCause = iTermuxRuntimeFailureCause.BOOTSTRAP_EXTRACTION_FAILED,
                degradedCause = null,
            )
        }

        iTermuxLifecycleRegistry.dispatchBootstrapState(
            state = result.bootstrapState,
            cause = result.failureCause,
        )
        when (result.bootstrapState) {
            iTermuxBootstrapState.READY -> {
                iTermuxLifecycleRegistry.dispatchEnvironmentValidation(
                    result = iTermuxEnvironmentValidationResult.VALID,
                    cause = null,
                )
            }

            iTermuxBootstrapState.DEGRADED -> {
                iTermuxLifecycleRegistry.dispatchEnvironmentValidation(
                    result = iTermuxEnvironmentValidationResult.DEGRADED,
                    cause = result.degradedCause,
                )
            }

            else -> Unit
        }

        return result
    }

    fun installPackagedBootstrap(
        context: Context,
        runtime: iTermuxRuntime,
    ): iTermuxRuntime {
        return installBootstrap(runtime) {
            context.assets.open(runtime.bootstrapAssetPath)
        }
    }

    private fun loadSupportedPackages(context: Context): List<String> {
        return runCatching {
            context.assets.open(iTermuxSupportedPackages.ASSET_PATH).use { stream ->
                iTermuxSupportedPackages.parse(stream)
            }
        }.getOrDefault(emptyList())
    }

    private fun hasAsset(context: Context, assetPath: String): Boolean {
        return runCatching {
            context.assets.open(assetPath).use { }
            true
        }.getOrDefault(false)
    }
}

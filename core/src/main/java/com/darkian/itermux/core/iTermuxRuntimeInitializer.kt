package com.darkian.itermux.core

// INTERNAL-TERMUX MODIFIED - merge carefully

/**
 * Builds the initialized runtime state from the host-owned files directory.
 */
object iTermuxRuntimeInitializer {
    fun initialize(
        filesDir: String,
        hostPackageName: String,
        config: iTermuxConfig = iTermuxConfig(),
        supportedPackages: List<String> = emptyList(),
        supportedAbis: List<String> = config.supportedAbisOverride ?: emptyList(),
        bootstrapAssetPath: String = config.bootstrapAssetPath,
        isBootstrapPayloadPackaged: Boolean = false,
        baseEnv: Map<String, String> = emptyMap(),
        extraEnv: Map<String, String> = emptyMap(),
        failSafe: Boolean = false,
        autoInstallBootstrap: Boolean = false,
        bootstrapInstaller: ((iTermuxRuntime) -> iTermuxRuntime)? = null,
        bootstrapStateObserver: ((iTermuxBootstrapState, iTermuxRuntimeFailureCause?) -> Unit)? = null,
        environmentValidationObserver: ((iTermuxEnvironmentValidationResult, iTermuxDegradedCause?) -> Unit)? = null,
    ): iTermuxRuntime {
        val identity = iTermuxIdentityResolver.resolve(
            hostPackageName = hostPackageName,
            config = config,
        )
        val paths = iTermuxPathResolver.resolve(
            filesDir = filesDir,
            identity = identity,
            config = config,
        )
        val bootstrapVariant = iTermuxBootstrapResolver.resolve(
            supportedAbis = supportedAbis,
            config = config,
        )
        val resolvedBootstrapAssetPath = bootstrapVariant?.assetPath ?: bootstrapAssetPath
        val bootstrapFailureCauseOverride = if (supportedAbis.isNotEmpty() && bootstrapVariant == null) {
            iTermuxRuntimeFailureCause.UNSUPPORTED_ABI
        } else {
            null
        }
        return refresh(
            identity = identity,
            paths = paths,
            supportedPackages = supportedPackages,
            supportedAbis = supportedAbis,
            bootstrapAssetPath = resolvedBootstrapAssetPath,
            bootstrapVariantAbi = bootstrapVariant?.abi,
            isBootstrapPayloadPackaged = isBootstrapPayloadPackaged,
            baseEnv = baseEnv,
            extraEnv = extraEnv,
            failSafe = failSafe,
            autoInstallBootstrap = autoInstallBootstrap,
            bootstrapInstaller = bootstrapInstaller,
            bootstrapFailureCauseOverride = bootstrapFailureCauseOverride,
            bootstrapStateObserver = bootstrapStateObserver,
            environmentValidationObserver = environmentValidationObserver,
        )
    }

    fun refresh(
        paths: iTermuxPaths,
        supportedPackages: List<String> = emptyList(),
        supportedAbis: List<String> = emptyList(),
        bootstrapAssetPath: String = iTermuxConfig().bootstrapAssetPath,
        bootstrapVariantAbi: String? = null,
        isBootstrapPayloadPackaged: Boolean = false,
        baseEnv: Map<String, String> = emptyMap(),
        extraEnv: Map<String, String> = emptyMap(),
        failSafe: Boolean = false,
        bootstrapStateObserver: ((iTermuxBootstrapState, iTermuxRuntimeFailureCause?) -> Unit)? = null,
        environmentValidationObserver: ((iTermuxEnvironmentValidationResult, iTermuxDegradedCause?) -> Unit)? = null,
    ): iTermuxRuntime {
        return refresh(
            identity = iTermuxIdentityResolver.resolve(paths),
            paths = paths,
            supportedPackages = supportedPackages,
            supportedAbis = supportedAbis,
            bootstrapAssetPath = bootstrapAssetPath,
            bootstrapVariantAbi = bootstrapVariantAbi,
            isBootstrapPayloadPackaged = isBootstrapPayloadPackaged,
            baseEnv = baseEnv,
            extraEnv = extraEnv,
            failSafe = failSafe,
            bootstrapStateObserver = bootstrapStateObserver,
            environmentValidationObserver = environmentValidationObserver,
        )
    }

    fun refresh(
        identity: iTermuxIdentity,
        paths: iTermuxPaths,
        supportedPackages: List<String> = emptyList(),
        supportedAbis: List<String> = emptyList(),
        bootstrapAssetPath: String = iTermuxConfig().bootstrapAssetPath,
        bootstrapVariantAbi: String? = null,
        isBootstrapPayloadPackaged: Boolean = false,
        baseEnv: Map<String, String> = emptyMap(),
        extraEnv: Map<String, String> = emptyMap(),
        failSafe: Boolean = false,
        autoInstallBootstrap: Boolean = false,
        bootstrapInstaller: ((iTermuxRuntime) -> iTermuxRuntime)? = null,
        bootstrapFailureCauseOverride: iTermuxRuntimeFailureCause? = null,
        bootstrapStateObserver: ((iTermuxBootstrapState, iTermuxRuntimeFailureCause?) -> Unit)? = null,
        environmentValidationObserver: ((iTermuxEnvironmentValidationResult, iTermuxDegradedCause?) -> Unit)? = null,
    ): iTermuxRuntime {
        val environment = iTermuxEnvironment.build(
            paths = paths,
            baseEnv = baseEnv,
            extraEnv = extraEnv,
            failSafe = failSafe,
        )

        iTermuxRuntimeFiles.ensureLayout(paths)
        iTermuxRuntimeFiles.writeEnvironmentFile(paths, environment)
        val selectedPropertiesFile = iTermuxProperties.findReadablePropertiesFile(paths)?.absolutePath
        val properties = iTermuxProperties.load(paths)
        val defaultWorkingDirectory = iTermuxWorkingDirectory.resolve(paths, properties)
        val isBootstrapRequired = iTermuxPrefixState.isBootstrapRequired(paths)
        val validation = if (isBootstrapRequired || bootstrapFailureCauseOverride != null) {
            null
        } else {
            iTermuxEnvironmentValidator.validate(
                paths = paths,
                environment = environment,
            )
        }

        val bootstrapState = when {
            bootstrapFailureCauseOverride != null -> iTermuxBootstrapState.FAILED
            isBootstrapRequired -> iTermuxBootstrapState.UNINITIALIZED
            validation?.result == iTermuxEnvironmentValidationResult.DEGRADED -> iTermuxBootstrapState.DEGRADED
            else -> iTermuxBootstrapState.READY
        }
        val failureCause = bootstrapFailureCauseOverride ?: if (validation?.result == iTermuxEnvironmentValidationResult.DEGRADED) {
            iTermuxRuntimeFailureCause.ENVIRONMENT_DEGRADED
        } else {
            null
        }

        val runtime = iTermuxRuntime(
            identity = identity,
            paths = paths,
            environment = environment,
            supportedPackages = supportedPackages,
            supportedAbis = supportedAbis,
            bootstrapAssetPath = bootstrapAssetPath,
            bootstrapVariantAbi = bootstrapVariantAbi,
            isBootstrapPayloadPackaged = isBootstrapPayloadPackaged,
            bootstrapState = bootstrapState,
            failureCause = failureCause,
            degradedCause = validation?.degradedCause,
            properties = properties,
            selectedPropertiesFile = selectedPropertiesFile,
            defaultWorkingDirectory = defaultWorkingDirectory,
            isBootstrapRequired = isBootstrapRequired,
        )

        val shouldAutoInstall = autoInstallBootstrap &&
            runtime.bootstrapState != iTermuxBootstrapState.FAILED &&
            runtime.isBootstrapRequired &&
            runtime.isBootstrapPayloadPackaged &&
            bootstrapInstaller != null
        if (shouldAutoInstall) {
            bootstrapStateObserver?.invoke(runtime.bootstrapState, runtime.failureCause)
        }

        val finalRuntime = maybeInstallBootstrap(
            runtime = runtime,
            autoInstallBootstrap = autoInstallBootstrap,
            bootstrapInstaller = bootstrapInstaller,
            bootstrapStateObserver = bootstrapStateObserver,
        )
        if (!shouldAutoInstall) {
            bootstrapStateObserver?.invoke(finalRuntime.bootstrapState, finalRuntime.failureCause)
        }

        notifyEnvironmentValidation(
            runtime = finalRuntime,
            environmentValidationObserver = environmentValidationObserver,
        )
        return finalRuntime
    }

    private fun maybeInstallBootstrap(
        runtime: iTermuxRuntime,
        autoInstallBootstrap: Boolean,
        bootstrapInstaller: ((iTermuxRuntime) -> iTermuxRuntime)?,
        bootstrapStateObserver: ((iTermuxBootstrapState, iTermuxRuntimeFailureCause?) -> Unit)?,
    ): iTermuxRuntime {
        if (runtime.bootstrapState == iTermuxBootstrapState.FAILED) {
            return runtime
        }
        if (!autoInstallBootstrap || !runtime.isBootstrapRequired) {
            return runtime
        }
        if (!runtime.isBootstrapPayloadPackaged) {
            return runtime
        }

        checkNotNull(bootstrapInstaller) {
            "A bootstrap installer is required when auto-installing a packaged payload."
        }
        return iTermuxBootstrapStateMachine.bootstrap(
            runtime = runtime,
            bootstrapInstaller = bootstrapInstaller,
            stateObserver = { state, cause ->
                bootstrapStateObserver?.invoke(state, cause)
            },
        )
    }

    private fun notifyEnvironmentValidation(
        runtime: iTermuxRuntime,
        environmentValidationObserver: ((iTermuxEnvironmentValidationResult, iTermuxDegradedCause?) -> Unit)?,
    ) {
        when (runtime.bootstrapState) {
            iTermuxBootstrapState.READY -> {
                environmentValidationObserver?.invoke(
                    iTermuxEnvironmentValidationResult.VALID,
                    null,
                )
            }

            iTermuxBootstrapState.DEGRADED -> {
                environmentValidationObserver?.invoke(
                    iTermuxEnvironmentValidationResult.DEGRADED,
                    runtime.degradedCause,
                )
            }

            else -> Unit
        }
    }
}

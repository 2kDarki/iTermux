package com.darkian.itermux.core

/**
 * Builds the initialized runtime state from the host-owned files directory.
 */
object iTermuxRuntimeInitializer {
    fun initialize(
        filesDir: String,
        hostPackageName: String,
        config: iTermuxConfig = iTermuxConfig(),
        baseEnv: Map<String, String> = emptyMap(),
        extraEnv: Map<String, String> = emptyMap(),
        failSafe: Boolean = false,
    ): iTermuxRuntime {
        val paths = iTermuxPathResolver.resolve(
            filesDir = filesDir,
            hostPackageName = hostPackageName,
            config = config,
        )
        return refresh(
            paths = paths,
            baseEnv = baseEnv,
            extraEnv = extraEnv,
            failSafe = failSafe,
        )
    }

    fun refresh(
        paths: iTermuxPaths,
        baseEnv: Map<String, String> = emptyMap(),
        extraEnv: Map<String, String> = emptyMap(),
        failSafe: Boolean = false,
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

        return iTermuxRuntime(
            paths = paths,
            environment = environment,
            properties = properties,
            selectedPropertiesFile = selectedPropertiesFile,
            defaultWorkingDirectory = defaultWorkingDirectory,
            isBootstrapRequired = isBootstrapRequired,
        )
    }
}

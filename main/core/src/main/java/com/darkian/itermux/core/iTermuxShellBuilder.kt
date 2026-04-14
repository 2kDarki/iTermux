package com.darkian.itermux.core

/**
 * Builds shell launch specifications from the canonical runtime paths.
 */
object iTermuxShellBuilder {
    fun loginShell(
        runtime: iTermuxRuntime,
        shellBinary: String = "sh",
        baseEnv: Map<String, String> = emptyMap(),
        extraEnv: Map<String, String> = emptyMap(),
        workingDirectory: String = runtime.defaultWorkingDirectory,
        failSafe: Boolean = false,
    ): iTermuxShellSpec {
        return loginShell(
            paths = runtime.paths,
            shellBinary = shellBinary,
            baseEnv = baseEnv,
            extraEnv = extraEnv,
            workingDirectory = workingDirectory,
            failSafe = failSafe,
        )
    }

    fun loginShell(
        paths: iTermuxPaths,
        shellBinary: String = "sh",
        baseEnv: Map<String, String> = emptyMap(),
        extraEnv: Map<String, String> = emptyMap(),
        workingDirectory: String = iTermuxEnvironment.defaultWorkingDirectory(paths),
        failSafe: Boolean = false,
    ): iTermuxShellSpec {
        return iTermuxShellSpec(
            executable = "${paths.binDir}/$shellBinary",
            arguments = emptyList(),
            workingDirectory = workingDirectory,
            environment = iTermuxEnvironment.build(
                paths = paths,
                baseEnv = baseEnv,
                extraEnv = extraEnv,
                failSafe = failSafe,
            ),
        )
    }

    fun command(
        runtime: iTermuxRuntime,
        executable: String,
        arguments: List<String>,
        baseEnv: Map<String, String> = emptyMap(),
        extraEnv: Map<String, String> = emptyMap(),
        workingDirectory: String = runtime.defaultWorkingDirectory,
        failSafe: Boolean = false,
    ): iTermuxShellSpec {
        return command(
            paths = runtime.paths,
            executable = executable,
            arguments = arguments,
            baseEnv = baseEnv,
            extraEnv = extraEnv,
            workingDirectory = workingDirectory,
            failSafe = failSafe,
        )
    }

    fun command(
        paths: iTermuxPaths,
        executable: String,
        arguments: List<String>,
        baseEnv: Map<String, String> = emptyMap(),
        extraEnv: Map<String, String> = emptyMap(),
        workingDirectory: String = iTermuxEnvironment.defaultWorkingDirectory(paths),
        failSafe: Boolean = false,
    ): iTermuxShellSpec {
        return iTermuxShellSpec(
            executable = executable,
            arguments = arguments,
            workingDirectory = workingDirectory,
            environment = iTermuxEnvironment.build(
                paths = paths,
                baseEnv = baseEnv,
                extraEnv = extraEnv,
                failSafe = failSafe,
            ),
        )
    }

    fun fileCommand(
        runtime: iTermuxRuntime,
        executable: String,
        arguments: List<String>,
        baseEnv: Map<String, String> = emptyMap(),
        extraEnv: Map<String, String> = emptyMap(),
        workingDirectory: String = runtime.defaultWorkingDirectory,
        failSafe: Boolean = false,
    ): iTermuxShellSpec {
        return fileCommand(
            paths = runtime.paths,
            executable = executable,
            arguments = arguments,
            baseEnv = baseEnv,
            extraEnv = extraEnv,
            workingDirectory = workingDirectory,
            failSafe = failSafe,
        )
    }

    fun fileCommand(
        paths: iTermuxPaths,
        executable: String,
        arguments: List<String>,
        baseEnv: Map<String, String> = emptyMap(),
        extraEnv: Map<String, String> = emptyMap(),
        workingDirectory: String = iTermuxEnvironment.defaultWorkingDirectory(paths),
        failSafe: Boolean = false,
    ): iTermuxShellSpec {
        val shellArgs = iTermuxShellArgs.setup(
            executable = executable,
            arguments = arguments,
            paths = paths,
        )

        return iTermuxShellSpec(
            executable = shellArgs.first(),
            arguments = shellArgs.drop(1),
            workingDirectory = workingDirectory,
            environment = iTermuxEnvironment.build(
                paths = paths,
                baseEnv = baseEnv,
                extraEnv = extraEnv,
                failSafe = failSafe,
            ),
        )
    }
}

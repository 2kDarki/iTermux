package com.darkian.itermux.core
// INTERNAL-TERMUX MODIFIED - merge carefully

/**
 * Convenience session factories on initialized runtime state.
 */
fun iTermuxRuntime.loginShell(
    shellBinary: String = "sh",
    baseEnv: Map<String, String> = emptyMap(),
    extraEnv: Map<String, String> = emptyMap(),
    workingDirectory: String = defaultWorkingDirectory,
    failSafe: Boolean = false,
): iTermuxShellSpec {
    return iTermuxShellBuilder.loginShell(
        runtime = this,
        shellBinary = shellBinary,
        baseEnv = baseEnv,
        extraEnv = extraEnv,
        workingDirectory = workingDirectory,
        failSafe = failSafe,
    )
}

fun iTermuxRuntime.command(
    executable: String,
    arguments: List<String>,
    baseEnv: Map<String, String> = emptyMap(),
    extraEnv: Map<String, String> = emptyMap(),
    workingDirectory: String = defaultWorkingDirectory,
    failSafe: Boolean = false,
): iTermuxShellSpec {
    return iTermuxShellBuilder.command(
        runtime = this,
        executable = executable,
        arguments = arguments,
        baseEnv = baseEnv,
        extraEnv = extraEnv,
        workingDirectory = workingDirectory,
        failSafe = failSafe,
    )
}

fun iTermuxRuntime.fileCommand(
    executable: String,
    arguments: List<String>,
    baseEnv: Map<String, String> = emptyMap(),
    extraEnv: Map<String, String> = emptyMap(),
    workingDirectory: String = defaultWorkingDirectory,
    failSafe: Boolean = false,
): iTermuxShellSpec {
    return iTermuxShellBuilder.fileCommand(
        runtime = this,
        executable = executable,
        arguments = arguments,
        baseEnv = baseEnv,
        extraEnv = extraEnv,
        workingDirectory = workingDirectory,
        failSafe = failSafe,
    )
}

fun iTermuxRuntime.createSession(
    sessionId: String,
    shellBinary: String = "sh",
    baseEnv: Map<String, String> = emptyMap(),
    extraEnv: Map<String, String> = emptyMap(),
    workingDirectory: String = defaultWorkingDirectory,
    failSafe: Boolean = false,
): iTermuxSession {
    val availabilityFailure = sessionStartFailureCause()
    if (availabilityFailure != null) {
        return failedSession(
            sessionId = sessionId,
            backend = iTermuxSessionBackends.NATIVE,
            mode = iTermuxSessionMode.LOGIN_SHELL,
            shellSpec = iTermuxShellSpec(
                executable = "${paths.binDir}/$shellBinary",
                arguments = emptyList(),
                workingDirectory = workingDirectory,
                environment = environment,
            ),
            failureCause = availabilityFailure,
        )
    }

    return iTermuxSession(
        id = sessionId,
        backend = iTermuxSessionBackends.NATIVE,
        mode = iTermuxSessionMode.LOGIN_SHELL,
        shellSpec = loginShell(
            shellBinary = shellBinary,
            baseEnv = baseEnv,
            extraEnv = extraEnv,
            workingDirectory = workingDirectory,
            failSafe = failSafe,
        ),
    )
}

fun iTermuxRuntime.createCommandSession(
    sessionId: String,
    executable: String,
    arguments: List<String>,
    baseEnv: Map<String, String> = emptyMap(),
    extraEnv: Map<String, String> = emptyMap(),
    workingDirectory: String = defaultWorkingDirectory,
    failSafe: Boolean = false,
): iTermuxSession {
    val availabilityFailure = sessionStartFailureCause()
    if (availabilityFailure != null) {
        return failedSession(
            sessionId = sessionId,
            backend = iTermuxSessionBackends.NATIVE,
            mode = iTermuxSessionMode.COMMAND,
            shellSpec = iTermuxShellSpec(
                executable = executable,
                arguments = arguments,
                workingDirectory = workingDirectory,
                environment = environment,
            ),
            failureCause = availabilityFailure,
        )
    }

    return iTermuxSession(
        id = sessionId,
        backend = iTermuxSessionBackends.NATIVE,
        mode = iTermuxSessionMode.COMMAND,
        shellSpec = command(
            executable = executable,
            arguments = arguments,
            baseEnv = baseEnv,
            extraEnv = extraEnv,
            workingDirectory = workingDirectory,
            failSafe = failSafe,
        ),
    )
}

fun iTermuxRuntime.createFileSession(
    sessionId: String,
    executable: String,
    arguments: List<String>,
    baseEnv: Map<String, String> = emptyMap(),
    extraEnv: Map<String, String> = emptyMap(),
    workingDirectory: String = defaultWorkingDirectory,
    failSafe: Boolean = false,
): iTermuxSession {
    val availabilityFailure = sessionStartFailureCause()
    if (availabilityFailure != null) {
        return failedSession(
            sessionId = sessionId,
            backend = iTermuxSessionBackends.NATIVE,
            mode = iTermuxSessionMode.FILE_COMMAND,
            shellSpec = iTermuxShellSpec(
                executable = executable,
                arguments = arguments,
                workingDirectory = workingDirectory,
                environment = environment,
            ),
            failureCause = availabilityFailure,
        )
    }

    return iTermuxSession(
        id = sessionId,
        backend = iTermuxSessionBackends.NATIVE,
        mode = iTermuxSessionMode.FILE_COMMAND,
        shellSpec = fileCommand(
            executable = executable,
            arguments = arguments,
            baseEnv = baseEnv,
            extraEnv = extraEnv,
            workingDirectory = workingDirectory,
            failSafe = failSafe,
        ),
    )
}

fun iTermuxRuntime.sessionStartFailureCause(): iTermuxRuntimeFailureCause? {
    return when (bootstrapState) {
        iTermuxBootstrapState.READY -> null
        iTermuxBootstrapState.DEGRADED -> failureCause ?: iTermuxRuntimeFailureCause.ENVIRONMENT_DEGRADED
        else -> failureCause ?: iTermuxRuntimeFailureCause.SESSION_START_FAILED
    }
}

private fun failedSession(
    sessionId: String,
    backend: iTermuxSessionBackend,
    mode: iTermuxSessionMode,
    shellSpec: iTermuxShellSpec,
    failureCause: iTermuxRuntimeFailureCause,
): iTermuxSession {
    return iTermuxSession(
        id = sessionId,
        backend = backend,
        mode = mode,
        shellSpec = shellSpec,
        state = iTermuxSessionState.DEAD,
        failureCause = failureCause,
    )
}

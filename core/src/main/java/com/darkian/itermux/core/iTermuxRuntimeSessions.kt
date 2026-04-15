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

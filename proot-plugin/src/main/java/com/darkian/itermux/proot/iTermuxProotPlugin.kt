package com.darkian.itermux.proot

import com.darkian.itermux.core.iTermuxEnvironment
import com.darkian.itermux.core.iTermuxRuntime
import com.darkian.itermux.core.iTermuxSession
import com.darkian.itermux.core.iTermuxSessionBackend
import com.darkian.itermux.core.iTermuxSessionMode
import com.darkian.itermux.core.iTermuxShellSpec

/**
 * Optional proot launcher that stays outside the core runtime module while
 * returning the same host-facing session contract as native sessions.
 */
object iTermuxProotPlugin {
    const val MODULE_ID: String = "internal-termux-proot"

    val BACKEND: iTermuxSessionBackend = iTermuxSessionBackend(id = "proot")

    fun launch(
        runtime: iTermuxRuntime,
        distribution: iTermuxProotDistribution,
        sessionId: String,
        shellArguments: List<String> = emptyList(),
        inheritRuntimeEnvironment: Boolean = false,
        baseEnv: Map<String, String> = emptyMap(),
        extraEnv: Map<String, String> = emptyMap(),
        config: iTermuxProotConfig = iTermuxProotConfig(
            executable = "${runtime.paths.binDir}/proot",
        ),
    ): iTermuxSession {
        val mergedBaseEnv = linkedMapOf<String, String>()
        if (inheritRuntimeEnvironment) {
            mergedBaseEnv.putAll(runtime.environment)
        }
        mergedBaseEnv.putAll(baseEnv)

        val mergedExtraEnv = linkedMapOf(
            "ITERMUX_SESSION_BACKEND" to BACKEND.id,
            "PROOT_DISTRO_NAME" to distribution.name,
            "PROOT_DISTRO_ROOTFS" to distribution.rootfsPath,
        )
        mergedExtraEnv.putAll(extraEnv)

        return iTermuxSession(
            id = sessionId,
            backend = BACKEND,
            mode = iTermuxSessionMode.LOGIN_SHELL,
            shellSpec = iTermuxShellSpec(
                executable = config.executable,
                arguments = buildArguments(
                    distribution = distribution,
                    shellArguments = shellArguments,
                    config = config,
                ),
                workingDirectory = runtime.defaultWorkingDirectory,
                environment = iTermuxEnvironment.build(
                    paths = runtime.paths,
                    baseEnv = mergedBaseEnv,
                    extraEnv = mergedExtraEnv,
                ),
            ),
        )
    }

    private fun buildArguments(
        distribution: iTermuxProotDistribution,
        shellArguments: List<String>,
        config: iTermuxProotConfig,
    ): List<String> {
        val arguments = mutableListOf<String>()
        arguments.addAll(config.extraArguments)
        arguments += listOf("-r", distribution.rootfsPath)
        config.bindPaths.forEach { bindPath ->
            arguments += listOf("-b", bindPath)
        }
        arguments += listOf("-w", distribution.workingDirectory, distribution.loginShell)
        arguments.addAll(shellArguments)
        return arguments
    }
}

data class iTermuxProotDistribution(
    val name: String,
    val rootfsPath: String,
    val loginShell: String = "/bin/sh",
    val workingDirectory: String = "/root",
)

data class iTermuxProotConfig(
    val executable: String,
    val bindPaths: List<String> = listOf("/dev", "/proc", "/sys"),
    val extraArguments: List<String> = listOf("--link2symlink", "-0"),
)

fun iTermuxRuntime.createProotSession(
    distribution: iTermuxProotDistribution,
    sessionId: String,
    shellArguments: List<String> = emptyList(),
    inheritRuntimeEnvironment: Boolean = false,
    baseEnv: Map<String, String> = emptyMap(),
    extraEnv: Map<String, String> = emptyMap(),
    config: iTermuxProotConfig = iTermuxProotConfig(
        executable = "${paths.binDir}/proot",
    ),
): iTermuxSession {
    return iTermuxProotPlugin.launch(
        runtime = this,
        distribution = distribution,
        sessionId = sessionId,
        shellArguments = shellArguments,
        inheritRuntimeEnvironment = inheritRuntimeEnvironment,
        baseEnv = baseEnv,
        extraEnv = extraEnv,
        config = config,
    )
}

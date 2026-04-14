package com.darkian.itermux.core

/**
 * Builds the baseline shell environment from the canonical runtime paths.
 *
 * This intentionally starts small and mirrors the core variables that the
 * upstream Termux runtime depends on most heavily.
 */
object iTermuxEnvironment {
    fun baseline(
        paths: iTermuxPaths,
        extraEnv: Map<String, String> = emptyMap(),
    ): Map<String, String> {
        return build(
            paths = paths,
            baseEnv = emptyMap(),
            extraEnv = extraEnv,
            failSafe = false,
        )
    }

    fun build(
        paths: iTermuxPaths,
        baseEnv: Map<String, String> = emptyMap(),
        extraEnv: Map<String, String> = emptyMap(),
        failSafe: Boolean = false,
    ): Map<String, String> {
        val environment = linkedMapOf<String, String>()
        environment.putAll(baseEnv)
        environment["HOME"] = paths.homeDir
        environment["PREFIX"] = paths.prefixDir

        if (!failSafe) {
            environment["TMPDIR"] = paths.tmpDir
            environment["PATH"] = paths.binDir
            environment.remove("LD_LIBRARY_PATH")
        }

        environment.putAll(extraEnv)
        return environment
    }

    fun defaultWorkingDirectory(paths: iTermuxPaths): String {
        return paths.homeDir
    }

    fun defaultBinPath(paths: iTermuxPaths): String {
        return paths.binDir
    }

    fun toDotEnvFile(environment: Map<String, String>): String {
        return environment.entries
            .asSequence()
            .filter { isValidVariableName(it.key) && isValidVariableValue(it.value) }
            .sortedBy { it.key }
            .joinToString(separator = "", postfix = "") { (name, value) ->
                "export $name=\"${escapeValue(value)}\"\n"
            }
    }

    private fun isValidVariableName(name: String): Boolean {
        return name.matches(Regex("[a-zA-Z_][a-zA-Z0-9_]*"))
    }

    private fun isValidVariableValue(value: String): Boolean {
        return !value.contains('\u0000')
    }

    private fun escapeValue(value: String): String {
        return buildString(value.length) {
            value.forEach { character ->
                if (character == '"' || character == '`' || character == '\\' || character == '$') {
                    append('\\')
                }
                append(character)
            }
        }
    }
}

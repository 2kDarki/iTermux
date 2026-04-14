package com.darkian.itermux.core
// INTERNAL-TERMUX MODIFIED - merge carefully

import java.io.FileInputStream
import java.io.IOException

/**
 * Resolves the actual argv used to launch a file inside the iTermux runtime.
 */
object iTermuxShellArgs {
    fun setup(
        executable: String,
        arguments: List<String> = emptyList(),
        paths: iTermuxPaths,
    ): List<String> {
        val interpreter = detectInterpreter(
            executable = executable,
            paths = paths,
        )

        return buildList {
            if (interpreter != null) {
                add(interpreter)
            }
            add(executable)
            addAll(arguments)
        }
    }

    private fun detectInterpreter(
        executable: String,
        paths: iTermuxPaths,
    ): String? {
        return try {
            FileInputStream(executable).use { stream ->
                val buffer = ByteArray(256)
                val bytesRead = stream.read(buffer)

                if (bytesRead <= 4) {
                    return null
                }

                when {
                    isElf(buffer) -> null
                    hasShebang(buffer) -> parseShebangInterpreter(buffer, bytesRead, paths)
                    else -> "${paths.binDir}/sh"
                }
            }
        } catch (_: IOException) {
            null
        }
    }

    private fun isElf(buffer: ByteArray): Boolean {
        return buffer[0] == 0x7F.toByte() &&
            buffer[1] == 'E'.code.toByte() &&
            buffer[2] == 'L'.code.toByte() &&
            buffer[3] == 'F'.code.toByte()
    }

    private fun hasShebang(buffer: ByteArray): Boolean {
        return buffer[0] == '#'.code.toByte() && buffer[1] == '!'.code.toByte()
    }

    private fun parseShebangInterpreter(
        buffer: ByteArray,
        bytesRead: Int,
        paths: iTermuxPaths,
    ): String? {
        val builder = StringBuilder()

        for (index in 2 until bytesRead) {
            val character = buffer[index].toInt().toChar()
            if (character == ' ' || character == '\n') {
                if (builder.isEmpty()) {
                    continue
                }

                return rewriteInterpreter(builder.toString(), paths)
            }

            builder.append(character)
        }

        return if (builder.isEmpty()) null else rewriteInterpreter(builder.toString(), paths)
    }

    private fun rewriteInterpreter(
        shebangExecutable: String,
        paths: iTermuxPaths,
    ): String? {
        if (!shebangExecutable.startsWith("/usr") && !shebangExecutable.startsWith("/bin")) {
            return null
        }

        val binaryName = shebangExecutable.substringAfterLast('/')
        return "${paths.binDir}/$binaryName"
    }
}

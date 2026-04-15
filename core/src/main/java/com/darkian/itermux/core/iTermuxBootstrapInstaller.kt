package com.darkian.itermux.core
// INTERNAL-TERMUX MODIFIED - merge carefully

import org.apache.commons.compress.archivers.tar.TarArchiveInputStream
import org.apache.commons.compress.compressors.xz.XZCompressorInputStream
import java.io.File
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption

/**
 * Extracts a packaged bootstrap payload into the host-owned prefix.
 */
object iTermuxBootstrapInstaller {
    fun install(
        runtime: iTermuxRuntime,
        openPayload: () -> InputStream,
    ): iTermuxRuntime {
        if (!runtime.isBootstrapRequired) {
            return runtime
        }
        check(runtime.isBootstrapPayloadPackaged) {
            "Bootstrap payload is not packaged at ${runtime.bootstrapAssetPath}."
        }

        val stagingDir = File(runtime.paths.stagingPrefixDir)
        val prefixDir = File(runtime.paths.prefixDir)
        stagingDir.deleteRecursively()
        prefixDir.deleteRecursively()
        stagingDir.mkdirs()

        openPayload().use { payload ->
            XZCompressorInputStream(payload).use { xzInput ->
                TarArchiveInputStream(xzInput).use { tarInput ->
                    extractTarArchive(
                        tarInput = tarInput,
                        destinationDir = stagingDir,
                    )
                }
            }
        }

        Files.move(
            stagingDir.toPath(),
            prefixDir.toPath(),
            StandardCopyOption.REPLACE_EXISTING,
        )

        return iTermuxRuntimeInitializer.refresh(
            identity = runtime.identity,
            paths = runtime.paths,
            supportedPackages = runtime.supportedPackages,
            bootstrapAssetPath = runtime.bootstrapAssetPath,
            isBootstrapPayloadPackaged = runtime.isBootstrapPayloadPackaged,
        )
    }

    private fun extractTarArchive(
        tarInput: TarArchiveInputStream,
        destinationDir: File,
    ) {
        var entry = tarInput.nextEntry as? org.apache.commons.compress.archivers.tar.TarArchiveEntry
        while (entry != null) {
            val normalizedName = normalizeEntryName(entry.name)
            if (normalizedName != null) {
                val destination = File(destinationDir, normalizedName)
                ensureInsideDestination(destinationDir, destination)

                when {
                    entry.isDirectory -> {
                        destination.mkdirs()
                        applyMode(destination, entry.mode)
                    }

                    entry.isSymbolicLink -> {
                        destination.parentFile?.mkdirs()
                        createSymlink(
                            link = destination.toPath(),
                            target = Path.of(entry.linkName),
                        )
                    }

                    else -> {
                        destination.parentFile?.mkdirs()
                        destination.outputStream().use { output ->
                            tarInput.copyTo(output)
                        }
                        applyMode(destination, entry.mode)
                    }
                }
            }

            entry = tarInput.nextEntry as? org.apache.commons.compress.archivers.tar.TarArchiveEntry
        }
    }

    private fun normalizeEntryName(entryName: String): String? {
        val normalized = entryName.replace('\\', '/').removePrefix("./").trimStart('/')
        return normalized.ifEmpty { null }
    }

    private fun ensureInsideDestination(
        destinationDir: File,
        destination: File,
    ) {
        val rootPath = destinationDir.canonicalFile.toPath()
        val destinationPath = destination.canonicalFile.toPath()
        check(destinationPath.startsWith(rootPath)) {
            "Refusing to extract outside prefix staging directory: ${destination.path}"
        }
    }

    private fun createSymlink(
        link: Path,
        target: Path,
    ) {
        Files.deleteIfExists(link)
        Files.createSymbolicLink(link, target)
    }

    private fun applyMode(
        destination: File,
        mode: Int,
    ) {
        val normalizedMode = mode and 0b111_111_111
        val ownerRead = normalizedMode and 0b100_000_000 != 0
        val ownerWrite = normalizedMode and 0b010_000_000 != 0
        val ownerExec = normalizedMode and 0b001_000_000 != 0
        val groupRead = normalizedMode and 0b000_100_000 != 0
        val groupWrite = normalizedMode and 0b000_010_000 != 0
        val groupExec = normalizedMode and 0b000_001_000 != 0
        val otherRead = normalizedMode and 0b000_000_100 != 0
        val otherWrite = normalizedMode and 0b000_000_010 != 0
        val otherExec = normalizedMode and 0b000_000_001 != 0

        destination.setReadable(ownerRead, true)
        destination.setWritable(ownerWrite, true)
        destination.setExecutable(ownerExec, true)
        destination.setReadable(groupRead || otherRead, false)
        destination.setWritable(groupWrite || otherWrite, false)
        destination.setExecutable(groupExec || otherExec, false)
    }
}

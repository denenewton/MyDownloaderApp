package com.example.boilerplate.downloader

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.awt.Desktop
import java.io.File

/**
 * Desktop implementation for media downloading using yt-dlp and FFmpeg.
 */
class DesktopMediaDownloader0 : MediaDownloader {

    private val progressRegex = Regex("""\[download\]\s+(\d+(?:\.\d+)?)%\s+of\s+([^\s]+)\s+at\s+([^\s]+)\s+ETA\s+([^\s]+)""")
    private val sep = ":::META:::" // Separador mais seguro

    private fun getBinaryPath(name: String): String {
        return BinaryManager.getPath(name)
    }

    override suspend fun fetchMetadata(query: String): Result<MediaMetadata> = withContext(Dispatchers.IO) {
        runCatching {
            if (query.isBlank()) throw Exception("Search query is empty")
            val ytDlpPath = getBinaryPath("yt-dlp")
            
            val infoCommand = listOf(
                ytDlpPath,
                "ytsearch1:$query", 
                "--print", "%(title)s$sep%(uploader)s$sep%(album)s$sep%(duration_string)s$sep%(thumbnail)s$sep%(filesize,filesize_approx)s$sep%(artist)s$sep%(track)s$sep%(webpage_url)s"
            )

            val infoProcess = ProcessBuilder(infoCommand).start()
            val output = infoProcess.inputStream.bufferedReader().readLine()

            if (output == null) {
                val errorMsg = infoProcess.errorStream.bufferedReader().readText()
                throw Exception("yt-dlp failed: $errorMsg")
            }

            val result = output.split(sep)

            if (result.size >= 6) {
                MediaMetadata(
                    title = result.getOrNull(7)?.takeIf { it != "NA" && it.isNotEmpty() } ?: result[0],
                    artist = result.getOrNull(6)?.takeIf { it != "NA" && it.isNotEmpty() } ?: result[1],
                    album = result[2].takeIf { it != "NA" && it.isNotEmpty() } ?: "Unknown Album",
                    duration = result[3],
                    thumbnailUrl = result[4],
                    fileSize = formatInitialSize(result[5]),
                    url = result.getOrNull(8)
                )
            } else {
                throw Exception("Unexpected metadata format")
            }
        }
    }

    override suspend fun fetchPlaylistMetadata(url: String): Result<List<MediaMetadata>> = withContext(Dispatchers.IO) {
        runCatching {
            val ytDlpPath = getBinaryPath("yt-dlp")

            // Pega o título da playlist e os itens
            val command = listOf(
                ytDlpPath,
                "--flat-playlist",
                "--print", "%(playlist_title)s$sep%(title)s$sep%(uploader)s$sep%(duration_string)s$sep%(thumbnail)s$sep%(webpage_url)s",
                url
            )

            val process = ProcessBuilder(command).start()
            val metadataList = mutableListOf<MediaMetadata>()
            
            process.inputStream.bufferedReader().useLines { lines ->
                lines.forEach { line ->
                    val result = line.split(sep)
                    if (result.size >= 6) {
                        metadataList.add(MediaMetadata(
                            title = result[1],
                            artist = result[2],
                            duration = result[3],
                            thumbnailUrl = result[4],
                            url = result[5],
                            fileSize = "Varies",
                            album = result[0]
                        ))
                    }
                }
            }

            if (metadataList.isEmpty()) {
                val errorMsg = process.errorStream.bufferedReader().readText()
                if (errorMsg.isNotEmpty()) throw Exception(errorMsg)
            }

            metadataList
        }
    }

    override suspend fun download(
        query: String,
        format: String,
        quality: String,
        onProgress: (Float, String) -> Unit
    ): Result<DownloadResult> = withContext(Dispatchers.IO) {
        runCatching {
            if (query.isBlank()) throw Exception("Search query is empty")
            val ytDlpPath = getBinaryPath("yt-dlp")
            val ffmpegPath = getBinaryPath("ffmpeg")

            val downloadsFolder = File(System.getProperty("user.home"), "Music/MyDownloaderApp")
            if (!downloadsFolder.exists()) downloadsFolder.mkdirs()

            val h = if (quality == "1080p") "1080" else "720"
            val outputTemplate = "${downloadsFolder.absolutePath}/%(title)s.%(ext)s"

            val target = if (query.startsWith("http")) query else "ytsearch1:$query"
            val command = mutableListOf(ytDlpPath, target)

            if (format == "MP3") {
                command.addAll(listOf("-x", "--audio-format", "mp3", "--audio-quality", "5"))
            } else {
                command.addAll(listOf(
                    "-f", "bestvideo[height<=$h][vcodec^=avc1]+bestaudio[acodec^=mp4a]/best[height<=$h][ext=mp4]/best",
                    "--merge-output-format", "mp4"
                ))
            }

            command.addAll(listOf("--newline", "--no-playlist", "--progress", "-o", outputTemplate))

            if (ffmpegPath.contains("/") || ffmpegPath.contains("\\")) {
                command.add("--ffmpeg-location")
                command.add(File(ffmpegPath).parent)
            }

            val pb = ProcessBuilder(command).redirectErrorStream(true)
            val process = pb.start()
            var lastLine = ""
            var finalFilePath: String? = null

            process.inputStream.bufferedReader().use { reader ->
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    lastLine = line ?: ""
                    println("DEBUG DOWNLOAD: $lastLine")

                    val match = progressRegex.find(lastLine)
                    if (match != null) {
                        val p = match.groupValues[1].toFloat() / 100f
                        val size = match.groupValues[2]
                        val speed = match.groupValues[3]
                        val eta = match.groupValues[4]
                        onProgress(p, "$size|$speed|$eta")
                    } else if (lastLine.contains("[ExtractAudio]") || lastLine.contains("[ffmpeg]")) {
                        onProgress(0.99f, "Finalizing...")
                    }

                    if (lastLine.contains("Destination:")) {
                        val path = lastLine.substringAfter("Destination:").trim()
                        if (!path.endsWith(".part")) finalFilePath = path
                    } else if (lastLine.contains("Merging formats into")) {
                        finalFilePath = lastLine.substringAfter("into").trim().removeSurrounding("\"")
                    } else if (lastLine.contains("has already been downloaded")) {
                        finalFilePath = lastLine.substringBefore("has already been downloaded").trim().removePrefix("[download]").trim()
                    }
                }
            }

            if (process.waitFor() == 0) {
                val resultFile = finalFilePath?.let { File(it) }
                if (resultFile != null && resultFile.exists()) {
                    DownloadResult(resultFile.nameWithoutExtension, resultFile.absolutePath)
                } else {
                    throw Exception("File not found")
                }
            } else {
                throw Exception("Download failed: $lastLine")
            }
        }
    }

    override fun openDownloadsFolder() {
        try {
            val folder = File(System.getProperty("user.home"), "Music/MyDownloaderApp")
            if (folder.exists()) Desktop.getDesktop().open(folder)
        } catch (e: Exception) { e.printStackTrace() }
    }


    override fun openFile(path: String) {
        val file = File(path)
        if (!file.exists()) return
        val os = System.getProperty("os.name").lowercase()
        try {
            if (os.contains("mac")) {
                val vlcProcess = ProcessBuilder("open", "-a", "VLC", file.absolutePath).start()
                if (vlcProcess.waitFor() != 0) Desktop.getDesktop().open(file)
            } else Desktop.getDesktop().open(file)
        } catch (e: Exception) {
            try { Desktop.getDesktop().open(file) } catch (e2: Exception) { e2.printStackTrace() }
        }
    }

   override fun shareFile(path: String) {
    val file = File(path)
    if (!file.exists()) return
    val os = System.getProperty("os.name").lowercase()
    
    try {
        when {
            os.contains("mac") -> {
                ProcessBuilder("open", "-R", file.absolutePath).start()
            }
            os.contains("win") -> {
                ProcessBuilder("explorer.exe", "/select,", file.absolutePath).start()
            }
            os.contains("linux") -> {
                // Tenta o método via DBus (funciona em GNOME, KDE, XFCE, etc.)
                // Isso abre a pasta e seleciona/destaca o arquivo
                ProcessBuilder(
                    "dbus-send",
                    "--session",
                    "--print-reply",
                    "--dest=org.freedesktop.FileManager1",
                    "/org/freedesktop/FileManager1",
                    "org.freedesktop.FileManager1.ShowItems",
                    "array:string:file://${file.absolutePath}",
                    "string:\"\""
                ).start()
            }
        }
    } catch (e: Exception) {
        // Fallback: Se o DBus falhar, apenas abre a pasta pai com xdg-open
        if (os.contains("linux")) {
            ProcessBuilder("xdg-open", file.parentFile.absolutePath).start()
        }
        e.printStackTrace()
    }
}

    override fun deleteFile(path: String): Boolean = File(path).delete()

    private fun formatInitialSize(sizeStr: String): String {
        if (sizeStr == "NA" || sizeStr.isBlank()) return "Unknown size"
        if (sizeStr.any { it.isLetter() }) return sizeStr.replace("iB", "B")
        val bytes = sizeStr.toLongOrNull() ?: return sizeStr
        return when {
            bytes >= 1024 * 1024 * 1024 -> "~%.1f GB".format(bytes / (1024.0 * 1024.0 * 1024.0))
            bytes >= 1024 * 1024 -> "~%.1f MB".format(bytes / (1024.0 * 1024.0))
            bytes >= 1024 -> "~%.1f KB".format(bytes / 1024.0)
            else -> "$bytes B"
        }
    }
}

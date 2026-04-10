package com.example.boilerplate.downloader

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.awt.Desktop
import java.io.File

class DesktopMediaDownloader : MediaDownloader {

    private val progressRegex = Regex("""\[download\]\s+(\d+(?:\.\d+)?)%\s+of\s+([^\s]+)\s+at\s+([^\s]+)\s+ETA\s+([^\s]+)""")
    private val sep = ":::META:::"

    private fun getBinaryPath(name: String): String = BinaryManager.getPath(name)

    // Helper para limpar o "NA" do yt-dlp
    private fun String?.clean(): String? = if (this == "NA" || this.isNullOrBlank()) null else this

    override suspend fun fetchMetadata(query: String): Result<MediaMetadata> = withContext(Dispatchers.IO) {
        runCatching {
            if (query.isBlank()) throw Exception("Search query is empty")
            val ytDlpPath = getBinaryPath("yt-dlp")

            val infoCommand = listOf(
                ytDlpPath,
                "ytsearch1:$query",
                "--no-playlist",
                "--no-warnings",
                "--print", "%(title)s$sep%(uploader)s$sep%(album)s$sep%(duration_string)s$sep%(thumbnails.-1.url)s$sep%(filesize,filesize_approx)s$sep%(artist)s$sep%(track)s$sep%(webpage_url)s"
            )

            val process = ProcessBuilder(infoCommand).start()
            val output = process.inputStream.bufferedReader().readLine() ?: throw Exception("No metadata found")
            val res = output.split(sep)

            if (res.size < 6) throw Exception("Unexpected metadata format")

            MediaMetadata(
                title = res[7].clean() ?: res[0], // Tenta 'track', cai em 'title'
                artist = res[6].clean() ?: res[1], // Tenta 'artist', cai em 'uploader'
                album = res[2].clean() ?: "Unknown Album",
                duration = res[3].clean() ?: "00:00",
                thumbnailUrl = res[4].clean() ?: "", // O segredo está aqui no res[4]
                fileSize = formatInitialSize(res[5]),
                url = res.getOrNull(8).clean()
            )
        }
    }

    override suspend fun fetchPlaylistMetadata(url: String): Result<List<MediaMetadata>> = withContext(Dispatchers.IO) {
        runCatching {
            val ytDlpPath = getBinaryPath("yt-dlp")
            val command = listOf(
                ytDlpPath,
                "--flat-playlist",
                "--no-warnings",
                "--print", "%(playlist_title)s$sep%(title)s$sep%(uploader)s$sep%(duration_string)s$sep%(thumbnails.-1.url)s$sep%(webpage_url)s",
                url
            )

            val process = ProcessBuilder(command).start()
            process.inputStream.bufferedReader().useLines { lines ->
                lines.mapNotNull { line ->
                    val res = line.split(sep)
                    if (res.size >= 6) {
                        MediaMetadata(
                            title = res[1],
                            artist = res[2],
                            duration = res[3],
                            thumbnailUrl = res[4].clean() ?: "",
                            url = res[5],
                            fileSize = "Varies",
                            album = res[0]
                        )
                    } else null
                }.toList()
            }
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

            val downloadsFolder = File(System.getProperty("user.home"), "Music/MyDownloaderApp").apply {
                if (!exists()) mkdirs()
            }

            val target = if (query.startsWith("http")) query else "ytsearch1:$query"
            val outputTemplate = "${downloadsFolder.absolutePath}/%(title)s.%(ext)s"

            val command = mutableListOf(ytDlpPath, target, "--newline", "--no-playlist", "--progress", "-o", outputTemplate)

            if (format == "MP3") {
                command.addAll(listOf("-x", "--audio-format", "mp3", "--audio-quality", "5"))
            } else {
                val h = if (quality == "1080p") "1080" else "720"
                command.addAll(listOf("-f", "bestvideo[height<=$h][vcodec^=avc1]+bestaudio[acodec^=mp4a]/best", "--merge-output-format", "mp4"))
            }

            // Adiciona localização do FFmpeg se necessário
            if (ffmpegPath.contains("/") || ffmpegPath.contains("\\")) {
                command.addAll(listOf("--ffmpeg-location", File(ffmpegPath).parent))
            }

            // Captura o caminho final real via print
            command.addAll(listOf("--print", "after_move:filepath"))

            val process = ProcessBuilder(command).redirectErrorStream(true).start()
            var finalFilePath: String? = null

            process.inputStream.bufferedReader().use { reader ->
                reader.forEachLine { line ->
                    // Tenta capturar progresso
                    progressRegex.find(line)?.let { match ->
                        val p = match.groupValues[1].toFloat() / 100f
                        onProgress(p, "${match.groupValues[2]}|${match.groupValues[3]}|${match.groupValues[4]}")
                    }

                    // Se a linha for um caminho absoluto, yt-dlp imprimiu via --print after_move
                    if (line.startsWith("/") || (line.length > 2 && line[1] == ':')) {
                        finalFilePath = line.trim()
                    }
                }
            }

            if (process.waitFor() == 0 && finalFilePath != null) {
                val file = File(finalFilePath!!)
                DownloadResult(file.nameWithoutExtension, file.absolutePath)
            } else {
                throw Exception("Download failed or file path not captured")
            }
        }
    }

    override fun openDownloadsFolder() {
        runCatching {
            val folder = File(System.getProperty("user.home"), "Music/MyDownloaderApp")
            if (folder.exists()) Desktop.getDesktop().open(folder)
        }
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
            if (os.contains("mac")) ProcessBuilder("open", "-R", file.absolutePath).start()
            else if (os.contains("win")) ProcessBuilder("explorer.exe", "/select,", file.absolutePath).start()
        } catch (e: Exception) { e.printStackTrace() }
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

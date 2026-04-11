package com.example.boilerplate.downloader

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.zip.ZipInputStream

object BinaryManager {
    private val cacheDir = File(System.getProperty("user.home"), ".music_downloader_cache/bin")
    private val os = System.getProperty("os.name").lowercase()
    private val isWindows = os.contains("win")
    private val isMac = os.contains("mac")
    private val isLinux = os.contains("nix") || os.contains("nux") || os.contains("aix")

    private val YT_DLP_URL = when {
        isWindows -> "https://github.com/yt-dlp/yt-dlp/releases/latest/download/yt-dlp.exe"
        isMac -> "https://github.com/yt-dlp/yt-dlp/releases/latest/download/yt-dlp_macos"
        isLinux -> "https://github.com/yt-dlp/yt-dlp/releases/latest/download/yt-dlp"
        else -> "https://github.com/yt-dlp/yt-dlp/releases/latest/download/yt-dlp"
    }

    private val FFMPEG_URL = when {
        isWindows -> "https://www.gyan.dev/ffmpeg/builds/ffmpeg-release-essentials.zip"
        isMac -> "https://evermeet.cx/ffmpeg/get/zip"
        else -> "" 
    }

    suspend fun ensureBinaries(onProgress: (String) -> Unit) = withContext(Dispatchers.IO) {
        if (!cacheDir.exists()) cacheDir.mkdirs()

        // 1. Verificar yt-dlp
        val ytDlpFile = File(cacheDir, if (isWindows) "yt-dlp.exe" else "yt-dlp")
        if (!ytDlpFile.exists() || ytDlpFile.length() < 1000000) {
            onProgress("Downloading yt-dlp...")
            downloadWithRetry(YT_DLP_URL, ytDlpFile)
            if (!isWindows) ytDlpFile.setExecutable(true)
        }

        // 2. Verificar FFmpeg
        val ffmpegFile = File(cacheDir, if (isWindows) "ffmpeg.exe" else "ffmpeg")
        val ffmpegInPath = isLinux && isCommandAvailable("ffmpeg")
        
        if (!ffmpegInPath && (!ffmpegFile.exists() || ffmpegFile.length() < 5000000)) {
            if (isLinux) {
                onProgress("Note: Please install 'ffmpeg' manually.")
            } else {
                onProgress("Downloading FFmpeg components...")
                downloadAndUnzipWithRetry(FFMPEG_URL, "ffmpeg")
                if (!isWindows) ffmpegFile.setExecutable(true)
            }
        }
    }

    private fun isCommandAvailable(command: String): Boolean {
        return try {
            val process = Runtime.getRuntime().exec(arrayOf("which", command))
            process.waitFor() == 0
        } catch (e: Exception) {
            false
        }
    }

    private fun downloadWithRetry(urlStr: String, target: File, retries: Int = 3) {
        var lastException: Exception? = null
        repeat(retries) { attempt ->
            try {
                val url = URL(urlStr)
                val connection = url.openConnection() as HttpURLConnection
                connection.connectTimeout = 15000 // 15s timeout
                connection.readTimeout = 30000    // 30s read timeout
                connection.instanceFollowRedirects = true
                
                connection.inputStream.use { input ->
                    target.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
                return // Success
            } catch (e: Exception) {
                lastException = e
                Thread.sleep(2000L * (attempt + 1))
            }
        }
        throw lastException ?: Exception("Failed to download $urlStr")
    }

    private fun downloadAndUnzipWithRetry(urlStr: String, binaryName: String, retries: Int = 3) {
        if (urlStr.isEmpty()) return
        var lastException: Exception? = null
        repeat(retries) { attempt ->
            try {
                val url = URL(urlStr)
                val connection = url.openConnection() as HttpURLConnection
                connection.connectTimeout = 20000
                connection.readTimeout = 60000
                connection.instanceFollowRedirects = true
                
                ZipInputStream(connection.inputStream).use { zip ->
                    var entry = zip.nextEntry
                    while (entry != null) {
                        if (entry.name.endsWith(binaryName) || entry.name.endsWith("$binaryName.exe")) {
                            val targetFile = File(cacheDir, if (isWindows) "$binaryName.exe" else binaryName)
                            FileOutputStream(targetFile).use { output ->
                                zip.copyTo(output)
                            }
                            zip.closeEntry()
                            return // Success
                        }
                        entry = zip.nextEntry
                    }
                }
                throw Exception("Binary $binaryName not found in zip")
            } catch (e: Exception) {
                lastException = e
                Thread.sleep(3000L * (attempt + 1))
            }
        }
        throw lastException ?: Exception("Failed to unzip $urlStr")
    }

    fun getPath(name: String): String {
        if (isLinux && isCommandAvailable(name)) return name
        val file = File(cacheDir, if (isWindows) "$name.exe" else name)
        return if (file.exists()) file.absolutePath else name
    }
}

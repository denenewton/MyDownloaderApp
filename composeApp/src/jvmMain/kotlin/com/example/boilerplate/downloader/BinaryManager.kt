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

    // Link exato para o binário independente no Mac (yt-dlp_macos)
    private val YT_DLP_URL = when {
        isWindows -> "https://github.com/yt-dlp/yt-dlp/releases/latest/download/yt-dlp.exe"
        isMac -> "https://github.com/yt-dlp/yt-dlp/releases/latest/download/yt-dlp_macos"
        else -> "https://github.com/yt-dlp/yt-dlp/releases/latest/download/yt-dlp"
    }

    private val FFMPEG_URL = when {
        isWindows -> "https://www.gyan.dev/ffmpeg/builds/ffmpeg-release-essentials.zip"
        isMac -> "https://evermeet.cx/ffmpeg/get/zip" // Baixa o ZIP com o binário
        else -> "https://johnvansickle.com/ffmpeg/releases/ffmpeg-release-amd64-static.tar.xz"
    }

    suspend fun ensureBinaries(onProgress: (String) -> Unit) = withContext(Dispatchers.IO) {
        if (!cacheDir.exists()) cacheDir.mkdirs()

        // 1. Verificar yt-dlp
        val ytDlpFile = File(cacheDir, if (isWindows) "yt-dlp.exe" else "yt-dlp")
        if (!ytDlpFile.exists() || ytDlpFile.length() < 100000) { // < 100kb é erro
            onProgress("Downloading yt-dlp (Standalone Mac version)...")
            downloadDirect(YT_DLP_URL, ytDlpFile)
            if (!isWindows) ytDlpFile.setExecutable(true)
        }

        // 2. Verificar FFmpeg
        val ffmpegFile = File(cacheDir, if (isWindows) "ffmpeg.exe" else "ffmpeg")
        if (!ffmpegFile.exists() || ffmpegFile.length() < 1000000) {
            onProgress("Downloading FFmpeg (this may take a minute)...")
            if (isMac || isWindows) {
                downloadAndUnzip(FFMPEG_URL, "ffmpeg")
            } else {
                downloadDirect(FFMPEG_URL, ffmpegFile)
            }
            if (!isWindows) ffmpegFile.setExecutable(true)
        }
    }

    private fun downloadDirect(urlStr: String, target: File) {
        val url = URL(urlStr)
        val connection = url.openConnection() as HttpURLConnection
        connection.instanceFollowRedirects = true
        connection.inputStream.use { input ->
            target.outputStream().use { output ->
                input.copyTo(output)
            }
        }
    }

    private fun downloadAndUnzip(urlStr: String, binaryName: String) {
        val url = URL(urlStr)
        val connection = url.openConnection() as HttpURLConnection
        connection.instanceFollowRedirects = true
        
        ZipInputStream(connection.inputStream).use { zip ->
            var entry = zip.nextEntry
            while (entry != null) {
                // Procura o arquivo ffmpeg ou ffmpeg.exe dentro do zip
                if (entry.name.endsWith(binaryName) || entry.name.endsWith("$binaryName.exe")) {
                    val targetFile = File(cacheDir, if (isWindows) "$binaryName.exe" else binaryName)
                    FileOutputStream(targetFile).use { output ->
                        zip.copyTo(output)
                    }
                    zip.closeEntry()
                    return
                }
                entry = zip.nextEntry
            }
        }
    }

    fun getPath(name: String): String {
        val file = File(cacheDir, if (isWindows) "$name.exe" else name)
        return if (file.exists()) file.absolutePath else name
    }
}

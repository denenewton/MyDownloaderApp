package com.example.boilerplate.downloader

interface MediaDownloader {
    suspend fun fetchMetadata(query: String): Result<MediaMetadata>
    suspend fun fetchPlaylistMetadata(url: String): Result<List<MediaMetadata>>
    suspend fun download(
        query: String,
        format: String,
        quality: String,
        onProgress: (Float, String) -> Unit
    ): Result<DownloadResult>
    
    fun openDownloadsFolder()
    fun openFile(path: String)
    fun shareFile(path: String)
    fun deleteFile(path: String): Boolean
}

data class MediaMetadata(
    val title: String,
    val artist: String = "Unknown",
    val album: String = "Unknown",
    val duration: String,
    val thumbnailUrl: String,
    val fileSize: String,
    val url: String? = null
)

data class DownloadResult(
    val title: String,
    val filePath: String
)

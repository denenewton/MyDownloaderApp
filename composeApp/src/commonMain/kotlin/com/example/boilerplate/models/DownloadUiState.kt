package com.example.boilerplate.models

import com.example.boilerplate.downloader.MediaMetadata

/**
 * Representa o que foi encontrado na busca. 
 * Pode ser nada (Idle), uma mídia única ou uma playlist.
 */
sealed interface SearchResult {
    object Idle : SearchResult
    data class Single(val metadata: MediaMetadata) : SearchResult
    data class Playlist(
        val name: String,
        val items: List<MediaMetadata>,
        val selectedIndices: Set<Int>,
        val currentDownloadIndex: Int = 0
    ) : SearchResult
}

enum class SearchType {
    NONE, SINGLE, PLAYLIST
}

data class DownloadUiState(
    val query: String = "",
    val isSearching: Boolean = false,
    val searchType: SearchType = SearchType.NONE,
    val isDownloading: Boolean = false,
    val progress: Float = 0f,
    val status: String = "",
    val speed: String = "",
    val eta: String = "",
    val downloadedSize: String = "",
    val error: String? = null,
    val downloadFormat: String = "MP3",
    val selectedQuality: String = "720p",
    
    // Agora todo o resultado da busca fica encapsulado aqui
    val result: SearchResult = SearchResult.Idle
)

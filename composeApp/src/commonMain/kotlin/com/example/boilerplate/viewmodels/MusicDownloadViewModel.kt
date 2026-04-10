package com.example.boilerplate.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.boilerplate.database.AppDatabaseMedia
import com.example.boilerplate.database.DownloadedItem
import com.example.boilerplate.downloader.MediaDownloader
import com.example.boilerplate.downloader.MediaMetadata
import com.example.boilerplate.models.DownloadUiState
import com.example.boilerplate.models.SearchResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * ViewModel responsável pela lógica de download de músicas e vídeos.
 */
class MusicDownloadViewModel(
    private val downloader: MediaDownloader,
    private val database: AppDatabaseMedia
) : ViewModel() {

    private val _uiState = MutableStateFlow(DownloadUiState())
    val uiState = _uiState.asStateFlow()

    // Histórico reativo filtrado por tipo
    val musicList = getFilteredHistory("MP3")
    val videoList = getFilteredHistory("MP4")

    private fun getFilteredHistory(type: String) = database.downloadDao().getAllDownloads()
        .map { list -> list.filter { it.type == type } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun onQueryChange(newQuery: String) {
        updateState { copy(query = newQuery) }
    }

    fun searchMetadata(query: String? = null, forcePlaylist: Boolean = false) {
        val searchTarget = query ?: _uiState.value.query
        if (searchTarget.isBlank()) return
        
        val isPlaylist = forcePlaylist || searchTarget.contains("list=") || searchTarget.contains("playlist?")
        val searchType = if (isPlaylist) com.example.boilerplate.models.SearchType.PLAYLIST else com.example.boilerplate.models.SearchType.SINGLE

        viewModelScope.launch {
            updateState { 
                copy(
                    isSearching = true, 
                    searchType = searchType,
                    status = "Searching...", 
                    error = null, 
                    query = searchTarget
                ) 
            }

            val result = if (isPlaylist) {
                downloader.fetchPlaylistMetadata(searchTarget)
            } else {
                downloader.fetchMetadata(searchTarget).map { listOf(it) }
            }

            result.onSuccess { items ->
                if (items.isEmpty()) {
                    updateState { copy(isSearching = false, searchType = com.example.boilerplate.models.SearchType.NONE, error = "No results found.") }
                    return@onSuccess
                }

                updateState {
                    copy(
                        isSearching = false,
                        searchType = com.example.boilerplate.models.SearchType.NONE,
                        result = if (isPlaylist) {
                            SearchResult.Playlist(
                                name = items.first().album,
                                items = items,
                                selectedIndices = items.indices.toSet()
                            )
                        } else {
                            SearchResult.Single(items.first())
                        },
                        status = if (isPlaylist) "${items.size} tracks found" else "Ready"
                    )
                }
            }.onFailure {
                updateState { copy(isSearching = false, searchType = com.example.boilerplate.models.SearchType.NONE, error = "Error: ${it.localizedMessage}") }
            }
        }
    }

    fun startDownload(format: String, quality: String) {
        if (_uiState.value.isDownloading) return

        viewModelScope.launch {
            val currentResult = _uiState.value.result
            if (currentResult is SearchResult.Idle) return@launch
            
            updateState { copy(isDownloading = true, progress = 0f, error = null) }

            val actualQuery = _uiState.value.query

            try {
                when (currentResult) {
                    is SearchResult.Single -> {
                        val downloadUrl = currentResult.metadata.url ?: actualQuery
                        if (downloadUrl.isBlank()) throw Exception("URL is empty")

                        performDownload(downloadUrl, currentResult.metadata, format, quality)
                        updateState { copy(status = "Completed!") }
                    }
                    is SearchResult.Playlist -> {
                        val selectedWithIndex = currentResult.items.mapIndexed { index, meta -> index to meta }
                            .filter { (index, _) -> currentResult.selectedIndices.contains(index) }

                        selectedWithIndex.forEachIndexed { displayIndex, (originalIndex, meta) ->
                            updateState {
                                copy(
                                    status = "Fetching details ${displayIndex + 1}/${selectedWithIndex.size}...",
                                    result = currentResult.copy(currentDownloadIndex = originalIndex)
                                )
                            }

                            val urlToFetch = meta.url ?: "ytsearch1:${meta.title}"
                            val fullMetadata = downloader.fetchMetadata(urlToFetch).getOrElse { meta }

                            updateState { copy(status = "Downloading ${displayIndex + 1}/${selectedWithIndex.size}") }

                            val downloadUrl = fullMetadata.url ?: meta.url ?: "ytsearch1:${meta.title}"
                            performDownload(downloadUrl, fullMetadata, format, quality)
                        }
                        updateState { copy(status = "All ${selectedWithIndex.size} items completed!") }
                    }
                    else -> {}
                }
            } catch (e: Exception) {
                e.printStackTrace()
                updateState { copy(error = "Task failed: ${e.localizedMessage ?: "Unknown error"}") }
            } finally {
                updateState { copy(isDownloading = false) }
            }
        }
    }

    private suspend fun performDownload(url: String, meta: MediaMetadata, format: String, quality: String) {
        downloader.download(url, format, quality) { p, s ->
            handleDownloadProgress(p, s)
        }.onSuccess { res ->
            val item = DownloadedItem(
                title = meta.title,
                artist = meta.artist,
                album = meta.album,
                duration = meta.duration,
                filePath = res.filePath,
                type = format,
                thumbnailUrl = meta.thumbnailUrl
            )
            database.downloadDao().insert(item)
        }.onFailure { error ->
            throw Exception(error.message ?: "Download failed")
        }
    }

    private fun handleDownloadProgress(p: Float, s: String) {
        val parts = s.split("|")
        if (parts.size >= 3) {
            updateState { copy(progress = p, downloadedSize = parts[0], speed = parts[1], eta = parts[2]) }
        } else {
            updateState { copy(progress = p, status = s) }
        }
    }

    private fun updateState(block: DownloadUiState.() -> DownloadUiState) {
        _uiState.value = _uiState.value.block()
    }

    fun togglePlaylistItem(index: Int) {
        val res = _uiState.value.result as? SearchResult.Playlist ?: return
        val newIndices = res.selectedIndices.toMutableSet().apply {
            if (contains(index)) remove(index) else add(index)
        }
        updateState { copy(result = res.copy(selectedIndices = newIndices)) }
    }

    fun toggleAllPlaylistItems(select: Boolean) {
        val res = _uiState.value.result as? SearchResult.Playlist ?: return
        updateState {
            copy(result = res.copy(selectedIndices = if (select) res.items.indices.toSet() else emptySet()))
        }
    }

    fun resetState(clearQuery: Boolean = false) {
        val currentQuery = _uiState.value.query
        _uiState.value = DownloadUiState().let { 
            if (!clearQuery) it.copy(query = currentQuery) else it
        }
    }

    fun openDownloads() = downloader.openDownloadsFolder()
    fun openFile(path: String) = downloader.openFile(path)
    fun shareFile(path: String) = downloader.shareFile(path)

    fun deleteItem(item: DownloadedItem) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                downloader.deleteFile(item.filePath)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            database.downloadDao().delete(item)
        }
    }
}
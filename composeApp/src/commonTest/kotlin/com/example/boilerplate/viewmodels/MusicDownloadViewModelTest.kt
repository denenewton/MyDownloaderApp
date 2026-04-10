package com.example.boilerplate.viewmodels

import com.example.boilerplate.database.AppDatabaseMedia
import com.example.boilerplate.database.DownloadDao
import com.example.boilerplate.database.DownloadedItem
import com.example.boilerplate.downloader.DownloadResult
import com.example.boilerplate.downloader.MediaDownloader
import com.example.boilerplate.downloader.MediaMetadata
import com.example.boilerplate.models.SearchResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class FakeMediaDownloader : MediaDownloader {
    override suspend fun fetchMetadata(query: String): Result<MediaMetadata> {
        return Result.success(MediaMetadata("Test Title", "Test Artist", "Test Album", "03:00", "http://thumb", "5MB", "http://url"))
    }

    override suspend fun fetchPlaylistMetadata(url: String): Result<List<MediaMetadata>> {
        return Result.success(listOf(MediaMetadata("Test Title", "Test Artist", "Test Album", "03:00", "http://thumb", "5MB", "http://url")))
    }

    override suspend fun download(query: String, format: String, quality: String, onProgress: (Float, String) -> Unit): Result<DownloadResult> {
        onProgress(0.5f, "Downloading...")
        onProgress(1.0f, "Completed")
        return Result.success(DownloadResult("Test Title", "/path/to/file"))
    }

    override fun openDownloadsFolder() {}
    override fun openFile(path: String) {}
    override fun shareFile(path: String) {}
    override fun deleteFile(path: String): Boolean = true
}

class FakeDownloadDao : DownloadDao {
    private val downloads = MutableStateFlow<List<DownloadedItem>>(emptyList())
    override suspend fun insert(item: DownloadedItem) {
        downloads.value = downloads.value + item
    }
    override fun getAllDownloads(): Flow<List<DownloadedItem>> = downloads
    override suspend fun delete(item: DownloadedItem) {
        downloads.value = downloads.value - item
    }
    fun clear() {
        downloads.value = emptyList()
    }
}

// We use a simple interface or a mock if possible, but faking the abstract class:
class FakeAppDatabaseMedia(private val dao: FakeDownloadDao) : AppDatabaseMedia() {
    override fun downloadDao(): DownloadDao = dao
    override fun clearAllTables() {
        dao.clear()
    }
    override fun createInvalidationTracker(): androidx.room.InvalidationTracker {
        return androidx.room.InvalidationTracker(this, emptyMap(), emptyMap(), "")
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class MusicDownloadViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var viewModel: MusicDownloadViewModel
    private lateinit var fakeDownloader: FakeMediaDownloader
    private lateinit var fakeDao: FakeDownloadDao
    private lateinit var fakeDatabase: AppDatabaseMedia

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        fakeDownloader = FakeMediaDownloader()
        fakeDao = FakeDownloadDao()
        fakeDatabase = FakeAppDatabaseMedia(fakeDao)
        viewModel = MusicDownloadViewModel(fakeDownloader, fakeDatabase)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testInitialState() {
        assertEquals("", viewModel.uiState.value.query)
        assertEquals(false, viewModel.uiState.value.isSearching)
    }

    @Test
    fun testQueryChange() {
        viewModel.onQueryChange("new query")
        assertEquals("new query", viewModel.uiState.value.query)
    }

    @Test
    fun testSearchMetadata() = runTest {
        viewModel.onQueryChange("test")
        viewModel.searchMetadata()
        advanceUntilIdle()
        assertEquals(false, viewModel.uiState.value.isSearching)
        assertEquals("Ready", viewModel.uiState.value.status)
        val result = viewModel.uiState.value.result
        kotlin.test.assertTrue(result is SearchResult.Single)
        assertEquals("Test Title", (result as SearchResult.Single).metadata.title)
    }
}

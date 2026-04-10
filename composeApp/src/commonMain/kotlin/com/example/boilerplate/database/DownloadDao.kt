package com.example.boilerplate.database


import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface DownloadDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: DownloadedItem)

    @Query("SELECT * FROM DownloadedItem ORDER BY timestamp DESC")
    fun getAllDownloads(): Flow<List<DownloadedItem>>

    @Delete
    suspend fun delete(item: DownloadedItem)

}

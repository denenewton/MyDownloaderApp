package com.example.boilerplate.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.datetime.Clock


// Entity (Common)
@Entity
data class DownloadedItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val artist: String = "Unknown",
    val album: String = "Unknown",
    val duration: String = "Unknown",
    val filePath: String,
    val type: String, // "MP3" or "MP4"
    val thumbnailUrl: String?,
    // Default value using Kotlinx Datetime to store the download time
    val timestamp: Long = Clock.System.now().toEpochMilliseconds()
)

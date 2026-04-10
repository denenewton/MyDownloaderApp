package com.example.boilerplate.di


import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.example.boilerplate.database.AppDatabaseMedia
import com.example.boilerplate.downloader.DesktopMediaDownloader0
import com.example.boilerplate.downloader.MediaDownloader
import org.koin.core.module.Module
import org.koin.dsl.module
import java.io.File

actual val platformModule: Module = module {
    // 1. Define how to create the Database on Desktop
    single<AppDatabaseMedia> {
        val dbFile = File(System.getProperty("user.home"), ".music_downloader_cache/downloads.db")
        Room.databaseBuilder<AppDatabaseMedia>(
            name = dbFile.absolutePath,
        )
            .setDriver(BundledSQLiteDriver()) // Required for Room KMP
            .fallbackToDestructiveMigration(true) // Added to handle schema changes by clearing data
            .build()
    }

    // 2. Define the Downloader implementation
    single<MediaDownloader> { DesktopMediaDownloader0() }
}

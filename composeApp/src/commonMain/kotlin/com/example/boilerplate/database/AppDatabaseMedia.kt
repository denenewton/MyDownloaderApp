package com.example.boilerplate.database


import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [DownloadedItem::class], version = 2)
abstract class AppDatabaseMedia : RoomDatabase() {
    abstract fun downloadDao(): DownloadDao
}

package com.example.boilerplate.database

import kotlin.test.Test
import kotlin.test.assertEquals

class DownloadedItemTest {
    @Test
    fun testDownloadedItemCreation() {
        val item = DownloadedItem(
            title = "Test Title",
            filePath = "/path/to/file",
            type = "MP3",
            thumbnailUrl = "http://example.com/thumb.jpg"
        )
        assertEquals("Test Title", item.title)
        assertEquals("MP3", item.type)
        assertEquals("Unknown", item.artist)
    }
}

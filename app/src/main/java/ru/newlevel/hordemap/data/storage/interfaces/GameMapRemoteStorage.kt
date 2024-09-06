package ru.newlevel.hordemap.data.storage.interfaces

import android.content.Context
import android.net.Uri

interface GameMapRemoteStorage {
    suspend fun downloadGameMapFromServer(context: Context, url: String): Uri?

    suspend fun getAllMapsAsList(): List<Triple<String, String, Long>>
}
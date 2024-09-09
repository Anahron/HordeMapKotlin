package ru.newlevel.hordemap.data.storage.interfaces

import android.content.Context
import android.net.Uri
import ru.newlevel.hordemap.data.storage.models.MapFileModel

interface GameMapRemoteStorage {
    suspend fun downloadGameMapFromServer(context: Context, url: String): Uri?

    suspend fun getAllMapsAsList(): List<MapFileModel>
}
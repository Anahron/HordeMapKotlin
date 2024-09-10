package ru.newlevel.hordemap.domain.repository

import android.content.Context
import android.net.Uri
import ru.newlevel.hordemap.data.storage.models.MapFileModel


interface GameMapRepository {
    suspend fun loadGameMapFromServer(context: Context, url: String): Uri?

    suspend fun getAllMapsAsList(): List<MapFileModel>
    suspend fun saveGameMapToFile(uri: Uri, suffix: String): Result<Uri?>
}
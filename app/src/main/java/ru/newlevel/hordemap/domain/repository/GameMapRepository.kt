package ru.newlevel.hordemap.domain.repository

import android.content.Context
import android.net.Uri


interface GameMapRepository {
    suspend fun loadGameMapFromServer(context: Context, url: String): Uri?

    suspend fun getAllMapsAsList(): List<Triple<String, String, Long>>
    suspend fun saveGameMapToFile(uri: Uri, suffix: String): Result<Uri?>
}
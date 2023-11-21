package ru.newlevel.hordemap.data.storage.interfaces

import android.net.Uri

interface GameMapLocalStorage {
    suspend fun saveGameMapToFile(uri: Uri, suffix: String)

    suspend fun loadLastMapFromFile(): Uri?
}
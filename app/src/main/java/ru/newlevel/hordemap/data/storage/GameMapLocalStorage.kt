package ru.newlevel.hordemap.data.storage

import android.net.Uri
import java.io.InputStream

interface GameMapLocalStorage {
    suspend fun saveGameMapToFile(uri: Uri)

    suspend fun loadLastMapFromFile(): InputStream?
}
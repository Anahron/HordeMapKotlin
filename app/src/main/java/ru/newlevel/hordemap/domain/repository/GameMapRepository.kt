package ru.newlevel.hordemap.domain.repository

import android.content.Context
import android.net.Uri
import java.io.InputStream


interface GameMapRepository {
    suspend fun loadGameMapFromServer(context: Context): Uri?

    suspend fun loadLastGameMap(): InputStream?

    suspend fun saveGameMapToFile(uri: Uri)
}
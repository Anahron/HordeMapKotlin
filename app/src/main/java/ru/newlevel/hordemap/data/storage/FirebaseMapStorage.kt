package ru.newlevel.hordemap.data.storage

import android.content.Context
import android.net.Uri

interface FirebaseMapStorage {
    suspend fun loadGameMapFromServer(context: Context): Uri?
}
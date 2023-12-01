package ru.newlevel.hordemap.data.storage.interfaces

import android.content.Context
import android.net.Uri

interface MessageFilesStorage {
    suspend fun uploadFile(uri: Uri, fileName: String?): Result<Uri>

    suspend fun downloadFile(context: Context, uri: Uri, fileName: String?): Result<Boolean>
}
package ru.newlevel.hordemap.data.storage.interfaces

import android.content.Context
import android.net.Uri
import androidx.lifecycle.MutableLiveData

interface MessageFilesStorage {
    suspend fun sendFile(message: String, uri: Uri, fileName: String?, fileSize: Long): String

    fun downloadFile(context: Context, uri: Uri, fileName: String?)

    fun getDownloadProgress(): MutableLiveData<Int>
}
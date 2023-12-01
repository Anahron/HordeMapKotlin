package ru.newlevel.hordemap.data.storage.interfaces

import android.content.Context
import android.net.Uri
import androidx.lifecycle.MutableLiveData

interface MessageFilesStorage {
    suspend fun uploadFile(uri: Uri, fileName: String?): Result<Uri>

    fun downloadFile(context: Context, uri: Uri, fileName: String?)

    fun getDownloadProgress(): MutableLiveData<Int>
}
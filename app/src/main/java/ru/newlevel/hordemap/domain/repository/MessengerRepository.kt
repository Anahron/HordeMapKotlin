package ru.newlevel.hordemap.domain.repository

import android.content.Context
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import ru.newlevel.hordemap.data.db.MyMessageEntity
import ru.newlevel.hordemap.data.storage.models.UserDataModel

interface MessengerRepository {
    suspend fun sendMessage(message: MyMessageEntity)

    fun getLocalMessageUpdate(): LiveData<List<MyMessageEntity>>

    fun deleteMessage(message: MyMessageEntity)
    fun stopMessageUpdate()

    suspend fun startMessageUpdate()

    suspend fun uploadFile(uri: Uri, fileName: String?): Result<Uri>

    suspend fun downloadFile(context: Context, uri: Uri, fileName: String?): Result<Boolean>

    fun getUsersProfiles(): MutableLiveData<List<UserDataModel>>
}
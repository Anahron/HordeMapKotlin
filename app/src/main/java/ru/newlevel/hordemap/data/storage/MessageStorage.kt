package ru.newlevel.hordemap.data.storage

import androidx.lifecycle.MutableLiveData
import ru.newlevel.hordemap.data.storage.models.MessageDataModel

interface MessageStorage {
    fun sendMessage(text: String)

    fun startMessageUpdate(): MutableLiveData<List<MessageDataModel>>

    fun stopMessageUpdate()
}
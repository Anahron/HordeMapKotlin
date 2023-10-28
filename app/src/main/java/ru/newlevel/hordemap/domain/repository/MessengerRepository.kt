package ru.newlevel.hordemap.domain.repository

import androidx.lifecycle.MutableLiveData
import ru.newlevel.hordemap.data.storage.models.MessageDataModel

interface MessengerRepository {
    fun sendMessage(text: String)

    fun startMessageUpdate(): MutableLiveData<List<MessageDataModel>>

    fun stopMessageUpdate()
}
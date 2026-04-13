package ru.newlevel.hordemap.presentation

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.Flow
import ru.newlevel.hordemap.domain.usecases.messengerCases.MessageUpdateInteractor

class MainViewModel(private val messageUpdateInteractor: MessageUpdateInteractor): ViewModel() {

    private val _newMessageAnnounced: Flow<Int> = messageUpdateInteractor.getNewMessageCountUpdate()
        val newMessageAnnounced: Flow<Int> = _newMessageAnnounced

   suspend fun syncMessageData(){
        messageUpdateInteractor.syncMessagesData()
    }
}
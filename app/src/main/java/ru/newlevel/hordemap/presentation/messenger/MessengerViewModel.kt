package ru.newlevel.hordemap.presentation.messenger

import android.content.Context
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ru.newlevel.hordemap.data.storage.models.MessageDataModel
import ru.newlevel.hordemap.domain.models.UserDomainModel
import ru.newlevel.hordemap.domain.usecases.messengerCases.MessengerUseCases

class MessengerViewModel(
    private val messengerUseCases: MessengerUseCases
) : ViewModel() {

    private var messagesMutableLiveData = MutableLiveData<List<MessageDataModel>>()
    val messagesLiveData get(): LiveData<List<MessageDataModel>> = messagesMutableLiveData

    private var progressMutableLiveData = MutableLiveData<Int>()
    val progressLiveData get(): LiveData<Int> = progressMutableLiveData

    private var usersProfileMutableLiveData = MutableLiveData<List<UserDomainModel>>()
    val usersProfileLiveData get(): LiveData<List<UserDomainModel>> = usersProfileMutableLiveData

    suspend fun startMessageUpdate() {
        usersProfileMutableLiveData = messengerUseCases.startMessageUpdateInteractor.getUsersProfiles()
        messagesMutableLiveData = messengerUseCases.startMessageUpdateInteractor.getMessageUpdate()
        progressMutableLiveData = messengerUseCases.startMessageUpdateInteractor.getDownloadProgress()

    }

    fun stopMessageUpdate() {
        messengerUseCases.stopMessageUpdateInteractor.execute()
    }

    fun sendMessage(text: String) {
        messengerUseCases.sendMessageUseCase.execute(text)
    }

    fun sendFile(message: String, uri: Uri, fileName: String?, fileSize: Long) {
        CoroutineScope(Dispatchers.IO).launch {
            messengerUseCases.sendFileUseCase.execute(message, uri, fileName, fileSize)
        }
    }

    fun downloadFile(context: Context, uri: Uri, fileName: String?) {
        fileName?.let { messengerUseCases.downloadFileUseCase.execute(context, uri, it) }
    }
}

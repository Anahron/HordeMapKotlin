package ru.newlevel.hordemap.presentation.messenger

import android.content.Context
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ru.newlevel.hordemap.data.db.MyMessageEntity
import ru.newlevel.hordemap.domain.models.UserDomainModel
import ru.newlevel.hordemap.domain.usecases.messengerCases.MessengerUseCases

class MessengerViewModel(
    private val messengerUseCases: MessengerUseCases
) : ViewModel() {

    var messagesLiveData = messengerUseCases.startMessageUpdateInteractor.getMessageUpdate()

    private var progressMutableLiveData = MutableLiveData<Int>()
    val progressLiveData get(): LiveData<Int> = progressMutableLiveData

    private var usersProfileMutableLiveData = MutableLiveData<List<UserDomainModel>>()
    val usersProfileLiveData get(): LiveData<List<UserDomainModel>> = usersProfileMutableLiveData

    suspend fun startMessageUpdate() {
        usersProfileMutableLiveData = messengerUseCases.startMessageUpdateInteractor.getUsersProfiles()
        progressMutableLiveData = messengerUseCases.startMessageUpdateInteractor.getDownloadProgress()
        messengerUseCases.startMessageUpdateInteractor.startMessageUpdate()
    }

    fun stopMessageUpdate() {
        messengerUseCases.stopMessageUpdateInteractor.execute()
    }

    fun deleteMessage(message: MyMessageEntity){
        messengerUseCases.deleteMessageUseCase.execute(message)
    }
    suspend fun sendMessage(text: String, replyId: Long?, editMessage: Long?) {
        messengerUseCases.sendMessageUseCase.execute(text, replyId, editMessage = editMessage)
    }

    suspend fun sendFile(message: String, uri: Uri, fileName: String?, fileSize: Long): Result<Uri> =
        withContext(Dispatchers.IO) {
            messengerUseCases.uploadFileUseCase.execute(message, uri, fileName, fileSize)
    }

    fun downloadFile(context: Context, uri: Uri, fileName: String?) {
        fileName?.let { messengerUseCases.downloadFileUseCase.execute(context, uri, it) }
    }
}

package ru.newlevel.hordemap.presentation.messenger

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import ru.newlevel.hordemap.data.db.MyMessageEntity
import ru.newlevel.hordemap.domain.models.UserDomainModel
import ru.newlevel.hordemap.domain.usecases.messengerCases.MessengerUseCases

class MessengerViewModel(private val messengerUseCases: MessengerUseCases) : ViewModel() {

    private val _messagesDataFlow: Flow<List<MyMessageEntity>> = messengerUseCases.messageUpdateInteractor.getMessageUpdate()
    val messagesDataFlow get(): Flow<List<MyMessageEntity>> = _messagesDataFlow

    private val _usersProfileDataFlow: Flow<List<UserDomainModel>> =  messengerUseCases.messageUpdateInteractor.getUsersProfiles()
    val usersProfileDataFlow get(): Flow<List<UserDomainModel>> = _usersProfileDataFlow

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

    suspend fun downloadFile(context: Context, uri: Uri, fileName: String?) : Result<Boolean>{
       return if (fileName != null) messengerUseCases.downloadFileUseCase.execute(context, uri, fileName) else Result.failure(
           Throwable("Unknown file"))
    }
}

package ru.newlevel.hordemap.presentatin.viewmodels

import android.content.Context
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ru.newlevel.hordemap.data.storage.models.MessageDataModel
import ru.newlevel.hordemap.domain.usecases.*

class MessengerViewModel(
    private val stopMessageUpdateUseCase: StopMessageUpdateUseCase,
    private val startMessageUpdateUseCase: StartMessageUpdateUseCase,
    private val sendMessageUseCase: SendMessageUseCase,
    private val sendFileUseCase: SendFileUseCase,
    private val downloadFileUseCase: DownloadFileUseCase
) : ViewModel() {

    private var messagesMutableLiveData = MutableLiveData<List<MessageDataModel>>()
    val messagesLiveData get(): LiveData<List<MessageDataModel>> = messagesMutableLiveData

    private var progressMutableLiveData = MutableLiveData<Int>()
    val progressLiveData get(): LiveData<Int> = progressMutableLiveData


    fun startMessageUpdate() {
        messagesMutableLiveData = startMessageUpdateUseCase.execute()
        progressMutableLiveData = startMessageUpdateUseCase.getDownloadProgress()
    }

    fun stopMessageUpdate() {
        stopMessageUpdateUseCase.execute()
    }

    fun sendMessage(text: String) {
        sendMessageUseCase.execute(text)
    }

    fun sendFile(message: String, uri: Uri, fileName: String?, fileSize: Long) {
        CoroutineScope(Dispatchers.IO).launch {
            sendFileUseCase.execute(message, uri, fileName, fileSize)
        }
    }

    fun downloadFile(context: Context, uri: Uri, fileName: String?) {
        fileName?.let { downloadFileUseCase.execute(context, uri, it) }
    }
}

package ru.newlevel.hordemap.domain.usecases

import android.net.Uri
import ru.newlevel.hordemap.domain.repository.MessengerRepository

class SendFileUseCase(private val messengerRepository: MessengerRepository) {
    suspend fun execute(message: String, uri: Uri, fileName: String?, fileSize: Long) {
        val remoteUri = messengerRepository.sendFile(message, uri, fileName, fileSize)
        if (remoteUri.isNotEmpty())
            messengerRepository.sendMessage(message, remoteUri, fileSize, fileName?: "")
    }
}
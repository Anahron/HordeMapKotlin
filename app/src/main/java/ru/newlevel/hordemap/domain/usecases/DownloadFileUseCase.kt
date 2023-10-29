package ru.newlevel.hordemap.domain.usecases

import android.content.Context
import android.net.Uri
import ru.newlevel.hordemap.domain.repository.MessengerRepository

class DownloadFileUseCase(private val messengerRepository: MessengerRepository) {
    fun execute(context: Context, uri: Uri, fileName: String) {
        messengerRepository.downloadFile(context, uri, fileName)
    }
}
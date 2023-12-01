package ru.newlevel.hordemap.domain.usecases.messengerCases

import android.content.Context
import android.net.Uri
import ru.newlevel.hordemap.domain.repository.MessengerRepository

class DownloadFileUseCase(private val messengerRepository: MessengerRepository) {
    suspend fun execute(context: Context, uri: Uri, fileName: String): Result<Boolean>  = messengerRepository.downloadFile(context, uri, fileName)
}
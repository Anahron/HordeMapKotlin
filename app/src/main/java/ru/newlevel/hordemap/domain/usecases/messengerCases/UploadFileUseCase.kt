package ru.newlevel.hordemap.domain.usecases.messengerCases

import android.net.Uri
import ru.newlevel.hordemap.data.db.MyMessageEntity
import ru.newlevel.hordemap.data.db.UserEntityProvider
import ru.newlevel.hordemap.domain.repository.MessengerRepository

class UploadFileUseCase(private val messengerRepository: MessengerRepository) {
    suspend fun execute(text: String, uri: Uri, fileName: String?, fileSize: Long): Result<Uri> {
        val result = messengerRepository.uploadFile(uri, fileName)
        val user = UserEntityProvider.userEntity
        result.onSuccess {
            val messageEntity = MyMessageEntity(
                timestamp = System.currentTimeMillis(),
                userName = user.name,
                message = text,
                deviceID = user.deviceID,
                profileImageUrl = user.profileImageUrl,
                selectedMarker = user.selectedMarker,
                fileName = fileName?: "",
                fileSize = fileSize,
                url = it.toString()
            )
            messengerRepository.sendMessage(messageEntity)
        }
        return result
    }
}
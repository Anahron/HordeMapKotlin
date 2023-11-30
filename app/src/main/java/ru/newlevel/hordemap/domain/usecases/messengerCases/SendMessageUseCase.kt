package ru.newlevel.hordemap.domain.usecases.messengerCases

import ru.newlevel.hordemap.data.db.MyMessageEntity
import ru.newlevel.hordemap.data.db.UserEntityProvider
import ru.newlevel.hordemap.domain.repository.MessengerRepository

class SendMessageUseCase(private val messengerRepository: MessengerRepository) {
    suspend fun execute(text: String, replyOn: String) {
        val user = UserEntityProvider.userEntity
        val messageEntity = MyMessageEntity(
            timestamp = System.currentTimeMillis(),
            userName = user.name,
            message = text,
            deviceID = user.deviceID,
            profileImageUrl = user.profileImageUrl,
            selectedMarker = user.selectedMarker,
            replyOn = if (replyOn.isNotEmpty()) replyOn.toLong() else 0
        )
        messengerRepository.sendMessage(messageEntity)
    }
}
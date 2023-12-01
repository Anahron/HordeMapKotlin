package ru.newlevel.hordemap.domain.usecases.messengerCases

import ru.newlevel.hordemap.data.db.MyMessageEntity
import ru.newlevel.hordemap.data.db.UserEntityProvider
import ru.newlevel.hordemap.domain.repository.MessengerRepository

class SendMessageUseCase(private val messengerRepository: MessengerRepository) {
    suspend fun execute(text: String, replyOn: Long?, editMessage: Long?) {
        val user = UserEntityProvider.userEntity
        val messageEntity = MyMessageEntity(
            timestamp = editMessage ?: System.currentTimeMillis(),
            userName = user.name,
            message = text,
            deviceID = user.deviceID,
            profileImageUrl = user.profileImageUrl,
            selectedMarker = user.selectedMarker,
            replyOn = replyOn ?: 0
        )
        messengerRepository.sendMessage(messageEntity)
    }
}
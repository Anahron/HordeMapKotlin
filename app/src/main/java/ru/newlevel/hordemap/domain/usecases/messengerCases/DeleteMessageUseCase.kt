package ru.newlevel.hordemap.domain.usecases.messengerCases

import ru.newlevel.hordemap.data.db.MyMessageEntity
import ru.newlevel.hordemap.domain.repository.MessengerRepository

class DeleteMessageUseCase(private val messengerRepository: MessengerRepository) {
    fun execute(message: MyMessageEntity) {
        messengerRepository.deleteRemoteMessage(message)
    }
}
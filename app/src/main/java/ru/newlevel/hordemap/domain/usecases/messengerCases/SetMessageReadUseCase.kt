package ru.newlevel.hordemap.domain.usecases.messengerCases

import ru.newlevel.hordemap.domain.repository.MessengerRepository

class SetMessageReadUseCase(private val messengerRepository: MessengerRepository) {
    suspend fun execute(id: Long) {
      messengerRepository.setMessageRead(id)
    }
}
package ru.newlevel.hordemap.domain.usecases.messengerCases

import ru.newlevel.hordemap.domain.repository.MessengerRepository

class StopMessageUpdateUseCase(private val messengerRepository: MessengerRepository) {
    fun execute(){
        messengerRepository.stopMessageUpdate()
    }
}
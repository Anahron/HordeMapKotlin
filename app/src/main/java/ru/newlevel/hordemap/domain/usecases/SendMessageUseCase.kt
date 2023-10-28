package ru.newlevel.hordemap.domain.usecases

import ru.newlevel.hordemap.domain.repository.MessengerRepository

class SendMessageUseCase(private val messengerRepository: MessengerRepository) {
    fun execute(text: String){
        messengerRepository.sendMessage(text)
    }
}
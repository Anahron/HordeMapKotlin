package ru.newlevel.hordemap.domain.usecases

import ru.newlevel.hordemap.domain.repository.UserRepository

class ResetUserUseCase(private val userRepository: UserRepository)  {
    fun execute() {
        return userRepository.resetUser()
    }
}
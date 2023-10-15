package ru.newlevel.hordemap.domain.usecases

import ru.newlevel.hordemap.domain.repository.UserRepository

class SaveAutoLoadUseCase(private val userRepository: UserRepository) {

    fun execute(boolean: Boolean) {
        userRepository.saveAutoLoad(boolean)
    }
}
package ru.newlevel.hordemap.domain.usecases

import ru.newlevel.hordemap.domain.models.UserDomainModel
import ru.newlevel.hordemap.domain.repository.UserRepository

class SaveUserUseCase(private val userRepository: UserRepository) {
    fun execute(user: UserDomainModel){
        userRepository.saveUser(user)
        userRepository.getUser()
    }
}
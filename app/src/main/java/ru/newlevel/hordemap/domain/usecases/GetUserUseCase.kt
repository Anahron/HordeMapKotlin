package ru.newlevel.hordemap.domain.usecases

import ru.newlevel.hordemap.domain.models.UserDomainModel

class GetUserUseCase {
    fun execute(): UserDomainModel{
        return UserDomainModel("Dron", 1, 1)
    }
}
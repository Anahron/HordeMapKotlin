package ru.newlevel.hordemap.domain.usecases.mapCases

import ru.newlevel.hordemap.app.mapToDomainModel
import ru.newlevel.hordemap.data.db.UserEntityProvider
import ru.newlevel.hordemap.domain.models.UserDomainModel
import ru.newlevel.hordemap.domain.repository.SettingsRepository

class GetUserSettingsUseCase(private val settingsRepository: SettingsRepository) {
    fun execute(): UserDomainModel {
        val user = settingsRepository.getUser()
        UserEntityProvider.userEntity = user.mapToDomainModel()
        return user.mapToDomainModel()
    }
}
package ru.newlevel.hordemap.domain.usecases.mapCases

import ru.newlevel.hordemap.domain.models.UserDomainModel
import ru.newlevel.hordemap.domain.repository.SettingsRepository

class GetSettingsUseCase(private val settingsRepository: SettingsRepository) {
    fun execute(): UserDomainModel {
        return settingsRepository.getUser()
    }
}
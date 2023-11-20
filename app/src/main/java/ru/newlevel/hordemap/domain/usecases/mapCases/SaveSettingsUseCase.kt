package ru.newlevel.hordemap.domain.usecases.mapCases

import ru.newlevel.hordemap.domain.models.UserDomainModel
import ru.newlevel.hordemap.domain.repository.SettingsRepository

class SaveSettingsUseCase(private val settingsRepository: SettingsRepository) {
    fun execute(user: UserDomainModel){
        settingsRepository.saveUser(user)
    }
}
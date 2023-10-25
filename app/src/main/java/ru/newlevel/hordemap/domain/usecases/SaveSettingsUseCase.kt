package ru.newlevel.hordemap.domain.usecases

import ru.newlevel.hordemap.domain.models.UserDomainModel
import ru.newlevel.hordemap.domain.repository.SettingsRepository

class SaveSettingsUseCase(private val settingsRepository: SettingsRepository) {
    fun execute(user: UserDomainModel){
        settingsRepository.saveUser(user)
    }
}
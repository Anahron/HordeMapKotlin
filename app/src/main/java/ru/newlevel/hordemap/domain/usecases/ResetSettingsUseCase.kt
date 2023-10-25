package ru.newlevel.hordemap.domain.usecases

import ru.newlevel.hordemap.domain.repository.SettingsRepository

class ResetSettingsUseCase(private val settingsRepository: SettingsRepository)  {
    fun execute() {
        return settingsRepository.resetUser()
    }
}
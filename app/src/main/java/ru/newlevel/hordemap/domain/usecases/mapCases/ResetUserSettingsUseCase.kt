package ru.newlevel.hordemap.domain.usecases.mapCases

import ru.newlevel.hordemap.domain.repository.SettingsRepository

class ResetUserSettingsUseCase(private val settingsRepository: SettingsRepository)  {
    fun execute() = settingsRepository.resetUserSettings()
}
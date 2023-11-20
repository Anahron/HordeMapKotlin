package ru.newlevel.hordemap.domain.usecases.mapCases

import ru.newlevel.hordemap.domain.repository.SettingsRepository

class SaveAutoLoadUseCase(private val settingsRepository: SettingsRepository) {

    fun execute(boolean: Boolean) {
        settingsRepository.saveAutoLoad(boolean)
    }
}
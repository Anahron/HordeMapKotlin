package ru.newlevel.hordemap.domain.usecases

import ru.newlevel.hordemap.domain.repository.SettingsRepository

class SaveAutoLoadUseCase(private val settingsRepository: SettingsRepository) {

    fun execute(boolean: Boolean) {
        settingsRepository.saveAutoLoad(boolean)
    }
}
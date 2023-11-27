package ru.newlevel.hordemap.domain.usecases

import ru.newlevel.hordemap.data.db.UserEntityProvider
import ru.newlevel.hordemap.domain.repository.SettingsRepository

class LogOutUseCase(private val settingsRepository: SettingsRepository) {
    suspend fun execute(){
        settingsRepository.deleteUserDataRemote(UserEntityProvider.userEntity.deviceID)
    }
}
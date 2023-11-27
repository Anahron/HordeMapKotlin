package ru.newlevel.hordemap.domain.usecases

import ru.newlevel.hordemap.app.mapToDataModel
import ru.newlevel.hordemap.domain.models.UserDomainModel
import ru.newlevel.hordemap.domain.repository.SettingsRepository

class SendUserToStorageUseCase(private val settingsRepository: SettingsRepository) {
    suspend fun execute(userDomainModel: UserDomainModel){
       settingsRepository.sendUserToStorage(userDomainModel.mapToDataModel())
    }
}
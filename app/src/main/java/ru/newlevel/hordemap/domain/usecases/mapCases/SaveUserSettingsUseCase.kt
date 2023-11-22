package ru.newlevel.hordemap.domain.usecases.mapCases

import ru.newlevel.hordemap.app.mapToDataModel
import ru.newlevel.hordemap.data.db.UserEntityProvider
import ru.newlevel.hordemap.domain.models.UserDomainModel
import ru.newlevel.hordemap.domain.repository.SettingsRepository

class SaveUserSettingsUseCase(private val settingsRepository: SettingsRepository) {
    fun execute(userDomainModel: UserDomainModel){
        UserEntityProvider.userEntity = userDomainModel
        settingsRepository.saveUser(userDomainModel.mapToDataModel())
    }
}
package ru.newlevel.hordemap.presentation.messenger.userGroup

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.Flow
import ru.newlevel.hordemap.domain.models.UserDomainModel
import ru.newlevel.hordemap.domain.usecases.messengerCases.MessengerUseCases

class UserGroupViewModel(messengerUseCases: MessengerUseCases) : ViewModel() {

    private val _usersProfileDataFlow: Flow<List<UserDomainModel>> =  messengerUseCases.messageUpdateInteractor.getUsersProfiles()
    val usersProfileDataFlow get(): Flow<List<UserDomainModel>> = _usersProfileDataFlow

}

package ru.newlevel.hordemap.presentatin

import androidx.lifecycle.ViewModel
import ru.newlevel.hordemap.domain.models.UserDomainModel
import ru.newlevel.hordemap.domain.usecases.GetUserUseCase
import ru.newlevel.hordemap.domain.usecases.SaveUserUseCase

private val getUserUseCase = GetUserUseCase()
private val saveUserUseCase = SaveUserUseCase()

class LoginVM: ViewModel() {
    fun saveUser(name: String, marker: Int){
        val user = UserDomainModel(name, marker, 1)
        saveUserUseCase.execute(user)
    }

    fun getUser(): Boolean{
        getUserUseCase.execute().name
        return false;
    }
}
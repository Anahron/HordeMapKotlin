package ru.newlevel.hordemap.presentatin


import android.util.Log
import androidx.lifecycle.ViewModel
import ru.newlevel.hordemap.data.repository.UserRepositoryImpl
import ru.newlevel.hordemap.domain.models.UserDomainModel
import ru.newlevel.hordemap.domain.usecases.GetUserUseCase
import ru.newlevel.hordemap.domain.usecases.SaveUserUseCase


class LoginVM(private val getUserUseCase: GetUserUseCase, private val saveUserUseCase: SaveUserUseCase): ViewModel() {

    init {
        Log.e("AAA", "LoginVM created")
    }
    fun saveUser(userDomainModel: UserDomainModel){
        saveUserUseCase.execute(userDomainModel)
    }

    fun getUser(): UserDomainModel{
        return getUserUseCase.execute();
    }
}
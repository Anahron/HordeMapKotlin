package ru.newlevel.hordemap.domain.repository

import android.net.Uri
import ru.newlevel.hordemap.domain.models.UserDomainModel

interface UserRepository {

    fun saveUser(userDomainModel: UserDomainModel)

    fun getUser(): UserDomainModel

    fun resetUser()

    fun saveUri(uri: Uri)

    fun getMapUri(): Uri
}
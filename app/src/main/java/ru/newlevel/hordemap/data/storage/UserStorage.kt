package ru.newlevel.hordemap.data.storage

import android.net.Uri
import ru.newlevel.hordemap.data.storage.models.UserDataModel


interface UserStorage {
    fun save(userDataModel: UserDataModel)

    fun get(): UserDataModel

    fun reset()

    fun saveUri(uri: Uri)

    fun getMapUri(): Uri
}


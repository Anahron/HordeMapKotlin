package ru.newlevel.hordemap.data.repository

import android.content.Context
import android.content.SharedPreferences
import ru.newlevel.hordemap.domain.models.UserDomainModel
import ru.newlevel.hordemap.domain.repository.UserRepository
import ru.newlevel.hordemap.domain.usecases.Utils

const val SHARE_PREFS_NAME = "sharedHordeMap"
const val KEY_NAME = "userName"
const val KEY_MARKER = "userMarker"
const val KEY_TIME_TO_SEND_DATA = "timeToSend"
const val KEY_STATIC_MARKER_SIZE = "staticMarkerSize"
const val KEY_USERS_MARKER_SIZE = "usersMarkerSize"
const val DEFAULT_SIZE = 60
const val DEFAULT_TIME = 30

class UserRepositoryImpl(private val context: Context): UserRepository {

    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(SHARE_PREFS_NAME, Context.MODE_PRIVATE)

    override fun saveUser(userDomainModel: UserDomainModel) {
        sharedPreferences.edit().putString(KEY_NAME, userDomainModel.name).apply()
        sharedPreferences.edit().putInt(KEY_MARKER, userDomainModel.selectedMarker).apply()
        sharedPreferences.edit().putInt(KEY_TIME_TO_SEND_DATA, userDomainModel.timeToSendData).apply()
        sharedPreferences.edit().putInt(KEY_STATIC_MARKER_SIZE, userDomainModel.staticMarkerSize).apply()
        sharedPreferences.edit().putInt(KEY_USERS_MARKER_SIZE, userDomainModel.usersMarkerSize).apply()
    }

    override fun getUser(): UserDomainModel {
        val userName = sharedPreferences.getString(KEY_NAME, "") ?: ""
        val selectedMarker = sharedPreferences.getInt(KEY_MARKER, 0)
        val timeToSend = sharedPreferences.getInt(KEY_TIME_TO_SEND_DATA, DEFAULT_TIME)
        val staticMarkerSize = sharedPreferences.getInt(KEY_STATIC_MARKER_SIZE, DEFAULT_SIZE)
        val usersMarkerSize = sharedPreferences.getInt(KEY_USERS_MARKER_SIZE, DEFAULT_SIZE)
        return UserDomainModel(userName,timeToSend,usersMarkerSize,staticMarkerSize,selectedMarker, Utils.getDeviceId(context))
    }

    override fun resetUser() {
        sharedPreferences.edit()
             //TODO удплить .remove(KEY_NAME)
            .remove(KEY_NAME)
            .remove(KEY_MARKER)
            .remove(KEY_TIME_TO_SEND_DATA)
            .remove(KEY_STATIC_MARKER_SIZE)
            .remove(KEY_USERS_MARKER_SIZE)
            .apply()
    }
}
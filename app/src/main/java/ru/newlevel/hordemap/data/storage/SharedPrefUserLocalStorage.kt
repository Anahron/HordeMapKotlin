package ru.newlevel.hordemap.data.storage

import android.content.Context
import android.content.SharedPreferences
import ru.newlevel.hordemap.app.getDeviceId
import ru.newlevel.hordemap.data.storage.interfaces.UserLocalStorage
import ru.newlevel.hordemap.data.storage.models.UserDataModel


const val SHARE_PREFS_NAME = "sharedHordeMap"
const val KEY_NAME = "userName"
const val KEY_MARKER = "userMarker"
const val KEY_TIME_TO_SEND_DATA = "timeToSend"
const val KEY_STATIC_MARKER_SIZE = "staticMarkerSize"
const val KEY_USERS_MARKER_SIZE = "usersMarkerSize"
const val DEFAULT_SIZE = 60
const val DEFAULT_TIME = 30
const val KEY_IS_AUTO_LOAD = "isAutoLoad"

class SharedPrefUserLocalStorage(private val context: Context): UserLocalStorage {

    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(SHARE_PREFS_NAME, Context.MODE_PRIVATE)

    override fun save(userDataModel: UserDataModel) {
        sharedPreferences.edit().putBoolean(KEY_IS_AUTO_LOAD, userDataModel.autoLoad).apply()
        sharedPreferences.edit().putString(KEY_NAME, userDataModel.name).apply()
        sharedPreferences.edit().putInt(KEY_MARKER, userDataModel.selectedMarker).apply()
        sharedPreferences.edit().putInt(KEY_TIME_TO_SEND_DATA, userDataModel.timeToSendData).apply()
        sharedPreferences.edit().putInt(KEY_STATIC_MARKER_SIZE, userDataModel.staticMarkerSize).apply()
        sharedPreferences.edit().putInt(KEY_USERS_MARKER_SIZE, userDataModel.usersMarkerSize).apply()
    }

    override fun get(): UserDataModel {
        val userName = sharedPreferences.getString(KEY_NAME, "") ?: ""
        val selectedMarker = sharedPreferences.getInt(KEY_MARKER, 0)
        val timeToSend = sharedPreferences.getInt(KEY_TIME_TO_SEND_DATA, DEFAULT_TIME)
        val staticMarkerSize = sharedPreferences.getInt(KEY_STATIC_MARKER_SIZE, DEFAULT_SIZE)
        val usersMarkerSize = sharedPreferences.getInt(KEY_USERS_MARKER_SIZE, DEFAULT_SIZE)
        val isAutoLoad = sharedPreferences.getBoolean(KEY_IS_AUTO_LOAD, false)
        return UserDataModel(userName,timeToSend,usersMarkerSize,staticMarkerSize,selectedMarker, getDeviceId(context), isAutoLoad)
    }

    override fun reset() {
        sharedPreferences.edit()
            //TODO удплить .remove(KEY_NAME)
            .remove(KEY_NAME)
            .remove(KEY_MARKER)
            .remove(KEY_TIME_TO_SEND_DATA)
            .remove(KEY_STATIC_MARKER_SIZE)
            .remove(KEY_USERS_MARKER_SIZE)
            .apply()
    }

    override fun saveAutoLoad(boolean: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_IS_AUTO_LOAD, boolean).apply()
    }
}
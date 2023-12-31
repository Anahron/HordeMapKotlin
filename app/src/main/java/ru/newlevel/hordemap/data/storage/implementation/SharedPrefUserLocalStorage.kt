package ru.newlevel.hordemap.data.storage.implementation

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import ru.newlevel.hordemap.R
import ru.newlevel.hordemap.app.DEFAULT_SIZE
import ru.newlevel.hordemap.app.DEFAULT_TIME
import ru.newlevel.hordemap.app.KEY_AUTH_NAME
import ru.newlevel.hordemap.app.KEY_IS_AUTO_LOAD
import ru.newlevel.hordemap.app.KEY_MARKER
import ru.newlevel.hordemap.app.KEY_NAME
import ru.newlevel.hordemap.app.KEY_NEW_MESSAGES_COUNT
import ru.newlevel.hordemap.app.KEY_PROFILE_URL
import ru.newlevel.hordemap.app.KEY_STATIC_MARKER_SIZE
import ru.newlevel.hordemap.app.KEY_TIME_TO_SEND_DATA
import ru.newlevel.hordemap.app.KEY_USERS_MARKER_SIZE
import ru.newlevel.hordemap.app.KEY_USER_ID
import ru.newlevel.hordemap.app.SHARE_PREFS_NAME
import ru.newlevel.hordemap.app.TAG
import ru.newlevel.hordemap.app.getMyDeviceId
import ru.newlevel.hordemap.data.storage.interfaces.MessagesCountLocalStorage
import ru.newlevel.hordemap.data.storage.interfaces.UserLocalStorage
import ru.newlevel.hordemap.data.storage.models.UserDataModel

class SharedPrefUserLocalStorage(private val context: Context) : UserLocalStorage, MessagesCountLocalStorage {

    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(SHARE_PREFS_NAME, Context.MODE_PRIVATE)

    override fun save(userDataModel: UserDataModel) {
        sharedPreferences.edit().putBoolean(KEY_IS_AUTO_LOAD, userDataModel.autoLoad).apply()
        sharedPreferences.edit().putString(KEY_NAME, userDataModel.name).apply()
        sharedPreferences.edit().putInt(KEY_MARKER, userDataModel.selectedMarker).apply()
        sharedPreferences.edit().putInt(KEY_TIME_TO_SEND_DATA, userDataModel.timeToSendData).apply()
        sharedPreferences.edit().putInt(KEY_STATIC_MARKER_SIZE, userDataModel.staticMarkerSize).apply()
        sharedPreferences.edit().putInt(KEY_USERS_MARKER_SIZE, userDataModel.usersMarkerSize).apply()
        sharedPreferences.edit().putString(KEY_PROFILE_URL, userDataModel.profileImageUrl).apply()
        sharedPreferences.edit().putString(KEY_AUTH_NAME, userDataModel.authName).apply()
        sharedPreferences.edit().putString(KEY_USER_ID, userDataModel.deviceID).apply()
    }

    override fun getNewMessageCount(): Flow<Int> = callbackFlow {
        var value = sharedPreferences.getInt(KEY_NEW_MESSAGES_COUNT, 0)
        trySend(value)
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { sharedPreferences, key ->
            if (key == KEY_NEW_MESSAGES_COUNT) {
                value = sharedPreferences.getInt(KEY_NEW_MESSAGES_COUNT, 0)
                trySend(value)
            }
        }
        sharedPreferences.registerOnSharedPreferenceChangeListener(listener)
        awaitClose {
            Log.e(TAG, "awaitClose in getProfilesInMessenger")
            sharedPreferences.unregisterOnSharedPreferenceChangeListener(listener)
        }
    }

    override fun incrementNewMessageCount(increment: Int) {
        var newCount = 0
        if (increment >= 0) {
            val currentCount = sharedPreferences.getInt(KEY_NEW_MESSAGES_COUNT, 0)
            newCount = currentCount + increment
        }
        sharedPreferences.edit().putInt(KEY_NEW_MESSAGES_COUNT, newCount).apply()
    }

    override fun get(): UserDataModel {
        Log.e(TAG, "get() in SharedPrefUserLocalStorage")
        val userName = sharedPreferences.getString(KEY_NAME, "") ?: ""
        val selectedMarker = sharedPreferences.getInt(KEY_MARKER, 0)
        val timeToSend = sharedPreferences.getInt(KEY_TIME_TO_SEND_DATA, DEFAULT_TIME)
        val staticMarkerSize = sharedPreferences.getInt(KEY_STATIC_MARKER_SIZE, DEFAULT_SIZE)
        val usersMarkerSize = sharedPreferences.getInt(KEY_USERS_MARKER_SIZE, DEFAULT_SIZE)
        val isAutoLoad = sharedPreferences.getBoolean(KEY_IS_AUTO_LOAD, false)
        val authName = sharedPreferences.getString(KEY_AUTH_NAME, context.getString(R.string.hintAnonim)) ?: ""
        val userId = sharedPreferences.getString(KEY_USER_ID, context.getMyDeviceId()) ?: context.getMyDeviceId()
        val profileUrl = sharedPreferences.getString(KEY_PROFILE_URL, "") ?: ""
        return UserDataModel(
            name = userName,
            timeToSendData = timeToSend,
            usersMarkerSize = usersMarkerSize,
            staticMarkerSize = staticMarkerSize,
            selectedMarker = selectedMarker,
            deviceID = userId,
            autoLoad = isAutoLoad,
            authName = authName,
            profileImageUrl = profileUrl
        )
    }

    override fun reset() {
        sharedPreferences.edit()
            .remove(KEY_MARKER)
            .remove(KEY_TIME_TO_SEND_DATA)
            .remove(KEY_STATIC_MARKER_SIZE)
            .remove(KEY_USERS_MARKER_SIZE)
            .remove(KEY_IS_AUTO_LOAD)
            .apply()
    }

    override fun saveAutoLoad(boolean: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_IS_AUTO_LOAD, boolean).apply()
    }
}
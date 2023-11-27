package ru.newlevel.hordemap.data.storage.implementation

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import ru.newlevel.hordemap.app.GEO_STATIC_MARKERS_PATH
import ru.newlevel.hordemap.app.GEO_USER_MARKERS_PATH
import ru.newlevel.hordemap.app.MESSAGE_PATH
import ru.newlevel.hordemap.app.TAG
import ru.newlevel.hordemap.app.TIMESTAMP_PATH
import ru.newlevel.hordemap.app.TIME_TO_DELETE_USER_MARKER
import ru.newlevel.hordemap.app.USERS_PROFILES_PATH
import ru.newlevel.hordemap.data.db.UserEntityProvider
import ru.newlevel.hordemap.data.storage.interfaces.MarkersRemoteStorage
import ru.newlevel.hordemap.data.storage.interfaces.MessageRemoteStorage
import ru.newlevel.hordemap.data.storage.interfaces.ProfileRemoteStorage
import ru.newlevel.hordemap.data.storage.models.MarkerDataModel
import ru.newlevel.hordemap.data.storage.models.MessageDataModel
import ru.newlevel.hordemap.data.storage.models.UserDataModel

class MyFirebaseDatabase : MarkersRemoteStorage, MessageRemoteStorage, ProfileRemoteStorage {

    private val databaseReference = FirebaseDatabase.getInstance().reference
    private val staticDatabaseReference = databaseReference.child(GEO_STATIC_MARKERS_PATH)
    private val userDatabaseReference = databaseReference.child(GEO_USER_MARKERS_PATH)
    private val liveDataStaticMarkers = MutableLiveData<List<MarkerDataModel>>()
    private val liveDataUserMarkers = MutableLiveData<List<MarkerDataModel>>()
    private val liveDataMessageDataModel = MutableLiveData<List<MessageDataModel>>()
    private val liveDataUsersProfiles = MutableLiveData<List<UserDataModel>>()
    override fun deleteStaticMarker(key: String) {
        staticDatabaseReference.child(key).removeValue()
    }

    override fun sendUserMarker(markerModel: MarkerDataModel) {
        Log.e(TAG, " sendUserMarker$markerModel")
        userDatabaseReference.child(markerModel.deviceId).setValue(markerModel)
    }

    override fun getMessageUpdate(): MutableLiveData<List<MessageDataModel>> {
        Log.e(TAG, "startMessageUpdate() вызван")
        databaseReference.child(MESSAGE_PATH).orderByChild("timestamp")
            .addValueEventListener(messageEventListener)
        return liveDataMessageDataModel
    }

    override fun startUserMarkerUpdates(): MutableLiveData<List<MarkerDataModel>> {
        userDatabaseReference.addValueEventListener(valueUserEventListener)
        return liveDataUserMarkers
    }

    override fun startStaticMarkerUpdates(): MutableLiveData<List<MarkerDataModel>> {
        staticDatabaseReference.addValueEventListener(valueStaticEventListener)
        return liveDataStaticMarkers
    }

    override fun sendStaticMarker(markerModel: MarkerDataModel) {
        staticDatabaseReference.child(markerModel.timestamp.toString()).setValue(markerModel)
    }

    override fun sendMessage(text: String) {
        val time = System.currentTimeMillis()
        val geoDataPath = "$MESSAGE_PATH/$time"
        val updates: MutableMap<String, Any> = HashMap()
        updates["$geoDataPath/userName"] = UserEntityProvider.userEntity.name
        updates["$geoDataPath/message"] = text
        updates["$geoDataPath/deviceID"] = UserEntityProvider.userEntity.deviceID
        updates["$geoDataPath/timestamp"] = time
        updates["$geoDataPath/selectedMarker"] = UserEntityProvider.userEntity.selectedMarker
        updates["$geoDataPath/profileImageUrl"] = UserEntityProvider.userEntity.profileImageUrl
        databaseReference.updateChildren(updates)
    }

    override fun sendMessage(text: String, downloadUrl: String, fileSize: Long, fileName: String) {
        val time = System.currentTimeMillis()
        val geoDataPath = "$MESSAGE_PATH/$time"
        val updates: MutableMap<String, Any> = HashMap()
        updates["$geoDataPath/userName"] = UserEntityProvider.userEntity.name
        updates["$geoDataPath/message"] = text
        updates["$geoDataPath/url"] = downloadUrl
        updates["$geoDataPath/deviceID"] = UserEntityProvider.userEntity.deviceID
        updates["$geoDataPath/selectedMarker"] = UserEntityProvider.userEntity.selectedMarker
        updates["$geoDataPath/timestamp"] = time
        updates["$geoDataPath/fileSize"] = fileSize
        updates["$geoDataPath/fileName"] = fileName
        updates["$geoDataPath/profileImageUrl"] = UserEntityProvider.userEntity.profileImageUrl
        databaseReference.updateChildren(updates)
    }

    override fun stopMarkerUpdates() {
        Log.e(TAG, "stopMarkerUpdates in StorageImpl вызван")
        userDatabaseReference.removeEventListener(valueUserEventListener)
        staticDatabaseReference.removeEventListener(valueStaticEventListener)
    }

    override fun stopMessageUpdate() {
        Log.e(TAG, "stopMessageUpdate вызван")
        databaseReference.child(MESSAGE_PATH).orderByChild("timestamp")
            .removeEventListener(messageEventListener)
        databaseReference.child(USERS_PROFILES_PATH).orderByChild("deviceID")
            .removeEventListener(valueUsersProfilesEventListener)
    }

    override fun getProfilesInMessenger(): MutableLiveData<List<UserDataModel>> {
        databaseReference.child(USERS_PROFILES_PATH).orderByChild("deviceID")
            .addValueEventListener(valueUsersProfilesEventListener)
        return liveDataUsersProfiles
    }

    override suspend fun sendUserData(userData: UserDataModel) {
        val geoDataPath = "$USERS_PROFILES_PATH/${userData.deviceID}"
        val updates: MutableMap<String, Any> = HashMap()
        updates["$geoDataPath/name"] = userData.name
        updates["$geoDataPath/deviceID"] = userData.deviceID
        updates["$geoDataPath/profileImageUrl"] = userData.profileImageUrl
        updates["$geoDataPath/authName"] = userData.authName
        updates["$geoDataPath/selectedMarker"] = userData.selectedMarker
        updates["$geoDataPath/timeToSendData"] = userData.timeToSendData
        return withContext(Dispatchers.IO) {
            val updateTask = databaseReference.updateChildren(updates)
            updateTask.await()
            if (updateTask.isSuccessful)
                updateAllUserMessages(userData)
        }
    }

    private fun updateAllUserMessages(userData: UserDataModel) {
        val deviceId = userData.deviceID
        databaseReference.child(MESSAGE_PATH).orderByChild("deviceID").equalTo(deviceId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    for (messageSnapshot in dataSnapshot.children) {
                        messageSnapshot.ref.child("userName").setValue(userData.name)
                        messageSnapshot.ref.child("selectedMarker").setValue(userData.selectedMarker)
                        messageSnapshot.ref.child("profileImageUrl").setValue(userData.profileImageUrl)
                    }
                }
                override fun onCancelled(databaseError: DatabaseError) {
                    Log.e(TAG, "MyFirebaseDatabase updateAllUserMessages onCancelled = " + databaseError.message)
                }
            })
    }


    private var valueUsersProfilesEventListener = object : ValueEventListener {
        override fun onDataChange(dataSnapshot: DataSnapshot) {
            val savedUsers: ArrayList<UserDataModel> = ArrayList()
            for (snapshot in dataSnapshot.children) {
                try {
                    val name = snapshot.child("name").getValue(String::class.java) ?: ""
                    val authName = snapshot.child("authName").getValue(String::class.java) ?: ""
                    val selectedMarker = snapshot.child("selectedMarker").getValue(Int::class.java) ?: 0
                    val deviceID = snapshot.child("deviceID").getValue(String::class.java) ?: ""
                    val profileImageUrl = snapshot.child("profileImageUrl").getValue(String::class.java) ?: ""
                    val user = UserDataModel(
                        deviceID = deviceID,
                        name = name,
                        profileImageUrl = profileImageUrl,
                        selectedMarker = selectedMarker,
                        authName = authName,
                    )
                    savedUsers.add(user)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            liveDataUsersProfiles.postValue(savedUsers)
        }

        override fun onCancelled(error: DatabaseError) {
        }
    }


    private var valueUserEventListener = object : ValueEventListener {
        override fun onDataChange(dataSnapshot: DataSnapshot) {
            val savedUserMarkers: ArrayList<MarkerDataModel> = ArrayList()
            val timeNow = System.currentTimeMillis()
            for (snapshot in dataSnapshot.children) {
                try {
                    var alpha: Float
                    val timestamp: Long? =
                        snapshot.child(TIMESTAMP_PATH).getValue(Long::class.java)
                    val timeDiffMillis = timeNow - timestamp!!
                    val timeDiffMinutes = timeDiffMillis / 60000
                    // Удаляем маркера в базе, которым больше TIME_TO_DELETE_USER_MARKER минут
                    alpha = if (timeDiffMinutes >= TIME_TO_DELETE_USER_MARKER) {
                        snapshot.ref.removeValue()
                        continue
                    } else {
                        // Устанавливаем прозрачность маркера от 0 до 5 минут максимум до 50%
                        1f - (timeDiffMinutes / 10f).coerceAtMost(0.5f)
                    }
                    val myMarker: MarkerDataModel =
                        snapshot.getValue(MarkerDataModel::class.java)!!
                    myMarker.deviceId = snapshot.key.toString()
                    myMarker.alpha = alpha
                    savedUserMarkers.add(myMarker)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            liveDataUserMarkers.postValue(savedUserMarkers)
        }

        override fun onCancelled(error: DatabaseError) {

        }
    }

    private var valueStaticEventListener = object : ValueEventListener {
        override fun onDataChange(dataSnapshot: DataSnapshot) {
            val savedStaticMarkers: ArrayList<MarkerDataModel> = ArrayList()
            for (snapshot in dataSnapshot.children) {
                try {
                    val myMarker: MarkerDataModel =
                        snapshot.getValue(MarkerDataModel::class.java)!!
                    savedStaticMarkers.add(myMarker)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            liveDataStaticMarkers.postValue(savedStaticMarkers)
        }

        override fun onCancelled(error: DatabaseError) {
        }
    }

    private val messageEventListener = object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            Log.e(TAG, "startMessageUpdate пришло обновление сообщений")
            val messages = ArrayList<MessageDataModel>()
            for (snap in snapshot.children) {
                val message: MessageDataModel? = snap.getValue(MessageDataModel::class.java)
                if (message != null) {
                    messages.add(message)
                }
            }
            if (messages.isNotEmpty()) {
                liveDataMessageDataModel.postValue(messages)
            }
        }

        override fun onCancelled(error: DatabaseError) {}
    }
}




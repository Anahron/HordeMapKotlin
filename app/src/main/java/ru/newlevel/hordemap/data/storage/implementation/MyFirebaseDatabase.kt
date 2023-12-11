package ru.newlevel.hordemap.data.storage.implementation

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import ru.newlevel.hordemap.app.GEO_STATIC_MARKERS_PATH
import ru.newlevel.hordemap.app.GEO_USER_MARKERS_PATH
import ru.newlevel.hordemap.app.MESSAGE_PATH
import ru.newlevel.hordemap.app.TAG
import ru.newlevel.hordemap.app.TIMESTAMP_PATH
import ru.newlevel.hordemap.app.TIME_TO_DELETE_USER_MARKER
import ru.newlevel.hordemap.app.USERS_PROFILES_PATH
import ru.newlevel.hordemap.data.db.MyMessageEntity
import ru.newlevel.hordemap.data.storage.interfaces.MarkersRemoteStorage
import ru.newlevel.hordemap.data.storage.interfaces.MessageRemoteStorage
import ru.newlevel.hordemap.data.storage.interfaces.ProfileRemoteStorage
import ru.newlevel.hordemap.data.storage.models.MarkerDataModel
import ru.newlevel.hordemap.data.storage.models.UserDataModel

class MyFirebaseDatabase : MarkersRemoteStorage, MessageRemoteStorage, ProfileRemoteStorage {

    private val databaseReference = FirebaseDatabase.getInstance().reference
    private val staticDatabaseReference = databaseReference.child(GEO_STATIC_MARKERS_PATH)
    private val userDatabaseReference = databaseReference.child(GEO_USER_MARKERS_PATH)
    private val liveDataMessageDataModel = MutableLiveData<List<MyMessageEntity>>()
    override fun deleteStaticMarker(key: String) {
        staticDatabaseReference.child(key).removeValue()
    }

    override fun sendStaticMarker(markerModel: MarkerDataModel) {
        staticDatabaseReference.child(markerModel.timestamp.toString()).setValue(markerModel)
    }

    override fun sendMessage(message: MyMessageEntity) {
        databaseReference.child("$MESSAGE_PATH/${message.timestamp}").setValue(message)
    }

    override fun stopMessageUpdate() {
        Log.e(TAG, "stopMessageUpdate вызван")
        databaseReference.child(MESSAGE_PATH).orderByChild("timestamp")
            .removeEventListener(messageEventListener)
    }

    override fun sendUserMarker(markerModel: MarkerDataModel) {
        Log.e(TAG, " sendUserMarker$markerModel")
        userDatabaseReference.child(markerModel.deviceId).setValue(markerModel)
    }

    override fun getMessageUpdate(): Flow<List<MyMessageEntity>> = callbackFlow{
       val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val messages = ArrayList<MyMessageEntity>()
                for (snap in snapshot.children) {
                    val message: MyMessageEntity? = snap.getValue(MyMessageEntity::class.java)
                    if (message != null) {
                        messages.add(message)
                    }
                }
                if (messages.isNotEmpty()) {
                   trySend(messages)
                }
            }
            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        databaseReference.child(MESSAGE_PATH).orderByChild("timestamp")
            .addValueEventListener(listener)
        awaitClose{
            Log.e(TAG, "awaitClose in getMessageUpdate")
            databaseReference.child(MESSAGE_PATH).orderByChild("timestamp")
                .removeEventListener(listener)
        }
    }

    override fun getUserMarkerUpdates(): Flow<List<MarkerDataModel>> {
        return callbackFlow {
            val listener = userDatabaseReference.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val savedUserMarkers: ArrayList<MarkerDataModel> = ArrayList()
                    val timeNow = System.currentTimeMillis()
                    for (snapshot in dataSnapshot.children) {
                        try {
                            var alpha: Float
                            val timestamp: Long? = snapshot.child(TIMESTAMP_PATH).getValue(Long::class.java)
                            val timeDiffMillis = timeNow - (timestamp ?: 0L)
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
                    trySend(savedUserMarkers).isSuccess
                }

                override fun onCancelled(error: DatabaseError) {
                    close(error.toException())
                }

            })
            awaitClose {
                Log.e(TAG, " awaitClose in userDatabaseReference")
                userDatabaseReference.removeEventListener(listener)
            }
        }
    }

    override fun getStaticMarkerUpdates(): Flow<List<MarkerDataModel>> = callbackFlow {
        val listener = object : ValueEventListener {
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
                trySend(savedStaticMarkers).isSuccess
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        staticDatabaseReference.addValueEventListener(listener)
        awaitClose {
            Log.e(TAG, "awaitClose in staticDatabaseReference")
            staticDatabaseReference.removeEventListener(listener)
        }
    }


    override fun getProfilesInMessenger(): Flow<List<UserDataModel>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val savedUsers: ArrayList<UserDataModel> = ArrayList()
                for (snapshot in dataSnapshot.children) {
                    try {
                        val name = snapshot.child("name").getValue(String::class.java) ?: ""
                        val authName = snapshot.child("authName").getValue(String::class.java) ?: ""
                        val selectedMarker = snapshot.child("selectedMarker").getValue(Int::class.java) ?: 0
                        val deviceID = snapshot.child("deviceID").getValue(String::class.java) ?: ""
                        val profileImageUrl =
                            snapshot.child("profileImageUrl").getValue(String::class.java) ?: ""
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
                trySend(savedUsers).isSuccess
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        databaseReference.child(USERS_PROFILES_PATH).orderByChild("deviceID")
            .addValueEventListener(listener)
        awaitClose{
            Log.e(TAG, "awaitClose in getProfilesInMessenger")
            databaseReference.child(USERS_PROFILES_PATH).orderByChild("deviceID")
                .removeEventListener(listener)
        }
    }

    override fun deleteMessage(message: MyMessageEntity) {
        databaseReference.child(MESSAGE_PATH).child(message.timestamp.toString()).removeValue()
    }

    override suspend fun sendUserData(userData: UserDataModel) {
        return withContext(Dispatchers.IO) {
            val updateTask = databaseReference.child("$USERS_PROFILES_PATH/${userData.deviceID}").setValue(userData)
            updateTask.await()
            if (updateTask.isSuccessful)
                updateAllUserMessages(userData)
        }
    }

    override suspend fun deleteUserDataRemote(deviceId: String) {
        databaseReference.child(USERS_PROFILES_PATH).child(deviceId)
            .removeValue()
            .addOnSuccessListener {
                Log.e(TAG, "Profile deleted successfully")
            }
            .addOnFailureListener {
                Log.e(TAG, "Failed to delete profile: ${it.message}")
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

    private val messageEventListener = object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            val messages = ArrayList<MyMessageEntity>()
            for (snap in snapshot.children) {
                val message: MyMessageEntity? = snap.getValue(MyMessageEntity::class.java)
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




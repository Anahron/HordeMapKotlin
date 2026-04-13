package ru.newlevel.hordemap.data.storage.implementation

import android.util.Log
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
import ru.newlevel.hordemap.data.db.MarkerEntity
import ru.newlevel.hordemap.data.db.MyMessageEntity
import ru.newlevel.hordemap.data.db.UserEntityProvider
import ru.newlevel.hordemap.data.storage.interfaces.MarkersRemoteStorage
import ru.newlevel.hordemap.data.storage.interfaces.MessageRemoteStorage
import ru.newlevel.hordemap.data.storage.interfaces.ProfileRemoteStorage
import ru.newlevel.hordemap.data.storage.models.UserDataModel
import ru.newlevel.hordemap.presentation.settings.GroupInfoModel
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class MyFirebaseDatabase : MarkersRemoteStorage, MessageRemoteStorage, ProfileRemoteStorage {

    private val databaseReference = FirebaseDatabase.getInstance().reference

    override suspend fun deleteStaticMarker(key: String) {
        databaseReference.child("$GEO_STATIC_MARKERS_PATH${UserEntityProvider.userEntity.userGroup}")
            .child(key).removeValue()
    }

    override fun sendStaticMarker(markerModel: MarkerEntity) {
        Log.e(TAG, " sendStaticMarker${UserEntityProvider.userEntity.userGroup}")
        databaseReference.child("$GEO_STATIC_MARKERS_PATH${UserEntityProvider.userEntity.userGroup}")
            .child(markerModel.timestamp.toString()).setValue(markerModel)
    }

    override fun sendMessage(message: MyMessageEntity) {
        databaseReference.child("$MESSAGE_PATH${UserEntityProvider.userEntity.userGroup}/${message.timestamp}")
            .setValue(message)
    }

    override fun sendUserMarker(markerModel: MarkerEntity) {
        databaseReference.child("$GEO_USER_MARKERS_PATH${UserEntityProvider.userEntity.userGroup}")
            .child(markerModel.deviceId).setValue(markerModel).addOnSuccessListener {
                Log.e(TAG, "UserMarker sended success")
            }
        updateLastSeenTimestamp(markerModel)
    }
    private fun updateLastSeenTimestamp(markerModel: MarkerEntity){
        databaseReference.child("$USERS_PROFILES_PATH/${UserEntityProvider.userEntity.userGroup}")
            .child(markerModel.deviceId)
            .updateChildren(mapOf("lastSeen" to markerModel.timestamp))
    }

    override fun getMessageUpdate(): Flow<List<MyMessageEntity>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val messages = ArrayList<MyMessageEntity>()
                for (snap in snapshot.children) {
                    val message: MyMessageEntity? = snap.getValue(MyMessageEntity::class.java)
                    if (message != null) {
                        messages.add(message)
                    }
                }
                trySend(messages)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        databaseReference.child("$MESSAGE_PATH${UserEntityProvider.userEntity.userGroup}")
            .orderByChild("timestamp").addValueEventListener(listener)
        awaitClose {
            Log.e(TAG, "awaitClose in getMessageUpdate")
            databaseReference.child("$MESSAGE_PATH${UserEntityProvider.userEntity.userGroup}")
                .orderByChild("timestamp").removeEventListener(listener)
        }
    }

    override fun getUserMarkerUpdates(): Flow<List<MarkerEntity>> = callbackFlow {
        val listener =
            databaseReference.child("$GEO_USER_MARKERS_PATH${UserEntityProvider.userEntity.userGroup}")
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        val savedUserMarkers: ArrayList<MarkerEntity> = ArrayList()
                        val timeNow = System.currentTimeMillis()
                        for (snapshot in dataSnapshot.children) {
                            try {
                                var alpha: Float
                                val timestamp: Long? =
                                    snapshot.child(TIMESTAMP_PATH).getValue(Long::class.java)
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
                                val myMarker: MarkerEntity =
                                    snapshot.getValue(MarkerEntity::class.java)!!
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
            databaseReference.child("$GEO_USER_MARKERS_PATH${UserEntityProvider.userEntity.userGroup}")
                .removeEventListener(listener)
        }
    }


    override fun getStaticMarkerUpdates(): Flow<List<MarkerEntity>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val savedStaticMarkers: ArrayList<MarkerEntity> = ArrayList()
                for (snapshot in dataSnapshot.children) {
                    try {
                        val myMarker: MarkerEntity =
                            snapshot.getValue(MarkerEntity::class.java)!!
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
        databaseReference.child("$GEO_STATIC_MARKERS_PATH${UserEntityProvider.userEntity.userGroup}")
            .addValueEventListener(listener)
        awaitClose {
            Log.e(TAG, "awaitClose in staticDatabaseReference")
            databaseReference.child("$GEO_STATIC_MARKERS_PATH${UserEntityProvider.userEntity.userGroup}")
                .removeEventListener(listener)
        }
    }


    override fun getProfilesInMessenger(): Flow<List<UserDataModel>> = callbackFlow {
        databaseReference.keepSynced(true)
        val listener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val savedUsers: ArrayList<UserDataModel> = ArrayList()
                for (snapshot in dataSnapshot.children) {
                    if (snapshot.key == "PASS")
                        continue
                    try {
                        val name = snapshot.child("name").getValue(String::class.java) ?: ""
                        val authName = snapshot.child("authName").getValue(String::class.java) ?: ""
                        val selectedMarker =
                            snapshot.child("selectedMarker").getValue(Int::class.java) ?: 0
                        val deviceID = snapshot.child("deviceID").getValue(String::class.java) ?: ""
                        val profileImageUrl =
                            snapshot.child("profileImageUrl").getValue(String::class.java) ?: ""
                        val lastSeen =
                            snapshot.child("lastSeen").getValue(Long::class.java) ?: 0L
                        val user = UserDataModel(
                            deviceID = deviceID,
                            name = name,
                            profileImageUrl = profileImageUrl,
                            selectedMarker = selectedMarker,
                            authName = authName,
                            lastSeen = lastSeen
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
        databaseReference.child("$USERS_PROFILES_PATH/${UserEntityProvider.userEntity.userGroup}")
            .orderByChild("deviceID").addValueEventListener(listener)
        awaitClose {
            Log.e(TAG, "awaitClose in getProfilesInMessenger")
            databaseReference.child("$USERS_PROFILES_PATH/${UserEntityProvider.userEntity.userGroup}")
                .orderByChild("deviceID").removeEventListener(listener)
        }
    }

    override suspend fun getProfilesAndChildCounts(): List<GroupInfoModel> =
        suspendCoroutine { continuation ->
            val listener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val nodes = snapshot.children.mapNotNull { childSnapshot ->
                        val nodeName = childSnapshot.key
                        var childCount = childSnapshot.childrenCount.toInt()
                        val password = childSnapshot.child("PASS").getValue(String::class.java)
                        Log.e(TAG, "group = ${nodeName} Password = " + password)
                        val safePassword = password?: ""
                        if (password != null){
                            childCount--
                        }
                        if (nodeName != null) {
                            val group = GroupInfoModel(nodeName, childCount, safePassword)
                            Log.e(TAG, "GroupInfoModel = ${group} ")
                            group
                        } else {
                            null
                        }
                    }
                    continuation.resume(nodes)
                }

                override fun onCancelled(error: DatabaseError) {
                    continuation.resumeWithException(error.toException())
                }
            }
            databaseReference.child(USERS_PROFILES_PATH).addListenerForSingleValueEvent(listener)
        }

    override fun deleteMessage(message: MyMessageEntity) {
        databaseReference.child("$MESSAGE_PATH${UserEntityProvider.userEntity.userGroup}")
            .child(message.timestamp.toString()).removeValue()
    }

    override suspend fun sendUserData(userData: UserDataModel) {
        return withContext(Dispatchers.IO) {
            val updateTask =
                databaseReference.child("$USERS_PROFILES_PATH/${UserEntityProvider.userEntity.userGroup}/${userData.deviceID}")
                    .setValue(userData)
            updateTask.await()
            if (updateTask.isSuccessful) updateAllUserMessages(userData)
        }
    }
    override suspend fun setPasswordForGroup(userGroup: Int, password: String) {
        databaseReference.child("$USERS_PROFILES_PATH/$userGroup").child("PASS").setValue(password)
            .addOnSuccessListener {
                Log.e(TAG, "Password set successfully")
            }
            .addOnFailureListener {
                Log.e(TAG, "Failed to set password: ${it.message}")
            }
    }

    override suspend fun deleteUserDataRemote(deviceId: String, userGroup: Int) {
        databaseReference.child("$USERS_PROFILES_PATH/${userGroup}").child(deviceId).removeValue()
            .addOnSuccessListener {
                Log.e(TAG, "Profile deleted successfully")
                checkAndDeleteGroupIfNeeded(userGroup)
            }.addOnFailureListener {
                Log.e(TAG, "Failed to delete profile: ${it.message}")
            }
    }
    private fun checkAndDeleteGroupIfNeeded(userGroup: Int) {
        databaseReference.child("$USERS_PROFILES_PATH/$userGroup").get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.childrenCount.toInt() == 1 && snapshot.child("PASS").exists()) {
                    databaseReference.child("$USERS_PROFILES_PATH/$userGroup").removeValue()
                        .addOnSuccessListener {
                            Log.e(TAG, "Group deleted successfully")
                        }
                        .addOnFailureListener {
                            Log.e(TAG, "Failed to delete group: ${it.message}")
                        }
                }
            }
            .addOnFailureListener {
                Log.e(TAG, "Failed to check group data: ${it.message}")
            }
    }
    override suspend fun getPasswordForGroup(userGroup: Int): String = suspendCoroutine { continuation ->
        databaseReference.child("$USERS_PROFILES_PATH/$userGroup").child("PASS").get()
            .addOnSuccessListener { snapshot ->
                val password = snapshot.getValue(String::class.java).orEmpty() // Получаем пароль
                continuation.resume(password) // Возвращаем результат через continuation
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Failed to retrieve password: ${exception.message}")
                continuation.resume("") // Возвращаем null при ошибке
            }
    }


    override suspend fun getProfilesInGroup(groupNumber: Int): List<UserDataModel> =
        suspendCoroutine { continuation ->
            val listener = object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val savedUsers: ArrayList<UserDataModel> = ArrayList()
                    for (snapshot in dataSnapshot.children) {
                        try {
                            if (snapshot.key == "PASS")
                                continue
                            val name = snapshot.child("name").getValue(String::class.java) ?: ""
                            val authName =
                                snapshot.child("authName").getValue(String::class.java) ?: ""
                            val selectedMarker =
                                snapshot.child("selectedMarker").getValue(Int::class.java) ?: 0
                            val deviceID =
                                snapshot.child("deviceID").getValue(String::class.java) ?: ""
                            val profileImageUrl =
                                snapshot.child("profileImageUrl").getValue(String::class.java) ?: ""
                            val lastSeen =
                                snapshot.child("lastSeen").getValue(Long::class.java) ?: 0L
                            val user = UserDataModel(
                                deviceID = deviceID,
                                name = name,
                                profileImageUrl = profileImageUrl,
                                selectedMarker = selectedMarker,
                                authName = authName,
                                lastSeen = lastSeen
                            )
                            savedUsers.add(user)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                    continuation.resume(savedUsers)
                }

                override fun onCancelled(error: DatabaseError) {
                    continuation.resumeWithException(error.toException())
                }
            }

            databaseReference.child("$USERS_PROFILES_PATH/$groupNumber").orderByChild("deviceID")
                .addListenerForSingleValueEvent(listener)
        }

    override suspend fun deleteUserDataRemote(deviceId: String) {
        databaseReference.child("$USERS_PROFILES_PATH/${UserEntityProvider.userEntity.userGroup}")
            .child(deviceId).removeValue().addOnSuccessListener {
                Log.e(TAG, "Profile deleted successfully")
            }.addOnFailureListener {
                Log.e(TAG, "Failed to delete profile: ${it.message}")
            }
    }

    private fun updateAllUserMessages(userData: UserDataModel) {
        val deviceId = userData.deviceID
        databaseReference.child("$MESSAGE_PATH${UserEntityProvider.userEntity.userGroup}")
            .orderByChild("deviceID").equalTo(deviceId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    for (messageSnapshot in dataSnapshot.children) {
                        messageSnapshot.ref.child("userName").setValue(userData.name)
                        messageSnapshot.ref.child("selectedMarker")
                            .setValue(userData.selectedMarker)
                        messageSnapshot.ref.child("profileImageUrl")
                            .setValue(userData.profileImageUrl)
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Log.e(TAG, "MyFirebaseDatabase updateAllUserMessages onCancelled = " + databaseError.message)
                }
            })
    }
}




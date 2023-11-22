package ru.newlevel.hordemap.data.storage.implementation

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import ru.newlevel.hordemap.data.db.UserEntityProvider
import ru.newlevel.hordemap.data.storage.interfaces.MarkersRemoteStorage
import ru.newlevel.hordemap.data.storage.interfaces.MessageRemoteStorage
import ru.newlevel.hordemap.data.storage.models.MarkerDataModel
import ru.newlevel.hordemap.data.storage.models.MessageDataModel

private const val GEO_USER_MARKERS_PATH = "geoData0"
private const val GEO_STATIC_MARKERS_PATH = "geoMarkers0"
private const val TIME_TO_DELETE_USER_MARKER = 30 // в минутах
private const val MESSAGE_PATH = "messages0"
private const val TIMESTAMP_PATH = "timestamp"
private const val TAG = "AAA"

class MyFirebaseDatabase : MarkersRemoteStorage, MessageRemoteStorage {

    private val databaseReference = FirebaseDatabase.getInstance().reference
    private val staticDatabaseReference = databaseReference.child(GEO_STATIC_MARKERS_PATH)
    private val userDatabaseReference = databaseReference.child(GEO_USER_MARKERS_PATH)
    private val liveDataStaticMarkers = MutableLiveData<List<MarkerDataModel>>()
    private val liveDataUserMarkers = MutableLiveData<List<MarkerDataModel>>()
    private val liveDataMessageDataModel = MutableLiveData<List<MessageDataModel>>()

    override fun deleteStaticMarker(key: String) {
        staticDatabaseReference.child(key).removeValue()
    }

    override fun sendUserMarker(markerModel: MarkerDataModel) {
        Log.e(TAG, " sendUserMarker" + markerModel.toString())
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
        updates["$geoDataPath/userName"] = UserEntityProvider.userEntity?.name.toString()
        updates["$geoDataPath/message"] = text
        updates["$geoDataPath/deviceID"] = UserEntityProvider.userEntity?.deviceID.toString()
        updates["$geoDataPath/timestamp"] = time
        databaseReference.updateChildren(updates)
    }

    override fun sendMessage(text: String, downloadUrl: String, fileSize: Long, fileName: String) {
        val time = System.currentTimeMillis()
        val geoDataPath = "$MESSAGE_PATH/$time"
        val updates: MutableMap<String, Any> = HashMap()
        updates["$geoDataPath/userName"] = UserEntityProvider.userEntity?.name.toString()
        updates["$geoDataPath/message"] = text
        updates["$geoDataPath/url"] = downloadUrl
        updates["$geoDataPath/deviceID"] = UserEntityProvider.userEntity?.deviceID.toString()
        updates["$geoDataPath/timestamp"] = time
        updates["$geoDataPath/fileSize"] = fileSize
        updates["$geoDataPath/fileName"] = fileName
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




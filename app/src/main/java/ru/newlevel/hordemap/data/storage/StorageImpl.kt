package ru.newlevel.hordemap.data.storage

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.maps.model.Marker
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.suspendCancellableCoroutine
import ru.newlevel.hordemap.data.db.UserEntityProvider
import ru.newlevel.hordemap.data.storage.models.MarkerDataModel
import ru.newlevel.hordemap.data.storage.models.MessageDataModel
import java.io.File
import kotlin.coroutines.resume

private const val GEO_USER_MARKERS_PATH = "geoData0"
private const val GEO_STATIC_MARKERS_PATH = "geoMarkers0"
private const val TIME_TO_DELETE_USER_MARKER = 30 // в минутах
private const val MESSAGE_PATH = "messages0"
private const val TIMESTAMP_PATH = "timestamp"
private const val TAG = "AAA"

class StorageImpl : MarkersDataStorage, MapStorage, MessageStorage {

    private val databaseReference by lazy(LazyThreadSafetyMode.NONE) { FirebaseDatabase.getInstance().reference }
    var storage: FirebaseStorage = FirebaseStorage.getInstance()

    val gsReference = storage.getReferenceFromUrl("gs://horde-4112c.appspot.com/maps/map.kmz")

    private val staticDatabaseReference = databaseReference.child(GEO_STATIC_MARKERS_PATH)
    private val userDatabaseReference = databaseReference.child(GEO_USER_MARKERS_PATH)

    private var valueUserEventListener: ValueEventListener? = null
    private var valueStaticEventListener: ValueEventListener? = null
    private var valueMessageEventListener: ValueEventListener? = null

    private val liveDataStaticMarkers = MutableLiveData<List<MarkerDataModel>>()
    private val liveDataUserMarkers = MutableLiveData<List<MarkerDataModel>>()
    private val liveDataMessageDataModel = MutableLiveData<List<MessageDataModel>>()

    override fun deleteStaticMarker(marker: Marker) {
        staticDatabaseReference.child(marker.tag.toString()).removeValue()
    }

    override fun sendCoordinates(markerModel: MarkerDataModel) {
        Log.e(TAG, "Координаты отправлены")
        userDatabaseReference.child(markerModel.deviceId).setValue(markerModel)
    }

    override fun startUserMarkerUpdates(): MutableLiveData<List<MarkerDataModel>> {
        //  userDatabaseReference.removeEventListener(valueUserEventListener)
        valueUserEventListener =
            userDatabaseReference.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    Log.e(TAG, "данные в startUserMarkerUpdates обновлены")
                    val savedUserMarkers: ArrayList<MarkerDataModel> = ArrayList()
                    val timeNow = System.currentTimeMillis()
                    for (snapshot in dataSnapshot.children) {
                        try {
                            var alpha: Float
                            val timestamp: Long? =
                                snapshot.child(TIMESTAMP_PATH).getValue(Long::class.java)
                            val timeDiffMillis = timeNow - timestamp!!
                            val timeDiffMinutes = timeDiffMillis / 60000
                            // Удаляем маркера, которым больше TIME_TO_DELETE_USER_MARKER минут
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

                override fun onCancelled(error: DatabaseError) {}
            })
        return liveDataUserMarkers
    }

    override fun startStaticMarkerUpdates(): MutableLiveData<List<MarkerDataModel>> {
        if (valueStaticEventListener != null)
            staticDatabaseReference.removeEventListener(valueStaticEventListener!!)
        valueStaticEventListener =
            staticDatabaseReference.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    Log.e(TAG, "данные в startStaticMarkerUpdates обновлены")
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
            })
        return liveDataStaticMarkers
    }

    override fun stopMarkerUpdates() {
        Log.e(TAG, "stopMarkerUpdates вызван")
        if (valueUserEventListener != null)
            userDatabaseReference.removeEventListener(valueUserEventListener!!)
        if (valueStaticEventListener != null)
            staticDatabaseReference.removeEventListener(valueStaticEventListener!!)
    }

    override fun createStaticMarker(markerModel: MarkerDataModel) {
        staticDatabaseReference.child(markerModel.timestamp.toString()).setValue(markerModel)
    }

    override suspend fun loadGameMapFromServer(context: Context): Uri? {
        return suspendCancellableCoroutine { continuation ->
            val filename = "lastSavedMap.kmz"
            val file = File(context.filesDir, filename)
            gsReference.getFile(file)
                .addOnSuccessListener { _ ->
                    continuation.resume(Uri.fromFile(file))
                }
                .addOnFailureListener {
                    continuation.resume(null)
                }
            continuation.invokeOnCancellation {
                continuation.resume(null)
            }
        }
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

    override fun startMessageUpdate(): MutableLiveData<List<MessageDataModel>> {
        //    databaseReference.removeEventListener(valueMessageEventListener)
        valueMessageEventListener = databaseReference.child(MESSAGE_PATH).orderByChild("timestamp")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val messages = ArrayList<MessageDataModel>()
                    for (snapshot in dataSnapshot.children) {
                        Log.e("AAA", snapshot.toString())
                        val message: MessageDataModel? =
                            snapshot.getValue(MessageDataModel::class.java)
                        if (message != null) {
                            messages.add(message)
                        }
                    }
                    if (messages.isNotEmpty()) {
                        liveDataMessageDataModel.postValue(messages)
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
        return liveDataMessageDataModel
    }

    override fun stopMessageUpdate() {
        if (valueMessageEventListener != null)
            databaseReference.removeEventListener(valueMessageEventListener!!)
    }

}



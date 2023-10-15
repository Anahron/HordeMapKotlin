package ru.newlevel.hordemap.data.storage

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.maps.model.Marker
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.suspendCancellableCoroutine
import ru.newlevel.hordemap.data.storage.models.MarkerDataModel
import java.io.File
import kotlin.coroutines.resume

private const val GEO_USER_MARKERS_PATH = "geoData0"
private const val GEO_STATIC_MARKERS_PATH = "geoMarkers0"
private const val TIME_TO_DELETE_USER_MARKER = 30 // в минутах
private const val TIMESTAMP_PATH = "timestamp"
private const val TAG = "AAA"

class FirebaseStorageImpl: MarkersDataStorage, FirebaseMapStorage {

    private val databaseReference by lazy(LazyThreadSafetyMode.NONE) { FirebaseDatabase.getInstance().reference }
    var storage: FirebaseStorage = FirebaseStorage.getInstance()

    val gsReference = storage.getReferenceFromUrl("gs://horde-4112c.appspot.com/maps/map.kmz")

    private val staticDatabaseReference = databaseReference.child(GEO_STATIC_MARKERS_PATH)
    private val userDatabaseReference = databaseReference.child(GEO_USER_MARKERS_PATH)

    private val savedUserMarkers: ArrayList<MarkerDataModel> = ArrayList()
    private val savedStaticMarkers: ArrayList<MarkerDataModel> = ArrayList()

    private var valueUserEventListener: ValueEventListener? = null
    private var valueStaticEventListener: ValueEventListener? = null

    override fun deleteStaticMarker(marker: Marker) {
        staticDatabaseReference.child(marker.tag.toString()).removeValue()
    }

    override fun sendCoordinates(markerModel: MarkerDataModel) {
        Log.e(TAG, "Координаты отправлены")
        userDatabaseReference.child(markerModel.deviceId).setValue(markerModel)
    }

    override fun startUserMarkerUpdates(): LiveData<List<MarkerDataModel>> {
        val liveDataUserMarkers = MutableLiveData<List<MarkerDataModel>>()

        if (valueUserEventListener != null) {
            userDatabaseReference.removeEventListener(valueUserEventListener!!)
        }
        valueUserEventListener =
            userDatabaseReference.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    Log.e(TAG, "данные в startUserMarkerUpdates обновлены")
                    savedUserMarkers.clear()
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

    override fun startStaticMarkerUpdates(): LiveData<List<MarkerDataModel>> {
        val liveDataStaticMarkers = MutableLiveData<List<MarkerDataModel>>()
        if (valueStaticEventListener != null) {
            staticDatabaseReference.removeEventListener(valueStaticEventListener!!)
        }

        valueStaticEventListener =
            staticDatabaseReference.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    Log.e(TAG, "данные в startStaticMarkerUpdates обновлены")
                    savedStaticMarkers.clear()
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
        if (valueUserEventListener != null) {
            userDatabaseReference.removeEventListener(valueUserEventListener!!)
            staticDatabaseReference.removeEventListener(valueStaticEventListener!!)
        }
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
                // При отмене можно предпринять соответствующие действия, если необходимо
            }
        }
    }

}



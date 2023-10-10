package ru.newlevel.hordemap.data.repository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.maps.model.Marker
import com.google.firebase.database.*
import ru.newlevel.hordemap.data.storage.models.MarkerModel
import ru.newlevel.hordemap.domain.repository.GeoDataRepository

//private const val MESSAGE_PATH = "messages"
//private const val MESSAGE_FILE_FOLDER = "MessengerFiles"
private const val GEO_USER_MARKERS_PATH = "geoData0"
private const val GEO_STATIC_MARKERS_PATH = "geoMarkers0"
private const val TIME_TO_DELETE_USER_MARKER = 30 // в минутах


class GeoDataRepositoryImpl : GeoDataRepository {

    private val databaseReference by lazy(LazyThreadSafetyMode.NONE) { FirebaseDatabase.getInstance().reference }

    private val staticDatabaseReference = databaseReference.child(GEO_STATIC_MARKERS_PATH)
    private val userDatabaseReference = databaseReference.child(GEO_USER_MARKERS_PATH)

    private val savedUserMarkers: ArrayList<MarkerModel> = ArrayList()
    private val savedStaticMarkers: ArrayList<MarkerModel> = ArrayList()

    private var valueUserEventListener: ValueEventListener? = null
    private var valueStaticEventListener: ValueEventListener? = null

    override fun deleteStaticMarker(marker: Marker) {
        staticDatabaseReference.child(marker.tag.toString()).removeValue()
    }

    override fun startUserMarkerUpdates(): LiveData<List<MarkerModel>> {
        val liveDataUserMarkers = MutableLiveData<List<MarkerModel>>()

        if (valueUserEventListener != null) {
            userDatabaseReference.removeEventListener(valueUserEventListener!!)
        }
        valueUserEventListener = userDatabaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                Log.e("AAA", "данные в startUserMarkerUpdates обновлены")
                savedUserMarkers.clear()
                val timeNow = System.currentTimeMillis()
                for (snapshot in dataSnapshot.children) {
                    try {
                        var alpha: Float
                        val timestamp: Long? = snapshot.child("timestamp").getValue(Long::class.java)
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
                        val myMarker: MarkerModel = snapshot.getValue(MarkerModel::class.java)!!
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

    override fun startStaticMarkerUpdates(): LiveData<List<MarkerModel>> {
        val liveDataStaticMarkers = MutableLiveData<List<MarkerModel>>()
        if (valueStaticEventListener != null) {
            staticDatabaseReference.removeEventListener(valueStaticEventListener!!)
        }

        valueStaticEventListener = staticDatabaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                Log.e("AAA", "данные в startStaticMarkerUpdates обновлены")
                savedStaticMarkers.clear()
                for (snapshot in dataSnapshot.children) {
                    try {
                        val myMarker: MarkerModel = snapshot.getValue(MarkerModel::class.java)!!
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
        Log.e("AAA", "stopMarkerUpdates вызван")
        if (valueUserEventListener != null) {
            userDatabaseReference.removeEventListener(valueUserEventListener!!)
            staticDatabaseReference.removeEventListener(valueStaticEventListener!!)
        }
    }
}
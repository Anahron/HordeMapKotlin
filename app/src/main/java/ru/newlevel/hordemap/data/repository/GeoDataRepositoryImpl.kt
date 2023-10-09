package ru.newlevel.hordemap.data.repository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.database.*
import ru.newlevel.hordemap.data.storage.models.MarkerModel
import ru.newlevel.hordemap.domain.repository.GeoDataRepository

private const val MESSAGE_PATH = "messages"
private const val MESSAGE_FILE_FOLDER = "MessengerFiles"
private const val GEO_DATA_PATH = "geoData0"
private const val GEO_MARKERS_PATH = "geoMarkers0"
private const val TIME_TO_DELETE_USER_MARKER = 30 // в минутах


class GeoDataRepositoryImpl() : GeoDataRepository {

    private val databaseReference by lazy(LazyThreadSafetyMode.NONE) { FirebaseDatabase.getInstance().reference }

    private val savedUserMarkers: ArrayList<MarkerModel> = ArrayList()
    private val savedStaticMarkers: ArrayList<MarkerModel> = ArrayList()

    private var valueUserEventListener: ValueEventListener? = null
    private var valueStaticEventListener: ValueEventListener? = null

    override fun startUserMarkerUpdates(): LiveData<List<MarkerModel>> {
        val liveDataUserMarkers = MutableLiveData<List<MarkerModel>>()

        if (valueUserEventListener != null) {
            databaseReference.removeEventListener(valueUserEventListener!!)
        }
        valueUserEventListener = databaseReference.ref.child(GEO_DATA_PATH).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                savedUserMarkers.clear()
                val timeNow = System.currentTimeMillis()
                for (snapshot in dataSnapshot.children) {
                    try {
                        var alpha: Float
                        val timestamp: Long? =
                            snapshot.child("timestamp").getValue(Long::class.java)
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
            databaseReference.removeEventListener(valueStaticEventListener!!)
        }

        valueStaticEventListener = databaseReference.ref.child(GEO_MARKERS_PATH).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                savedStaticMarkers.clear()
                for (snapshot in dataSnapshot.children) {
                    try {
                        Log.e("AAA", snapshot.toString() + this)
                        val myMarker: MarkerModel = snapshot.getValue(MarkerModel::class.java)!!
                        savedStaticMarkers.add(myMarker)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                Log.e("AAA", savedStaticMarkers.toString())
                liveDataStaticMarkers.postValue(savedStaticMarkers)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("AAA", "ошибка в startStaticMarkerUpdates")
            }
        })
        return liveDataStaticMarkers
    }

    override fun stopMarkerUpdates() {
        Log.e("AAA", "stopMarkerUpdates вызван")
        if (valueUserEventListener != null) {
            databaseReference.removeEventListener(valueUserEventListener!!)
            databaseReference.removeEventListener(valueStaticEventListener!!)
        }
    }
}
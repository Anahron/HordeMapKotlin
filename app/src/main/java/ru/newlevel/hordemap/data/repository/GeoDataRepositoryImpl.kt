package ru.newlevel.hordemap.data.repository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.database.*
import ru.newlevel.hordemap.data.models.MarkerModel
import ru.newlevel.hordemap.domain.repository.GeoDataRepository

private const val MESSAGE_PATH = "messages"
private const val MESSAGE_FILE_FOLDER = "MessengerFiles"
private const val GEO_DATA_PATH = "geoData0"
private const val GEO_MARKERS_PATH = "geoMarkers0"
private const val TIME_TO_DELETE_USER_MARKER = 30 // в минутах


class GeoDataRepositoryImpl() : GeoDataRepository {

    private val databaseReference by lazy(LazyThreadSafetyMode.NONE) { FirebaseDatabase.getInstance().reference }
    private val geoDataReference = databaseReference.child(GEO_DATA_PATH)
    val markers: ArrayList<MarkerModel> = ArrayList()

    private var valueEventListener: ValueEventListener? = null

    override fun startMarkerUpdates(): LiveData<List<MarkerModel>> {
        val liveData = MutableLiveData<List<MarkerModel>>()

        if (valueEventListener != null) {
            geoDataReference.removeEventListener(valueEventListener!!)
        }

        valueEventListener = geoDataReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                markers.clear()
                val timeNow = System.currentTimeMillis()
                for (snapshot in dataSnapshot.children) {
                    try {
                        Log.e("AAA", snapshot.toString() + this)
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
                        markers.add(myMarker)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                liveData.postValue(markers)
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
                // Обработка ошибки
            }
        })
        return liveData
    }

    override fun stopMarkerUpdates() {
        Log.e("AAA", "stopMarkerUpdates вызван")
        if (valueEventListener != null) {
            geoDataReference.removeEventListener(valueEventListener!!)
        }
    }
}
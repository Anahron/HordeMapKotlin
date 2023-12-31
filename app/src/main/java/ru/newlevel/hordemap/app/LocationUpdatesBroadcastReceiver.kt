package ru.newlevel.hordemap.app

import android.content.Context
import android.content.Intent
import android.location.Location
import android.util.Log
import androidx.legacy.content.WakefulBroadcastReceiver
import com.google.android.gms.location.LocationAvailability
import com.google.android.gms.location.LocationResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import ru.newlevel.hordemap.data.db.MyDatabase
import ru.newlevel.hordemap.data.db.MyLocationEntity
import ru.newlevel.hordemap.data.db.UserEntityProvider
import ru.newlevel.hordemap.data.storage.interfaces.MarkersRemoteStorage
import ru.newlevel.hordemap.domain.usecases.mapCases.GetUserSettingsUseCase
import java.util.Date


class LocationUpdatesBroadcastReceiver : WakefulBroadcastReceiver(), KoinComponent {

    override fun onReceive(context: Context, intent: Intent?) {
        Log.e(TAG, "onReceive in LocationUpdatesBroadcastReceiver")
        if (intent?.action == ACTION_PROCESS_UPDATES) {
            LocationAvailability.extractLocationAvailability(intent)?.let { locationAvailability ->
                if (!locationAvailability.isLocationAvailable) {
                    Log.e(TAG, "Location services are no longer available!")
                }
            }
            Log.e(TAG, LocationResult.extractResult(intent).toString())
            LocationResult.extractResult(intent)?.let { locationResult ->
                val locations = locationResult.locations
                if (locations.isNotEmpty()) {
                    saveLocation(locations)
                }
            }
        } else Log.e(TAG, "intent.action != ACTION_PROCESS_UPDATES")
    }

    private fun saveLocation(locations: List<Location>) {
        try {
            val userEntity = getKoin().get<GetUserSettingsUseCase>().execute()
            val location = locations[locations.lastIndex]
            CoroutineScope(Dispatchers.IO).launch {
                val locationDao = getKoin().get<MyDatabase>().locationDao()
                locations.forEach { location ->
                    if (location.accuracy < 25)
                        locationDao.addLocation(
                            MyLocationEntity(
                                latitude = location.latitude,
                                longitude = location.longitude,
                                date = Date(location.time),
                                sessionId = UserEntityProvider.sessionId.toString()
                            )
                        )
                }
                userEntity.let {
                    getKoin().get<MarkersRemoteStorage>()
                        .sendUserMarker(location.toMarker(it))
                }
            }
        } catch (e: UninitializedPropertyAccessException) {
            e.printStackTrace()
        }
    }
}

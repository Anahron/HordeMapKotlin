package ru.newlevel.hordemap.domain.usecases

import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.SphericalUtil
import ru.newlevel.hordemap.data.db.UserEntityProvider
import ru.newlevel.hordemap.domain.models.TrackItemDomainModel
import ru.newlevel.hordemap.domain.repository.LocationRepository
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

class GetSessionLocationsUseCase(private val locationRepository: LocationRepository) {

    fun getCurrentSessionLocations(sessionId: String): TrackItemDomainModel? {
        val locationEntity = locationRepository.getLocationsSortedByUpdateTime(sessionId)
        val locationsList = locationEntity.map {
            LatLng(it.latitude, it.longitude)
        }
        val distance = SphericalUtil.computeLength(locationsList)
        return if (locationEntity.isNotEmpty()) TrackItemDomainModel(
            locationEntity[0].date.time,
            "My track",
            sessionId,
            convertTimestampToDate(locationEntity[0].date.time),
            calculateDurations(locationEntity[0].date.time - locationEntity[locationEntity.lastIndex].date.time),
            locationEntity[0].date.time - locationEntity[locationEntity.lastIndex].date.time,
            distanceToString(distance),
            distance.toInt(),
            locationsList
        ) else null
    }

    suspend fun getAllSessionLocations(): List<TrackItemDomainModel> {
        val uniqueSessionIds = locationRepository.getAllLocationsGroupedBySessionId()
        val allLocations = mutableListOf<TrackItemDomainModel>()

        for (sessionId in uniqueSessionIds) {
            val locationEntity = locationRepository.getLocationsSortedByUpdateTime(sessionId)
            val locationsList = locationEntity.map {
                LatLng(it.latitude, it.longitude)
            }
            if (locationEntity[0].sessionId != UserEntityProvider.sessionId.toString()) {
                val distance = SphericalUtil.computeLength(locationsList)
                if (distance < 300)
                    locationRepository.deleteLocationsBySessionId(sessionId = sessionId)
                else
                    allLocations.add(
                        TrackItemDomainModel(
                            locationEntity[0].date.time,
                            "My track",
                            sessionId,
                            convertTimestampToDate(locationEntity[0].date.time),
                            calculateDurations(locationEntity[0].date.time - locationEntity[locationEntity.lastIndex].date.time),
                            locationEntity[0].date.time - locationEntity[locationEntity.lastIndex].date.time,
                            distanceToString(distance),
                            distance.toInt(),
                            locationsList
                        )
                    )
            }
        }
        return allLocations.sortedByDescending { it.timestamp }
    }

    private fun distanceToString(resultDistance: Double): String {
        return when {
            resultDistance < 1000 -> "${resultDistance.roundToInt()}m"
            else -> String.format("%.2fkm", resultDistance / 1000)
        }
    }

    private fun calculateDurations(milliseconds: Long): String {
        val days = (milliseconds / (1000 * 60 * 60 * 24)).toInt()
        val hours = ((milliseconds % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60)).toInt()
        val minutes = ((milliseconds % (1000 * 60 * 60)) / (1000 * 60)).toInt()

        val formattedString = StringBuilder()

        if (days > 0) {
            formattedString.append("$days" + "d ")
        }
        if (hours > 0) {
            formattedString.append("$hours" + "h ")
        }
        if (minutes > 0) {
            formattedString.append("$minutes" + "m")
        } else
            formattedString.append("0m")
        return formattedString.toString()
    }

    private fun convertTimestampToDate(timestamp: Long): String {
        val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        val date = Date(timestamp)
        return dateFormat.format(date)
    }
}
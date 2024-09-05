package ru.newlevel.hordemap.domain.usecases.markersCases

import com.google.android.gms.maps.model.LatLng
import ru.newlevel.hordemap.data.db.MarkerEntity
import ru.newlevel.hordemap.data.db.UserEntityProvider
import ru.newlevel.hordemap.domain.repository.GeoDataRepository

class SendStaticMarkerUseCase(private val geoDataRepository: GeoDataRepository) {
    fun execute(latLng: LatLng, description: String, checkedItem: Int) {
        geoDataRepository.createStaticMarker(MarkerEntity().apply {
            latitude = latLng.latitude
            longitude = latLng.longitude
            title = description
            userName = UserEntityProvider.userEntity.name
            deviceId = UserEntityProvider.userEntity.deviceID
            timestamp = System.currentTimeMillis()
            item = checkedItem
        })
    }
}
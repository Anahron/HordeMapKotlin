package ru.newlevel.hordemap.domain.usecases

import com.google.android.gms.maps.model.LatLng
import ru.newlevel.hordemap.data.db.UserEntityProvider
import ru.newlevel.hordemap.data.storage.models.MarkerDataModel
import ru.newlevel.hordemap.domain.repository.GeoDataRepository

class CreateStaticMarkerUseCase(private val geoDataRepository: GeoDataRepository) {
    fun execute(latLng: LatLng, description: String, checkedItem: Int) {
        geoDataRepository.createStaticMarker(MarkerDataModel().apply {
            latitude = latLng.latitude
            longitude = latLng.longitude
            title = description
            userName = UserEntityProvider.userEntity?.name ?: "Аноним"
            deviceId = UserEntityProvider.userEntity?.deviceID ?: ""
            timestamp = System.currentTimeMillis()
            item = checkedItem
        })
    }
}
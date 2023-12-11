package ru.newlevel.hordemap.data.repository

import kotlinx.coroutines.flow.Flow
import ru.newlevel.hordemap.device.MySensorManager
import ru.newlevel.hordemap.domain.repository.SensorRepository

class SensorRepositoryImpl(private val mySensorManager: MySensorManager): SensorRepository {
    override fun getCompassData(): Flow<FloatArray> = mySensorManager.getCompassData()
}

package ru.newlevel.hordemap.domain.usecases.mapCases

import android.hardware.SensorManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ru.newlevel.hordemap.domain.repository.SensorRepository

class CompassInteractor(private val sensorRepository: SensorRepository) {

    private val orientationAngles = FloatArray(3)
    private val rotationVectorReading = FloatArray(3)
    private val rotationMatrix = FloatArray(9)

    fun getCompassData(): Flow<Float> {
        val sensorLiveData:  Flow<FloatArray>  = sensorRepository.getCompassData()
        val angleLiveData: Flow<Float> = sensorLiveData.map { sensorEvent ->
            return@map eventToAngle(sensorEvent)
        }
        return angleLiveData
    }

    private fun eventToAngle(event: FloatArray): Float {
        System.arraycopy(event, 0, rotationVectorReading, 0, rotationVectorReading.size)
        SensorManager.getRotationMatrixFromVector(rotationMatrix, rotationVectorReading)
        SensorManager.getOrientation(rotationMatrix, orientationAngles)
        return Math.toDegrees(orientationAngles[0].toDouble()).toFloat()
    }
}
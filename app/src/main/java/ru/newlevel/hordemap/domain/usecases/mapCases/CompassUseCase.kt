package ru.newlevel.hordemap.domain.usecases.mapCases

import android.hardware.SensorEvent
import android.hardware.SensorManager
import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import ru.newlevel.hordemap.domain.repository.SensorRepository

class CompassUseCase(private val sensorRepository: SensorRepository) {

    private val orientationAngles = FloatArray(3)
    private val rotationVectorReading = FloatArray(3)
    private val rotationMatrix = FloatArray(9)

    fun stopSensorEventListener(){
        sensorRepository.stopSensorEventListener()
    }

    fun startSensorEventListener(): LiveData<Float> {
        val sensorLiveData: LiveData<SensorEvent> = sensorRepository.startSensorEventListener()
        val angleLiveData: LiveData<Float> = sensorLiveData.map { sensorEvent ->
            return@map eventToAngle(sensorEvent)
        }
        return angleLiveData
    }

    private fun eventToAngle(event: SensorEvent): Float {
        System.arraycopy(event.values, 0, rotationVectorReading, 0, rotationVectorReading.size)
        SensorManager.getRotationMatrixFromVector(rotationMatrix, rotationVectorReading)
        SensorManager.getOrientation(rotationMatrix, orientationAngles)
        return Math.toDegrees(orientationAngles[0].toDouble()).toFloat()
    }
}
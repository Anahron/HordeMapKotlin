package ru.newlevel.hordemap.device

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import ru.newlevel.hordemap.app.TAG

class MySensorManager(private val sensorManager: SensorManager) {

    fun getCompassData(): Flow<FloatArray> = callbackFlow {
        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                if (event?.sensor?.type == Sensor.TYPE_ROTATION_VECTOR) {
                    trySend(event.values)
                }
            }
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }
        sensorManager.registerListener(
            listener,
            sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR),
            SensorManager.SENSOR_STATUS_ACCURACY_HIGH or 80000
        )
        awaitClose {
            Log.e(TAG, "sensorManager.unregisterListener(listener)")
            sensorManager.unregisterListener(listener)
        }
    }
}

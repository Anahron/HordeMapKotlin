package ru.newlevel.hordemap.device

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class MySensorManager(private val sensorManager: SensorManager): SensorEventListener  {
    private val mutableLiveDataEvent = MutableLiveData<SensorEvent>()
    private val liveDataEvent: LiveData<SensorEvent> get() = mutableLiveDataEvent

    fun compassOFF() {
        sensorManager.unregisterListener(this)
    }

    fun compassON(): LiveData<SensorEvent> {
        sensorManager.unregisterListener(this)
        sensorManager.registerListener(this,
                sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR),
            SensorManager.SENSOR_STATUS_ACCURACY_HIGH or 80000
        )
        return liveDataEvent
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_ROTATION_VECTOR) {
            mutableLiveDataEvent.postValue(event)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    }
}
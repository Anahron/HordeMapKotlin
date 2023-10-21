package ru.newlevel.hordemap.data.repository


import android.hardware.SensorEvent
import androidx.lifecycle.LiveData
import ru.newlevel.hordemap.device.MySensorManager
import ru.newlevel.hordemap.domain.repository.SensorRepository

class SensorRepositoryImpl(private val mySensorManager: MySensorManager): SensorRepository {
    override fun startSensorEventListener(): LiveData<SensorEvent> {
       return mySensorManager.compassON()
    }

    override fun stopSensorEventListener() {
        mySensorManager.compassOFF()
    }
}

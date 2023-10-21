package ru.newlevel.hordemap.domain.repository

import android.hardware.SensorEvent
import androidx.lifecycle.LiveData


interface SensorRepository {
    fun startSensorEventListener(): LiveData<SensorEvent>

    fun stopSensorEventListener()
}
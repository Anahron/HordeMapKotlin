package ru.newlevel.hordemap.domain.repository

import kotlinx.coroutines.flow.Flow


interface SensorRepository {
    fun getCompassData(): Flow<FloatArray>
}
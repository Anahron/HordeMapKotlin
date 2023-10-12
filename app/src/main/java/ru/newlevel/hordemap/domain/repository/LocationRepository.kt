package ru.newlevel.hordemap.domain.repository

interface LocationRepository {

    fun startLocationUpdates()

    fun stopLocationUpdates()

}

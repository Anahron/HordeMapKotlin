package ru.newlevel.hordemap.domain.usecases

import ru.newlevel.hordemap.domain.repository.MarkerRepository

class HideMarkersUseCase(private val markerRepository: MarkerRepository){
    fun execute(): Boolean{
        markerRepository.hideMarkers()
        return false
    }
}
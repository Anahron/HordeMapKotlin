package ru.newlevel.hordemap.domain.usecases

import ru.newlevel.hordemap.domain.repository.MarkerRepository

class ShowMarkersUseCase (private val markerRepository: MarkerRepository){
    fun execute(): Boolean{
        markerRepository.showMarkers()
        return true
    }
}
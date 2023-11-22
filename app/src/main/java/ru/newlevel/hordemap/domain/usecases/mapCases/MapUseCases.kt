package ru.newlevel.hordemap.domain.usecases.mapCases

import ru.newlevel.hordemap.domain.usecases.markersCases.DeleteMarkerUseCase
import ru.newlevel.hordemap.domain.usecases.markersCases.SendStaticMarkerUseCase
import ru.newlevel.hordemap.domain.usecases.markersCases.StartMarkerUpdateInteractor
import ru.newlevel.hordemap.domain.usecases.markersCases.StopMarkerUpdateInteractor

data class MapUseCases(
    val deleteMarkerUseCase: DeleteMarkerUseCase,
    val saveGameMapToFileUseCase: SaveGameMapToFileUseCase,
    val loadLastGameMapUseCase: LoadLastGameMapUseCase,
    val loadGameMapFromServerUseCase: LoadGameMapFromServerUseCase,
    val sendStaticMarkerUseCase: SendStaticMarkerUseCase,
    val stopMarkerUpdateInteractor: StopMarkerUpdateInteractor,
    val startMarkerUpdateInteractor: StartMarkerUpdateInteractor,
    val compassInteractor: CompassInteractor,
    val createRouteUseCase: CreateRouteUseCase,
    val locationUpdatesInteractor: LocationUpdatesInteractor,
)
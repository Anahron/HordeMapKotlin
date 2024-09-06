package ru.newlevel.hordemap.domain.usecases.mapCases

import ru.newlevel.hordemap.domain.usecases.markersCases.DeleteMarkerUseCase
import ru.newlevel.hordemap.domain.usecases.markersCases.SendStaticMarkerUseCase
import ru.newlevel.hordemap.domain.usecases.markersCases.StartMarkerUpdateInteractor

data class MapUseCases(
    val deleteMarkerUseCase: DeleteMarkerUseCase,
    val saveGameMapToFileUseCase: SaveGameMapToFileUseCase,
    val loadLastGameMapUseCase: LoadLastGameMapUseCase,
    val loadGameMapFromServerUseCase: LoadGameMapFromServerUseCase,
    val sendStaticMarkerUseCase: SendStaticMarkerUseCase,
    val insetMarkersToDBIterator: InsetMarkersToDBIterator,
    val startMarkerUpdateInteractor: StartMarkerUpdateInteractor,
    val compassInteractor: CompassInteractor,
    val createRouteUseCase: CreateRouteUseCase,
    val locationUpdatesInteractor: LocationUpdatesInteractor,
)
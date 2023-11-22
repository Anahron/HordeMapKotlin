package ru.newlevel.hordemap.domain.usecases.tracksCases

data class TracksUseCases(
    val getSessionLocationsUseCase: GetSessionLocationsUseCase,
    val deleteSessionLocationUseCase: DeleteSessionLocationUseCase,
    val renameTrackNameForSessionUseCase: RenameTrackNameForSessionUseCase,
    val setFavouriteTrackForSessionUseCase: SetFavouriteTrackForSessionUseCase,
    val saveCurrentTrackUseCase: SaveCurrentTrackUseCase,
    val deleteAllTracksUseCase: DeleteAllTracksUseCase
)
package ru.newlevel.hordemap.domain.usecases.messengerCases

data class MessengerUseCases(
    val stopMessageUpdateInteractor: StopMessageUpdateInteractor,
    val startMessageUpdateInteractor: StartMessageUpdateInteractor,
    val sendMessageUseCase: SendMessageUseCase,
    val sendFileUseCase: SendFileUseCase,
    val downloadFileUseCase: DownloadFileUseCase
)
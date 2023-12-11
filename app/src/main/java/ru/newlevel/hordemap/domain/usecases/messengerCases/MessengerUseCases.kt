package ru.newlevel.hordemap.domain.usecases.messengerCases

data class MessengerUseCases(
    val messageUpdateInteractor: MessageUpdateInteractor,
    val sendMessageUseCase: SendMessageUseCase,
    val uploadFileUseCase: UploadFileUseCase,
    val downloadFileUseCase: DownloadFileUseCase,
    val deleteMessageUseCase: DeleteMessageUseCase
)
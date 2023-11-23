package ru.newlevel.hordemap.presentation.sign_in

data class SingInResult(
    val data: UserData?,
    val errorMessage: String?
)
data class UserData(
    val userId:String,
    val userName: String?,
    val profileImageUrl: String?
)
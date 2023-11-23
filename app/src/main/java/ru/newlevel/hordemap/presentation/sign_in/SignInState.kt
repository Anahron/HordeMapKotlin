package ru.newlevel.hordemap.presentation.sign_in

data class SignInState(
    val isSingSuccess: Boolean = false,
    val signInError: String? = null
)
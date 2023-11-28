package ru.newlevel.hordemap.presentation.sign_in

import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.net.Uri
import android.util.Log
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.BeginSignInRequest.GoogleIdTokenRequestOptions
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import ru.newlevel.hordemap.R
import ru.newlevel.hordemap.app.TAG
import ru.newlevel.hordemap.domain.usecases.LogOutUseCase
import ru.newlevel.hordemap.domain.usecases.mapCases.GetUserSettingsUseCase
import java.util.concurrent.CancellationException

class GoogleAuthUiClient(
    private val context: Context,
    private val oneTapClient: SignInClient,
    private val logOutUseCase: LogOutUseCase,
    private val getUserSettingsUseCase: GetUserSettingsUseCase
) {

    private val auth = Firebase.auth
    suspend fun signInAnonymously(): SingInResult {
        return try {
            withContext(Dispatchers.IO) {
                val user = auth.signInAnonymously().await().user
                Log.d(TAG, "signInAnonymously:success")
                SingInResult(
                    data = user?.run {
                        UserData(
                            isAnonymous = isAnonymous,
                            userId = uid,
                            userName = displayName,
                            profileImageUrl = photoUrl?.toString()
                        )
                    }, errorMessage = null
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return SingInResult(
                data = null,
                errorMessage = e.message
            )
        }
    }


    suspend fun profileUpdate(newUserPhoto: Uri): SingInResult {
        return withContext(Dispatchers.IO) {
            val user = auth.currentUser
            val profileUpdates = UserProfileChangeRequest.Builder()
                .setPhotoUri(newUserPhoto)
                .build()

            val resultDeferred = CompletableDeferred<SingInResult>()
            user?.updateProfile(profileUpdates)?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val newUserData = auth.currentUser
                    val result = SingInResult(
                        data = newUserData?.let {
                            UserData(
                                isAnonymous = it.isAnonymous,
                                userId = it.uid,
                                userName = it.displayName,
                                profileImageUrl = it.photoUrl?.toString()
                            )
                        }, errorMessage = null
                    )
                    resultDeferred.complete(result)
                } else {
                    val result = SingInResult(
                        data = null,
                        errorMessage = "Failed to update user profile."
                    )
                    resultDeferred.complete(result)
                }
            }
            resultDeferred.await()
        }
    }


    suspend fun signIn(): MyResult<*> {
        val myResult = try {
            withContext(Dispatchers.IO) {
                oneTapClient.beginSignIn(
                    buildSignInRequest()
                )
            }.await()
        } catch (e: Exception) {
            e.printStackTrace()
            if (e is CancellationException) throw e
            return MyResult.Error(e)
        }
        return MyResult.Success(myResult?.pendingIntent?.intentSender as IntentSender)
    }

    suspend fun signInFromIntent(intent: Intent): SingInResult {
        val credential = oneTapClient.getSignInCredentialFromIntent(intent)
        val googleIdToken = credential.googleIdToken
        val googleCredentials = GoogleAuthProvider.getCredential(googleIdToken, null)
        return try {
            withContext(Dispatchers.IO) {
                val user = auth.signInWithCredential(googleCredentials).await().user
                SingInResult(
                    data = user?.run {
                        UserData(
                            userId = uid,
                            userName = displayName,
                            profileImageUrl = photoUrl?.toString()
                        )
                    }, errorMessage = null
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            if (e is CancellationException) throw e
            SingInResult(
                data = null,
                errorMessage = e.message
            )
        }
    }
    suspend fun deleteUserFromDatabase(){
        logOutUseCase.execute()
    }


    suspend fun signOut() {
        try {
            deleteUserFromDatabase()
            val currentUser = auth.currentUser
            Log.e(TAG, currentUser.toString())
            if (auth.currentUser?.isAnonymous == true)
                currentUser?.delete()
            oneTapClient.signOut().await()
            auth.signOut()
        } catch (e: Exception) {
            e.printStackTrace()
            if (e is CancellationException) throw e
        }
    }

    fun getSignedInUser(): UserData? {
        getUserSettingsUseCase.execute()
        return auth.currentUser?.run {
            UserData(
                userId = uid,
                userName = displayName,
                profileImageUrl = photoUrl?.toString()
            )
        }
    }

    private fun buildSignInRequest(): BeginSignInRequest {
        return BeginSignInRequest.builder()
            .setGoogleIdTokenRequestOptions(
                GoogleIdTokenRequestOptions.builder()
                    .setSupported(true)
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId(context.getString(R.string.web_client_id))
                    .build()
            )
            .setAutoSelectEnabled(true)
            .build()
    }
}
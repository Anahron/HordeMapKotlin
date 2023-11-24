package ru.newlevel.hordemap.presentation.sign_in

import android.content.Context
import android.content.Intent
import android.content.IntentSender
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.BeginSignInRequest.GoogleIdTokenRequestOptions
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import ru.newlevel.hordemap.R
import ru.newlevel.hordemap.presentation.MyResult
import java.util.concurrent.CancellationException

class GoogleAuthUiClient(private val context: Context, private val oneTapClient: SignInClient) {

    private val auth = Firebase.auth

    suspend fun signIn():  MyResult<*> {
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

    suspend fun signOut() {
        try {
            oneTapClient.signOut().await()
            auth.signOut()
        } catch (e: Exception) {
            e.printStackTrace()
            if (e is CancellationException) throw e
        }
    }

    fun getSignedInUser(): UserData? = auth.currentUser?.run {
        UserData(
            userId = uid,
            userName = displayName,
            profileImageUrl = photoUrl?.toString()
        )
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
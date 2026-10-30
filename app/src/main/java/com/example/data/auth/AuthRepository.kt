package com.example.data.auth

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.tasks.await

class AuthRepository(private val context: Context) {

  private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
  private val credentialManager = CredentialManager.create(context)

  val currentUser: FirebaseUser?
    get() = firebaseAuth.currentUser

  val isUserLoggedIn: Boolean
    get() = firebaseAuth.currentUser != null

  suspend fun signInWithGoogle(): Result<FirebaseUser> {
    return try {
      val googleIdOption = GetGoogleIdOption.Builder()
        .setServerClientId("YOUR_WEB_CLIENT_ID") // Replace with your actual Web Client ID from Firebase Console
        .setAutoSelectEnabled(true)
        .build()

      val request = GetCredentialRequest.Builder()
        .addCredentialOption(googleIdOption)
        .build()

      val result = credentialManager.getCredential(context, request)
      val credential = result.credential

      val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
      val idToken = googleIdTokenCredential.idToken

      val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
      val authResult = firebaseAuth.signInWithCredential(firebaseCredential).await()

      Result.success(authResult.user!!)
    } catch (e: GetCredentialException) {
      Result.failure(e)
    } catch (e: GoogleIdTokenParsingException) {
      Result.failure(e)
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  suspend fun signOut(): Result<Unit> {
    return try {
      firebaseAuth.signOut()
      Result.success(Unit)
    } catch (e: Exception) {
      Result.failure(e)
    }
  }
}

package com.example.ui.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.auth.AuthRepository
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AuthUiState(
  val isLoading: Boolean = false,
  val isAuthenticated: Boolean = false,
  val user: FirebaseUser? = null,
  val errorMessage: String? = null
)

class AuthViewModel(application: Application) : AndroidViewModel(application) {

  private val authRepository: AuthRepository by lazy {
    try {
      AuthRepository(application.applicationContext)
    } catch (e: Exception) {
      Log.e("AuthViewModel", "Failed to initialize AuthRepository", e)
      throw e
    }
  }

  private val _uiState = MutableStateFlow(AuthUiState())
  val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

  init {
    try {
      checkAuthStatus()
    } catch (e: Exception) {
      Log.e("AuthViewModel", "Failed to check auth status", e)
      _uiState.value = _uiState.value.copy(
        errorMessage = "Authentication error: ${e.message}"
      )
    }
  }

  private fun checkAuthStatus() {
    _uiState.value = _uiState.value.copy(
      isAuthenticated = authRepository.isUserLoggedIn,
      user = authRepository.currentUser
    )
  }

  fun signInWithGoogle() {
    viewModelScope.launch {
      _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

      authRepository.signInWithGoogle()
        .onSuccess { user ->
          _uiState.value = AuthUiState(
            isLoading = false,
            isAuthenticated = true,
            user = user,
            errorMessage = null
          )
        }
        .onFailure { error ->
          _uiState.value = AuthUiState(
            isLoading = false,
            isAuthenticated = false,
            user = null,
            errorMessage = "Sign-in failed: ${error.message}"
          )
        }
    }
  }

  fun signOut() {
    viewModelScope.launch {
      authRepository.signOut()
      _uiState.value = AuthUiState(
        isLoading = false,
        isAuthenticated = false,
        user = null,
        errorMessage = null
      )
    }
  }

  fun clearError() {
    _uiState.value = _uiState.value.copy(errorMessage = null)
  }
}

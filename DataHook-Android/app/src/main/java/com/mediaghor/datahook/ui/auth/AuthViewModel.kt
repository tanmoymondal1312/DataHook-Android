package com.mediaghor.datahook.ui.auth

import android.content.Context
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mediaghor.datahook.data.repository.AuthRepository
import com.mediaghor.datahook.util.GoogleSignInCancelled
import com.mediaghor.datahook.util.fetchFcmToken
import com.mediaghor.datahook.util.requestGoogleIdToken
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private val EMAIL_REGEX = Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")

data class AuthUiState(
    val isRegister: Boolean = false,
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val loading: Boolean = false,
    val error: String? = null,
    val nameError: String? = null,
    val emailError: String? = null,
    val passwordError: String? = null,
    val authenticated: Boolean = false,
)

class AuthViewModel(private val authRepository: AuthRepository) : ViewModel() {

    private val _state = MutableStateFlow(AuthUiState())
    val state: StateFlow<AuthUiState> = _state.asStateFlow()

    fun toggleMode() = _state.update {
        it.copy(
            isRegister = !it.isRegister,
            error = null,
            nameError = null,
            emailError = null,
            passwordError = null,
        )
    }

    fun onName(value: String) = _state.update { it.copy(name = value, nameError = null, error = null) }
    fun onEmail(value: String) = _state.update { it.copy(email = value, emailError = null, error = null) }
    fun onPassword(value: String) = _state.update { it.copy(password = value, passwordError = null, error = null) }

    fun submit() {
        val current = _state.value
        if (!validate(current)) return

        _state.update { it.copy(loading = true, error = null) }
        viewModelScope.launch {
            val result = if (current.isRegister) {
                authRepository.register(current.name, current.email, current.password)
            } else {
                authRepository.login(current.email, current.password)
            }

            result.fold(
                onSuccess = {
                    registerPushToken()
                    _state.update { it.copy(loading = false, authenticated = true) }
                },
                onFailure = { e ->
                    _state.update { it.copy(loading = false, error = e.message ?: "Authentication failed") }
                },
            )
        }
    }

    /**
     * Google sign-in. The same call serves both modes — the backend creates
     * the account when the email is new and reuses it when it is not, so the
     * button behaves identically on the login and register screens.
     */
    fun signInWithGoogle(context: Context) {
        if (_state.value.loading) return

        _state.update {
            it.copy(
                loading = true,
                error = null,
                nameError = null,
                emailError = null,
                passwordError = null,
            )
        }
        viewModelScope.launch {
            val token = requestGoogleIdToken(context).getOrElse { e ->
                // Dismissing the chooser is a normal action, not a failure.
                val message = if (e is GoogleSignInCancelled) null else googleErrorMessage(e)
                _state.update { it.copy(loading = false, error = message) }
                return@launch
            }

            authRepository.loginWithGoogle(token).fold(
                onSuccess = {
                    registerPushToken()
                    _state.update { it.copy(loading = false, authenticated = true) }
                },
                onFailure = { e ->
                    _state.update {
                        it.copy(loading = false, error = e.message ?: "Google sign-in failed")
                    }
                },
            )
        }
    }

    private fun googleErrorMessage(e: Throwable): String = when (e) {
        // Raised when no Google account is on the device, or Play Services is
        // missing/outdated — the generic message alone is not actionable.
        is NoCredentialException -> "No Google account found on this device"
        is GetCredentialException -> "Google sign-in unavailable: ${e.message}"
        else -> e.message ?: "Google sign-in failed"
    }

    private suspend fun registerPushToken() {
        val token = fetchFcmToken() ?: return
        authRepository.registerDevice(token)
    }

    private fun validate(s: AuthUiState): Boolean {
        var ok = true
        var nameErr: String? = null
        var emailErr: String? = null
        var passErr: String? = null

        if (s.isRegister && s.name.isBlank()) {
            nameErr = "Name is required"; ok = false
        }
        if (!EMAIL_REGEX.matches(s.email.trim())) {
            emailErr = "Enter a valid email"; ok = false
        }
        if (s.password.length < if (s.isRegister) 8 else 1) {
            passErr = if (s.isRegister) "At least 8 characters" else "Password is required"
            ok = false
        }

        if (!ok) {
            _state.update {
                it.copy(nameError = nameErr, emailError = emailErr, passwordError = passErr)
            }
        }
        return ok
    }
}

package com.mediaghor.datahook.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mediaghor.datahook.data.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SettingsUiState(
    val deleting: Boolean = false,
    val error: String? = null,
)

class SettingsViewModel(
    private val authRepository: AuthRepository,
) : ViewModel() {

    val email: Flow<String?> = authRepository.userEmail

    private val _state = MutableStateFlow(SettingsUiState())
    val state: StateFlow<SettingsUiState> = _state.asStateFlow()

    fun logout() {
        viewModelScope.launch { authRepository.logout() }
    }

    /**
     * Deletes the account for good. On success the session is already cleared
     * by the repository, so the app falls back to the auth screen on its own.
     */
    fun deleteAccount() {
        if (_state.value.deleting) return
        _state.update { it.copy(deleting = true, error = null) }
        viewModelScope.launch {
            authRepository.deleteAccount().onFailure { e ->
                _state.update {
                    it.copy(deleting = false, error = e.message ?: "Couldn't delete your account")
                }
            }
        }
    }

    fun consumeError() = _state.update { it.copy(error = null) }
}

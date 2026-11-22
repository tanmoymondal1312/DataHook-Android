package com.mediaghor.datahook.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mediaghor.datahook.data.local.AppPreferences
import com.mediaghor.datahook.data.repository.AuthRepository
import com.mediaghor.datahook.update.UpdateController
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SettingsUiState(
    val deleting: Boolean = false,
    val checkingUpdates: Boolean = false,
    /** Appearance — mirrors [AppPreferences] (MODE_SYSTEM / LIGHT / DARK). */
    val themeMode: Int = AppPreferences.MODE_SYSTEM,
    val dynamicColor: Boolean = false,
    val error: String? = null,
    /** One-shot info message (e.g. update-check result) for the snackbar. */
    val info: String? = null,
)

class SettingsViewModel(
    private val authRepository: AuthRepository,
    private val updateController: UpdateController,
    private val appPreferences: AppPreferences,
) : ViewModel() {

    val email: Flow<String?> = authRepository.userEmail

    private val _state = MutableStateFlow(SettingsUiState())
    val state: StateFlow<SettingsUiState> = _state.asStateFlow()

    init {
        // Mirror the persisted appearance prefs into the UI state.
        viewModelScope.launch {
            appPreferences.themeMode.collect { mode ->
                _state.update { it.copy(themeMode = mode) }
            }
        }
        viewModelScope.launch {
            appPreferences.dynamicColor.collect { enabled ->
                _state.update { it.copy(dynamicColor = enabled) }
            }
        }
    }

    fun setThemeMode(mode: Int) {
        viewModelScope.launch { appPreferences.setThemeMode(mode) }
    }

    fun setDynamicColor(enabled: Boolean) {
        viewModelScope.launch { appPreferences.setDynamicColor(enabled) }
    }

    fun logout() {
        viewModelScope.launch { authRepository.logout() }
    }

    /** Asks the backend whether a newer version exists; result lands in a snackbar. */
    fun checkForUpdates() {
        if (_state.value.checkingUpdates) return
        _state.update { it.copy(checkingUpdates = true) }
        updateController.recheck { message ->
            _state.update { it.copy(checkingUpdates = false, info = message) }
        }
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
    fun consumeInfo() = _state.update { it.copy(info = null) }
}

package com.mediaghor.datahook.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mediaghor.datahook.data.remote.dto.Endpoint
import com.mediaghor.datahook.data.repository.AuthRepository
import com.mediaghor.datahook.data.repository.EndpointRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DashboardUiState(
    val initialLoading: Boolean = true,
    val refreshing: Boolean = false,
    val endpoints: List<Endpoint> = emptyList(),
    val error: String? = null,
)

class DashboardViewModel(
    private val endpointRepository: EndpointRepository,
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(DashboardUiState())
    val state: StateFlow<DashboardUiState> = _state.asStateFlow()

    init {
        load()
    }

    fun load() {
        _state.update { it.copy(initialLoading = it.endpoints.isEmpty(), error = null) }
        fetch()
    }

    fun refresh() {
        _state.update { it.copy(refreshing = true, error = null) }
        fetch()
    }

    private fun fetch() {
        viewModelScope.launch {
            endpointRepository.getEndpoints().fold(
                onSuccess = { list ->
                    _state.update {
                        it.copy(
                            initialLoading = false,
                            refreshing = false,
                            endpoints = list.sortedByDescending { e -> e.id },
                            error = null,
                        )
                    }
                },
                onFailure = { e ->
                    _state.update {
                        it.copy(
                            initialLoading = false,
                            refreshing = false,
                            error = e.message ?: "Failed to load endpoints",
                        )
                    }
                },
            )
        }
    }

    fun logout() {
        viewModelScope.launch { authRepository.logout() }
    }
}

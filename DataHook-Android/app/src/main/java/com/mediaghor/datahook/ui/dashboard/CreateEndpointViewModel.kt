package com.mediaghor.datahook.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mediaghor.datahook.data.repository.EndpointRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CreateEndpointUiState(
    val name: String = "",
    val description: String = "",
    val loading: Boolean = false,
    val error: String? = null,
    val nameError: String? = null,
    val createdId: Int? = null,
)

class CreateEndpointViewModel(
    private val endpointRepository: EndpointRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(CreateEndpointUiState())
    val state: StateFlow<CreateEndpointUiState> = _state.asStateFlow()

    fun onName(value: String) = _state.update { it.copy(name = value, nameError = null, error = null) }
    fun onDescription(value: String) = _state.update { it.copy(description = value) }

    fun create() {
        val current = _state.value
        if (current.name.isBlank()) {
            _state.update { it.copy(nameError = "Name is required") }
            return
        }
        _state.update { it.copy(loading = true, error = null) }
        viewModelScope.launch {
            endpointRepository.createEndpoint(current.name, current.description).fold(
                onSuccess = { detail ->
                    _state.update { it.copy(loading = false, createdId = detail.id) }
                },
                onFailure = { e ->
                    _state.update { it.copy(loading = false, error = e.message ?: "Failed to create endpoint") }
                },
            )
        }
    }
}

package com.mediaghor.datahook.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mediaghor.datahook.data.remote.dto.FeedItem
import com.mediaghor.datahook.data.repository.AuthRepository
import com.mediaghor.datahook.data.repository.EndpointRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class FeedUiState(
    val initialLoading: Boolean = true,
    val refreshing: Boolean = false,
    val loadingMore: Boolean = false,
    val items: List<FeedItem> = emptyList(),
    val error: String? = null,
    val search: String = "",
    val page: Int = 1,
    val hasMore: Boolean = false,
    val totalCount: Int = 0,
)

class FeedViewModel(
    private val endpointRepository: EndpointRepository,
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(FeedUiState())
    val state: StateFlow<FeedUiState> = _state.asStateFlow()

    private var searchJob: Job? = null

    init {
        load()
    }

    /** Silent reload (keeps content on screen); shows the big spinner only when empty. */
    fun load() {
        _state.update { it.copy(initialLoading = it.items.isEmpty(), error = null) }
        fetch(reset = true)
    }

    fun refresh() {
        _state.update { it.copy(refreshing = true, error = null) }
        fetch(reset = true)
    }

    fun onSearchChange(query: String) {
        _state.update { it.copy(search = query) }
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(350)
            _state.update { it.copy(initialLoading = it.items.isEmpty()) }
            fetch(reset = true)
        }
    }

    fun loadMore() {
        val s = _state.value
        if (s.hasMore && !s.loadingMore && !s.initialLoading && !s.refreshing) {
            _state.update { it.copy(loadingMore = true) }
            fetch(reset = false)
        }
    }

    private fun fetch(reset: Boolean) {
        val current = _state.value
        val page = if (reset) 1 else current.page + 1
        viewModelScope.launch {
            endpointRepository.getFeed(current.search, page).fold(
                onSuccess = { data ->
                    _state.update {
                        val existing = if (reset) emptyList() else it.items
                        it.copy(
                            initialLoading = false,
                            refreshing = false,
                            loadingMore = false,
                            items = existing + data.results,
                            page = page,
                            hasMore = data.next != null,
                            totalCount = data.count,
                            error = null,
                        )
                    }
                },
                onFailure = { e ->
                    _state.update {
                        it.copy(
                            initialLoading = false,
                            refreshing = false,
                            loadingMore = false,
                            error = e.message ?: "Failed to load feed",
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

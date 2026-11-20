package com.mediaghor.datahook.ui.endpoint

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mediaghor.datahook.data.remote.dto.Attribute
import com.mediaghor.datahook.data.remote.dto.AttributeRequest
import com.mediaghor.datahook.data.remote.dto.AttributeUpdateRequest
import com.mediaghor.datahook.data.remote.dto.EndpointDetail
import com.mediaghor.datahook.data.remote.dto.EndpointStats
import com.mediaghor.datahook.data.remote.dto.Submission
import com.mediaghor.datahook.data.repository.EndpointRepository
import com.mediaghor.datahook.ui.navigation.Routes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ExportPayload(val fileName: String, val mimeType: String, val bytes: ByteArray)

data class SubmissionsState(
    val loading: Boolean = false,
    /** Pull-to-refresh spinner; keeps the list on screen while it runs. */
    val refreshing: Boolean = false,
    val loadingMore: Boolean = false,
    val items: List<Submission> = emptyList(),
    val error: String? = null,
    val search: String = "",
    val page: Int = 1,
    val hasMore: Boolean = false,
    val totalCount: Int = 0,
    val loadedOnce: Boolean = false,
)

data class EndpointDetailUiState(
    val loading: Boolean = true,
    val error: String? = null,
    val detail: EndpointDetail? = null,
    val stats: EndpointStats? = null,
    val attributes: List<Attribute> = emptyList(),
    val notifyUpdating: Boolean = false,
    val notifyTitleSaving: Boolean = false,
    val logoSaving: Boolean = false,
    val rotating: Boolean = false,
    val deleting: Boolean = false,
    val deleted: Boolean = false,
    val attributeSaving: Boolean = false,
    val attributeError: String? = null,
    val attributeSavedTick: Int = 0,
    val submissions: SubmissionsState = SubmissionsState(),
    val exporting: Boolean = false,
    val exportPayload: ExportPayload? = null,
    val message: String? = null,
)

class EndpointDetailViewModel(
    private val repository: EndpointRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val endpointId: Int = savedStateHandle.get<Int>(Routes.ARG_ENDPOINT_ID) ?: -1

    private val _state = MutableStateFlow(EndpointDetailUiState())
    val state: StateFlow<EndpointDetailUiState> = _state.asStateFlow()

    private var searchJob: Job? = null

    init {
        load()
    }

    fun load() {
        _state.update { it.copy(loading = it.detail == null, error = null) }
        viewModelScope.launch {
            val detailDeferred = async { repository.getEndpoint(endpointId) }
            val attrsDeferred = async { repository.getAttributes(endpointId) }
            val statsDeferred = async { repository.getStats(endpointId) }
            val detailResult = detailDeferred.await()
            val attrsResult = attrsDeferred.await()
            val statsResult = statsDeferred.await()

            detailResult.fold(
                onSuccess = { detail ->
                    _state.update {
                        it.copy(
                            loading = false,
                            detail = detail,
                            stats = statsResult.getOrNull() ?: it.stats,
                            attributes = attrsResult.getOrNull()
                                ?.sortedBy { a -> a.order } ?: it.attributes,
                            error = null,
                        )
                    }
                },
                onFailure = { e ->
                    _state.update { it.copy(loading = false, error = e.message) }
                },
            )
        }
    }

    // ---- Overview actions ----
    fun setNotify(enabled: Boolean) {
        _state.update { it.copy(notifyUpdating = true) }
        viewModelScope.launch {
            repository.setNotifyOnSubmit(endpointId, enabled).fold(
                onSuccess = { detail ->
                    _state.update { it.copy(notifyUpdating = false, detail = detail) }
                },
                onFailure = { e ->
                    _state.update { it.copy(notifyUpdating = false, message = e.message) }
                },
            )
        }
    }

    fun setNotifyTitle(title: String) {
        _state.update { it.copy(notifyTitleSaving = true) }
        viewModelScope.launch {
            repository.setNotifyTitle(endpointId, title).fold(
                onSuccess = { detail ->
                    _state.update {
                        it.copy(
                            notifyTitleSaving = false,
                            detail = detail,
                            message = if (title.isBlank()) {
                                "Using the default title"
                            } else {
                                "Notification title saved"
                            },
                        )
                    }
                },
                onFailure = { e ->
                    _state.update { it.copy(notifyTitleSaving = false, message = e.message) }
                },
            )
        }
    }

    fun uploadLogo(bytes: ByteArray, mimeType: String) {
        _state.update { it.copy(logoSaving = true) }
        viewModelScope.launch {
            repository.uploadLogo(endpointId, bytes, mimeType).fold(
                onSuccess = { detail ->
                    _state.update {
                        it.copy(logoSaving = false, detail = detail, message = "Logo updated")
                    }
                },
                onFailure = { e ->
                    _state.update { it.copy(logoSaving = false, message = e.message) }
                },
            )
        }
    }

    fun removeLogo() {
        _state.update { it.copy(logoSaving = true) }
        viewModelScope.launch {
            repository.removeLogo(endpointId).fold(
                onSuccess = { detail ->
                    _state.update {
                        it.copy(logoSaving = false, detail = detail, message = "Logo removed")
                    }
                },
                onFailure = { e ->
                    _state.update { it.copy(logoSaving = false, message = e.message) }
                },
            )
        }
    }

    fun rotateKey() {
        _state.update { it.copy(rotating = true) }
        viewModelScope.launch {
            repository.rotateKey(endpointId).fold(
                onSuccess = { detail ->
                    _state.update {
                        it.copy(rotating = false, detail = detail, message = "API key rotated")
                    }
                },
                onFailure = { e ->
                    _state.update { it.copy(rotating = false, message = e.message) }
                },
            )
        }
    }

    fun deleteEndpoint() {
        _state.update { it.copy(deleting = true) }
        viewModelScope.launch {
            repository.deleteEndpoint(endpointId).fold(
                onSuccess = { _state.update { it.copy(deleting = false, deleted = true) } },
                onFailure = { e -> _state.update { it.copy(deleting = false, message = e.message) } },
            )
        }
    }

    // ---- Attributes ----
    fun saveAttribute(
        existing: Attribute?,
        label: String,
        key: String,
        type: String,
        required: Boolean,
        flags: AttributeFlags,
    ) {
        _state.update { it.copy(attributeSaving = true, attributeError = null) }
        viewModelScope.launch {
            val result = if (existing == null) {
                val order = _state.value.attributes.size
                repository.addAttribute(
                    endpointId,
                    AttributeRequest(
                        label.trim(), key.trim(), type, required, order,
                        showInNotification = flags.showInNotification,
                        showAsSubtitle = flags.showAsSubtitle,
                        showAsDataHeader = flags.showAsDataHeader,
                    ),
                ).map { }
            } else {
                repository.updateAttribute(
                    endpointId,
                    existing.id,
                    AttributeUpdateRequest(
                        label.trim(), key.trim(), type, required,
                        showInNotification = flags.showInNotification,
                        showAsSubtitle = flags.showAsSubtitle,
                        showAsDataHeader = flags.showAsDataHeader,
                    ),
                ).map { }
            }

            result.fold(
                onSuccess = {
                    reloadAttributesAndDetail()
                    _state.update {
                        it.copy(
                            attributeSaving = false,
                            attributeError = null,
                            attributeSavedTick = it.attributeSavedTick + 1,
                        )
                    }
                },
                onFailure = { e ->
                    _state.update { it.copy(attributeSaving = false, attributeError = e.message) }
                },
            )
        }
    }

    fun deleteAttribute(attribute: Attribute) {
        viewModelScope.launch {
            repository.deleteAttribute(endpointId, attribute.id).fold(
                onSuccess = { reloadAttributesAndDetail() },
                onFailure = { e -> _state.update { it.copy(message = e.message) } },
            )
        }
    }

    private suspend fun reloadAttributesAndDetail() {
        val attrs = repository.getAttributes(endpointId).getOrNull()
        val detail = repository.getEndpoint(endpointId).getOrNull()
        _state.update {
            it.copy(
                attributes = attrs?.sortedBy { a -> a.order } ?: it.attributes,
                detail = detail ?: it.detail,
            )
        }
    }

    // ---- Submissions ----
    fun ensureSubmissionsLoaded() {
        val s = _state.value.submissions
        if (!s.loadedOnce && !s.loading) loadSubmissions(reset = true)
    }

    fun onSearchChange(query: String) {
        _state.update { it.copy(submissions = it.submissions.copy(search = query)) }
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(350)
            loadSubmissions(reset = true)
        }
    }

    /** Pull-to-refresh: top spinner only, list stays visible. */
    fun refreshSubmissions() = loadSubmissions(reset = true, pull = true)

    /** Error-state retry: full-screen spinner (the list is empty anyway). */
    fun retrySubmissions() = loadSubmissions(reset = true)

    private fun loadSubmissions(reset: Boolean, pull: Boolean = false) {
        val current = _state.value.submissions
        val page = if (reset) 1 else current.page + 1
        _state.update {
            it.copy(
                submissions = it.submissions.copy(
                    loading = reset && !pull,
                    refreshing = pull,
                    loadingMore = !reset,
                    error = null,
                ),
            )
        }
        viewModelScope.launch {
            repository.getSubmissions(endpointId, current.search, page).fold(
                onSuccess = { pageData ->
                    _state.update {
                        val existing = if (reset) emptyList() else it.submissions.items
                        it.copy(
                            submissions = it.submissions.copy(
                                loading = false,
                                refreshing = false,
                                loadingMore = false,
                                items = existing + pageData.results,
                                page = page,
                                hasMore = pageData.next != null,
                                totalCount = pageData.count,
                                error = null,
                                loadedOnce = true,
                            ),
                        )
                    }
                },
                onFailure = { e ->
                    _state.update {
                        it.copy(
                            submissions = it.submissions.copy(
                                loading = false,
                                refreshing = false,
                                loadingMore = false,
                                error = e.message,
                                loadedOnce = true,
                            ),
                        )
                    }
                },
            )
        }
    }

    fun loadMoreSubmissions() {
        val s = _state.value.submissions
        if (s.hasMore && !s.loading && !s.loadingMore) loadSubmissions(reset = false)
    }

    fun deleteSubmission(submission: Submission) {
        viewModelScope.launch {
            repository.deleteSubmission(endpointId, submission.id).fold(
                onSuccess = {
                    _state.update {
                        val newItems = it.submissions.items.filterNot { s -> s.id == submission.id }
                        it.copy(
                            submissions = it.submissions.copy(
                                items = newItems,
                                totalCount = (it.submissions.totalCount - 1).coerceAtLeast(0),
                            ),
                            message = "Submission deleted",
                        )
                    }
                },
                onFailure = { e -> _state.update { it.copy(message = e.message) } },
            )
        }
    }

    // ---- Export ----
    fun export(format: String) {
        _state.update { it.copy(exporting = true) }
        viewModelScope.launch {
            repository.export(endpointId, format).fold(
                onSuccess = { body ->
                    val bytes = withContext(Dispatchers.IO) { body.bytes() }
                    val name = _state.value.detail?.name.orEmpty().ifBlank { "endpoint" }
                        .lowercase().replace(Regex("[^a-z0-9]+"), "_").trim('_')
                    val fileName = "${name.ifBlank { "endpoint" }}_submissions.$format"
                    val mime = if (format == "csv") "text/csv" else "application/json"
                    _state.update {
                        it.copy(exporting = false, exportPayload = ExportPayload(fileName, mime, bytes))
                    }
                },
                onFailure = { e ->
                    _state.update { it.copy(exporting = false, message = e.message) }
                },
            )
        }
    }

    fun consumeExport() = _state.update { it.copy(exportPayload = null) }
    fun consumeMessage() = _state.update { it.copy(message = null) }
}

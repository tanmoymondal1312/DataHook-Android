package com.mediaghor.datahook.data.repository

import com.mediaghor.datahook.data.remote.DataHookApi
import com.mediaghor.datahook.data.remote.apiCall
import com.mediaghor.datahook.data.remote.dto.Attribute
import com.mediaghor.datahook.data.remote.dto.AttributeRequest
import com.mediaghor.datahook.data.remote.dto.AttributeUpdateRequest
import com.mediaghor.datahook.data.remote.dto.CreateEndpointRequest
import com.mediaghor.datahook.data.remote.dto.Endpoint
import com.mediaghor.datahook.data.remote.dto.EndpointDetail
import com.mediaghor.datahook.data.remote.dto.EndpointStats
import com.mediaghor.datahook.data.remote.dto.PaginatedFeed
import com.mediaghor.datahook.data.remote.dto.PaginatedSubmissions
import com.mediaghor.datahook.data.remote.dto.UpdateEndpointRequest
import com.mediaghor.datahook.data.remote.unwrapUnit
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody

class EndpointRepository(private val api: DataHookApi) {

    // ---- Endpoints ----
    suspend fun getEndpoints(): Result<List<Endpoint>> = apiCall { api.getEndpoints() }

    suspend fun createEndpoint(name: String, description: String?): Result<EndpointDetail> =
        apiCall {
            api.createEndpoint(
                CreateEndpointRequest(name.trim(), description?.trim()?.ifBlank { null }),
            )
        }

    suspend fun getEndpoint(id: Int): Result<EndpointDetail> = apiCall { api.getEndpoint(id) }

    suspend fun setNotifyOnSubmit(id: Int, enabled: Boolean): Result<EndpointDetail> =
        apiCall { api.updateEndpoint(id, UpdateEndpointRequest(notifyOnSubmit = enabled)) }

    /** Blank clears the custom title, restoring "New submission · <name>". */
    suspend fun setNotifyTitle(id: Int, title: String): Result<EndpointDetail> =
        apiCall { api.updateEndpoint(id, UpdateEndpointRequest(notifyTitle = title.trim())) }

    /** Uploads the notification logo; returns the refreshed detail. */
    suspend fun uploadLogo(
        id: Int,
        bytes: ByteArray,
        mimeType: String,
    ): Result<EndpointDetail> = apiCall {
        val part = MultipartBody.Part.createFormData(
            name = "logo",
            // The server derives the stored name; the extension just keeps it tidy.
            filename = "logo.${mimeType.substringAfterLast('/', "png")}",
            body = bytes.toRequestBody(mimeType.toMediaTypeOrNull()),
        )
        api.uploadLogo(id, part)
    }

    suspend fun removeLogo(id: Int): Result<EndpointDetail> =
        apiCall { api.deleteLogo(id) }

    suspend fun deleteEndpoint(id: Int): Result<Unit> =
        apiCall { api.deleteEndpoint(id) }.unwrapUnit()

    /** Rotates the API key then returns the refreshed detail (new key + snippets). */
    suspend fun rotateKey(id: Int): Result<EndpointDetail> = apiCall {
        api.rotateKey(id)
        api.getEndpoint(id)
    }

    // ---- Attributes ----
    suspend fun getAttributes(id: Int): Result<List<Attribute>> = apiCall { api.getAttributes(id) }

    suspend fun addAttribute(id: Int, request: AttributeRequest): Result<Attribute> =
        apiCall { api.createAttribute(id, request) }

    suspend fun updateAttribute(
        id: Int,
        attributeId: Int,
        request: AttributeUpdateRequest,
    ): Result<Attribute> = apiCall { api.updateAttribute(id, attributeId, request) }

    suspend fun deleteAttribute(id: Int, attributeId: Int): Result<Unit> =
        apiCall { api.deleteAttribute(id, attributeId) }.unwrapUnit()

    // ---- Submissions ----
    suspend fun getSubmissions(id: Int, search: String?, page: Int): Result<PaginatedSubmissions> =
        apiCall { api.getSubmissions(id, search?.ifBlank { null }, page) }

    suspend fun deleteSubmission(id: Int, submissionId: Int): Result<Unit> =
        apiCall { api.deleteSubmission(id, submissionId) }.unwrapUnit()

    // ---- Export ----
    suspend fun export(id: Int, format: String): Result<ResponseBody> =
        apiCall { api.export(id, format) }

    // ---- Home feed (all endpoints) ----
    suspend fun getFeed(search: String?, page: Int): Result<PaginatedFeed> =
        apiCall { api.getFeed(search?.ifBlank { null }, page) }

    // ---- Endpoint analytics ----
    suspend fun getStats(id: Int): Result<EndpointStats> = apiCall { api.getStats(id) }
}

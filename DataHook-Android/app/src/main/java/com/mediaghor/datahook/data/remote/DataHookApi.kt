package com.mediaghor.datahook.data.remote

import com.mediaghor.datahook.data.remote.dto.Attribute
import com.mediaghor.datahook.data.remote.dto.AttributeRequest
import com.mediaghor.datahook.data.remote.dto.AttributeUpdateRequest
import com.mediaghor.datahook.data.remote.dto.AuthResponse
import com.mediaghor.datahook.data.remote.dto.GoogleAuthRequest
import com.mediaghor.datahook.data.remote.dto.CreateEndpointRequest
import com.mediaghor.datahook.data.remote.dto.DeviceRequest
import com.mediaghor.datahook.data.remote.dto.Endpoint
import com.mediaghor.datahook.data.remote.dto.EndpointDetail
import com.mediaghor.datahook.data.remote.dto.EndpointStats
import com.mediaghor.datahook.data.remote.dto.LoginRequest
import com.mediaghor.datahook.data.remote.dto.PaginatedFeed
import com.mediaghor.datahook.data.remote.dto.PaginatedSubmissions
import com.mediaghor.datahook.data.remote.dto.RegisterRequest
import com.mediaghor.datahook.data.remote.dto.RotateKeyResponse
import com.mediaghor.datahook.data.remote.dto.UpdateEndpointRequest
import com.mediaghor.datahook.data.remote.dto.User
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import okhttp3.MultipartBody
import retrofit2.http.GET
import retrofit2.http.HTTP
import retrofit2.http.PATCH
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Streaming

interface DataHookApi {

    // ---- Auth ----
    @POST("api/auth/register/")
    suspend fun register(@Body body: RegisterRequest): AuthResponse

    @POST("api/auth/login/")
    suspend fun login(@Body body: LoginRequest): AuthResponse

    @POST("api/auth/google/")
    suspend fun googleLogin(@Body body: GoogleAuthRequest): AuthResponse

    @GET("api/auth/me/")
    suspend fun me(): User

    /** Permanently erases the account and everything it owns. */
    @DELETE("api/auth/me/")
    suspend fun deleteAccount(): Response<Unit>

    // ---- Devices (FCM) ----
    @POST("api/devices/")
    suspend fun registerDevice(@Body body: DeviceRequest): Response<Unit>

    @HTTP(method = "DELETE", path = "api/devices/", hasBody = true)
    suspend fun deleteDevice(@Body body: DeviceRequest): Response<Unit>

    // ---- Endpoints ----
    @GET("api/endpoints/")
    suspend fun getEndpoints(): List<Endpoint>

    @POST("api/endpoints/")
    suspend fun createEndpoint(@Body body: CreateEndpointRequest): EndpointDetail

    @GET("api/endpoints/{id}/")
    suspend fun getEndpoint(@Path("id") id: Int): EndpointDetail

    @PATCH("api/endpoints/{id}/")
    suspend fun updateEndpoint(@Path("id") id: Int, @Body body: UpdateEndpointRequest): EndpointDetail

    @DELETE("api/endpoints/{id}/")
    suspend fun deleteEndpoint(@Path("id") id: Int): Response<Unit>

    @Multipart
    @POST("api/endpoints/{id}/logo/")
    suspend fun uploadLogo(
        @Path("id") id: Int,
        @Part logo: MultipartBody.Part,
    ): EndpointDetail

    @DELETE("api/endpoints/{id}/logo/")
    suspend fun deleteLogo(@Path("id") id: Int): EndpointDetail

    @POST("api/endpoints/{id}/rotate-key/")
    suspend fun rotateKey(@Path("id") id: Int): RotateKeyResponse

    // ---- Attributes ----
    @GET("api/endpoints/{id}/attributes/")
    suspend fun getAttributes(@Path("id") id: Int): List<Attribute>

    @POST("api/endpoints/{id}/attributes/")
    suspend fun createAttribute(@Path("id") id: Int, @Body body: AttributeRequest): Attribute

    @PATCH("api/endpoints/{id}/attributes/{aid}/")
    suspend fun updateAttribute(
        @Path("id") id: Int,
        @Path("aid") aid: Int,
        @Body body: AttributeUpdateRequest,
    ): Attribute

    @DELETE("api/endpoints/{id}/attributes/{aid}/")
    suspend fun deleteAttribute(@Path("id") id: Int, @Path("aid") aid: Int): Response<Unit>

    // ---- Submissions ----
    @GET("api/endpoints/{id}/submissions/")
    suspend fun getSubmissions(
        @Path("id") id: Int,
        @Query("search") search: String?,
        @Query("page") page: Int,
    ): PaginatedSubmissions

    @DELETE("api/endpoints/{id}/submissions/{sid}/")
    suspend fun deleteSubmission(@Path("id") id: Int, @Path("sid") sid: Int): Response<Unit>

    // ---- Export ----
    @Streaming
    @GET("api/endpoints/{id}/export/")
    suspend fun export(@Path("id") id: Int, @Query("format") format: String): ResponseBody

    // ---- Cross-endpoint feed (home) ----
    @GET("api/submissions/")
    suspend fun getFeed(
        @Query("search") search: String?,
        @Query("page") page: Int,
    ): PaginatedFeed

    // ---- Per-endpoint analytics ----
    @GET("api/endpoints/{id}/stats/")
    suspend fun getStats(@Path("id") id: Int): EndpointStats
}

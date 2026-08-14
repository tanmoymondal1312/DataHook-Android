package com.mediaghor.datahook.data.remote

import com.mediaghor.datahook.data.remote.dto.RefreshRequest
import com.mediaghor.datahook.data.remote.dto.RefreshResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * Dedicated, synchronous refresh call used by [TokenAuthenticator]. It runs on a
 * bare OkHttp client WITHOUT the auth interceptor/authenticator so refreshing can
 * never recurse into itself.
 */
interface RefreshApi {
    @POST("api/auth/refresh/")
    fun refresh(@Body body: RefreshRequest): Call<RefreshResponse>
}

package com.mediaghor.datahook.data.remote

import com.mediaghor.datahook.data.remote.dto.AppUpdateConfig
import retrofit2.http.GET

/**
 * Unauthenticated endpoints. Kept separate from [DataHookApi] (which attaches
 * the JWT machinery) so the update check stays light — exactly like
 * [RefreshApi] for token refresh.
 */
interface UpdateApi {

    @GET("api/app-update/")
    suspend fun getAppUpdate(): AppUpdateConfig
}
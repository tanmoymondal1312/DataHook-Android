package com.mediaghor.datahook.data.repository

import com.mediaghor.datahook.data.remote.UpdateApi
import com.mediaghor.datahook.data.remote.apiCall
import com.mediaghor.datahook.data.remote.dto.AppUpdateConfig

class UpdateRepository(private val api: UpdateApi) {

    /** Fetches the backend-controlled update configuration; never throws. */
    suspend fun fetch(): Result<AppUpdateConfig> = apiCall { api.getAppUpdate() }
}
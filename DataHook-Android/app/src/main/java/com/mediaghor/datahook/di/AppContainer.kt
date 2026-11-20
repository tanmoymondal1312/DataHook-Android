package com.mediaghor.datahook.di

import android.content.Context
import com.mediaghor.datahook.BuildConfig
import com.mediaghor.datahook.data.local.AppPreferences
import com.mediaghor.datahook.data.local.TokenManager
import com.mediaghor.datahook.data.remote.ApiConfig
import com.mediaghor.datahook.data.remote.AuthInterceptor
import com.mediaghor.datahook.data.remote.DataHookApi
import com.mediaghor.datahook.data.remote.RefreshApi
import com.mediaghor.datahook.data.remote.TokenAuthenticator
import com.mediaghor.datahook.data.repository.AuthRepository
import com.mediaghor.datahook.data.repository.EndpointRepository
import com.mediaghor.datahook.data.repository.UpdateRepository
import com.mediaghor.datahook.data.remote.UpdateApi
import com.mediaghor.datahook.update.UpdateController
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import retrofit2.Retrofit
import retrofit2.create
import java.util.concurrent.TimeUnit

/**
 * Manual DI container. One instance lives on the [com.mediaghor.datahook.DataHookApp]
 * for the process lifetime and hands repositories to ViewModels.
 */
class AppContainer(context: Context) {

    private val appContext = context.applicationContext

    val tokenManager = TokenManager(appContext)
    val appPreferences = AppPreferences(appContext)

    private val contentType = "application/json".toMediaType()
    private val converterFactory = ApiConfig.json.asConverterFactory(contentType)

    // --- Bare client + Retrofit for token refresh only (no auth, no recursion) ---
    private val bareClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    private val refreshApi: RefreshApi = Retrofit.Builder()
        .baseUrl(ApiConfig.BASE_URL)
        .client(bareClient)
        .addConverterFactory(converterFactory)
        .build()
        .create()

    // --- Authenticated client: attaches Bearer + refreshes on 401 ---
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor.Level.BODY
        } else {
            HttpLoggingInterceptor.Level.NONE
        }
    }

    private val authClient = OkHttpClient.Builder()
        .addInterceptor(AuthInterceptor(tokenManager))
        .authenticator(TokenAuthenticator(tokenManager) { refreshApi })
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val api: DataHookApi = Retrofit.Builder()
        .baseUrl(ApiConfig.BASE_URL)
        .client(authClient)
        .addConverterFactory(converterFactory)
        .build()
        .create()

    // --- Update check: bare client with short timeouts so a slow backend
    // never stalls the app's startup path. No auth, no logging. ---
    private val updateClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    private val updateApi: UpdateApi = Retrofit.Builder()
        .baseUrl(ApiConfig.BASE_URL)
        .client(updateClient)
        .addConverterFactory(converterFactory)
        .build()
        .create()

    val authRepository = AuthRepository(api, tokenManager)
    val endpointRepository = EndpointRepository(api)
    val updateController = UpdateController(
        repository = UpdateRepository(updateApi),
        currentVersionCode = BuildConfig.VERSION_CODE,
    )
}

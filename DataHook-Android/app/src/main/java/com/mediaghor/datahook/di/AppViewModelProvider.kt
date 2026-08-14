package com.mediaghor.datahook.di

import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.mediaghor.datahook.DataHookApp
import com.mediaghor.datahook.ui.auth.AuthViewModel
import com.mediaghor.datahook.ui.dashboard.CreateEndpointViewModel
import com.mediaghor.datahook.ui.dashboard.DashboardViewModel
import com.mediaghor.datahook.ui.endpoint.EndpointDetailViewModel
import com.mediaghor.datahook.ui.home.FeedViewModel
import com.mediaghor.datahook.ui.settings.SettingsViewModel

/**
 * Single factory that constructs every ViewModel from the app's [com.mediaghor.datahook.di.AppContainer].
 * Screens use `viewModel(factory = AppViewModelProvider.Factory)`.
 */
object AppViewModelProvider {
    val Factory = viewModelFactory {
        initializer {
            AuthViewModel(app().container.authRepository)
        }
        initializer {
            DashboardViewModel(
                app().container.endpointRepository,
                app().container.authRepository,
            )
        }
        initializer {
            FeedViewModel(
                app().container.endpointRepository,
                app().container.authRepository,
            )
        }
        initializer {
            CreateEndpointViewModel(app().container.endpointRepository)
        }
        initializer {
            EndpointDetailViewModel(
                app().container.endpointRepository,
                createSavedStateHandle(),
            )
        }
        initializer {
            SettingsViewModel(app().container.authRepository)
        }
    }
}

private fun CreationExtras.app(): DataHookApp = this[APPLICATION_KEY] as DataHookApp

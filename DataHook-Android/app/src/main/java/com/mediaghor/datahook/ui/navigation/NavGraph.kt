package com.mediaghor.datahook.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.mediaghor.datahook.data.repository.AuthRepository
import com.mediaghor.datahook.ui.auth.AuthScreen
import com.mediaghor.datahook.ui.common.FullScreenLoading
import com.mediaghor.datahook.ui.common.LocalSnackbarController
import com.mediaghor.datahook.ui.common.SnackbarController
import com.mediaghor.datahook.ui.dashboard.CreateEndpointScreen
import com.mediaghor.datahook.ui.dashboard.EndpointsScreen
import com.mediaghor.datahook.ui.endpoint.EndpointDetailScreen
import com.mediaghor.datahook.ui.home.HomeScreen
import com.mediaghor.datahook.ui.endpoint.SubmissionDetailScreen
import com.mediaghor.datahook.ui.settings.SettingsScreen
import com.mediaghor.datahook.util.SubmissionCodec

/**
 * App root: provides the app-wide snackbar, gates on login state, and hosts the
 * navigation graph. [deepLinkEndpointId] comes from a tapped notification.
 */
@Composable
fun DataHookRoot(
    authRepository: AuthRepository,
    deepLinkEndpointId: Int?,
    deepLinkSubmissionPayload: String?,
    onDeepLinkConsumed: () -> Unit,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val snackbarController = remember(snackbarHostState) {
        SnackbarController(snackbarHostState, scope)
    }

    val loggedIn by authRepository.isLoggedIn.collectAsStateWithLifecycle(initialValue = null)

    CompositionLocalProvider(LocalSnackbarController provides snackbarController) {
        Box(Modifier.fillMaxSize()) {
            when (val isLoggedIn = loggedIn) {
                null -> FullScreenLoading()
                else -> DataHookNavGraph(
                    startLoggedIn = isLoggedIn,
                    loggedIn = isLoggedIn,
                    deepLinkEndpointId = deepLinkEndpointId,
                    deepLinkSubmissionPayload = deepLinkSubmissionPayload,
                    onDeepLinkConsumed = onDeepLinkConsumed,
                )
            }

            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .imePadding(),
            )
        }
    }
}

@Composable
private fun DataHookNavGraph(
    startLoggedIn: Boolean,
    loggedIn: Boolean,
    deepLinkEndpointId: Int?,
    deepLinkSubmissionPayload: String?,
    onDeepLinkConsumed: () -> Unit,
) {
    val navController = rememberNavController()

    // Bounce to Auth whenever the session is cleared (explicit logout or a failed refresh).
    LaunchedEffect(loggedIn) {
        if (!loggedIn) {
            navController.navigate(Routes.AUTH) {
                popUpTo(0) { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    // Handle a notification deep link once we're authenticated. A submission
    // payload opens the exact submission; otherwise fall back to the endpoint.
    LaunchedEffect(deepLinkEndpointId, deepLinkSubmissionPayload, loggedIn) {
        if (!loggedIn) return@LaunchedEffect
        when {
            deepLinkSubmissionPayload != null -> {
                navController.navigate(Routes.submissionDetail(deepLinkSubmissionPayload))
                onDeepLinkConsumed()
            }
            deepLinkEndpointId != null -> {
                navController.navigate(Routes.endpointDetail(deepLinkEndpointId, Routes.TAB_SUBMISSIONS))
                onDeepLinkConsumed()
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = if (startLoggedIn) Routes.DASHBOARD else Routes.AUTH,
    ) {
        composable(Routes.AUTH) {
            AuthScreen(
                onAuthenticated = {
                    navController.navigate(Routes.DASHBOARD) {
                        popUpTo(Routes.AUTH) { inclusive = true }
                        launchSingleTop = true
                    }
                },
            )
        }

        composable(Routes.DASHBOARD) {
            HomeScreen(
                onOpenEndpoints = { navController.navigate(Routes.ENDPOINTS) },
                onCreateEndpoint = { navController.navigate(Routes.CREATE_ENDPOINT) },
                onOpenSettings = { navController.navigate(Routes.SETTINGS) },
                onOpenSubmission = { submission ->
                    navController.navigate(Routes.submissionDetail(SubmissionCodec.encode(submission)))
                },
            )
        }

        composable(Routes.ENDPOINTS) {
            EndpointsScreen(
                onOpenEndpoint = { id -> navController.navigate(Routes.endpointDetail(id)) },
                onCreateEndpoint = { navController.navigate(Routes.CREATE_ENDPOINT) },
                onBack = { navController.popBackStack() },
            )
        }

        composable(Routes.CREATE_ENDPOINT) {
            CreateEndpointScreen(
                onBack = { navController.popBackStack() },
                onCreated = { id ->
                    navController.navigate(Routes.endpointDetail(id)) {
                        popUpTo(Routes.CREATE_ENDPOINT) { inclusive = true }
                    }
                },
            )
        }

        composable(
            route = Routes.ENDPOINT_DETAIL,
            arguments = listOf(
                navArgument(Routes.ARG_ENDPOINT_ID) { type = NavType.IntType },
                navArgument(Routes.ARG_TAB) {
                    type = NavType.IntType
                    defaultValue = 0
                },
            ),
        ) { backStackEntry ->
            val initialTab = backStackEntry.arguments?.getInt(Routes.ARG_TAB) ?: 0
            EndpointDetailScreen(
                initialTab = initialTab,
                onBack = { navController.popBackStack() },
                onOpenSubmission = { submission ->
                    navController.navigate(Routes.submissionDetail(SubmissionCodec.encode(submission)))
                },
            )
        }

        composable(
            route = Routes.SUBMISSION_DETAIL,
            arguments = listOf(navArgument(Routes.ARG_SUBMISSION) { type = NavType.StringType }),
        ) { backStackEntry ->
            val payload = backStackEntry.arguments?.getString(Routes.ARG_SUBMISSION).orEmpty()
            SubmissionDetailScreen(
                submission = SubmissionCodec.decode(payload),
                onBack = { navController.popBackStack() },
            )
        }

        composable(Routes.SETTINGS) {
            SettingsScreen(onBack = { navController.popBackStack() })
        }
    }
}

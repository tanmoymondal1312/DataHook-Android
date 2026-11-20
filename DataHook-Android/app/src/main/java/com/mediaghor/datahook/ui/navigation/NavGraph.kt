package com.mediaghor.datahook.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.mediaghor.datahook.R
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

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

    // Handle a notification deep link once we're authenticated.
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
        startDestination = if (startLoggedIn) Routes.MAIN else Routes.AUTH,
    ) {
        // Auth screen — no bottom navigation.
        composable(Routes.AUTH) {
            AuthScreen(
                onAuthenticated = {
                    navController.navigate(Routes.MAIN) {
                        popUpTo(Routes.AUTH) { inclusive = true }
                        launchSingleTop = true
                    }
                },
            )
        }

        // Main tab destination — contains the bottom nav + nested tab screens.
        composable(Routes.MAIN) {
            BottomNavHost(
                navController = navController,
                onOpenEndpoint = { id ->
                    navController.navigate(Routes.endpointDetail(id))
                },
                onOpenSubmission = { submission ->
                    navController.navigate(Routes.submissionDetail(SubmissionCodec.encode(submission)))
                },
            )
        }

        // Nested screens — no bottom navigation.
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
    }
}

/**
 * Hosts the three main tabs (Feed, Endpoints, Settings) inside a Scaffold so
 * the bottom navigation bar is always present on these screens.
 */
@Composable
private fun BottomNavHost(
    navController: androidx.navigation.NavHostController,
    onOpenEndpoint: (Int) -> Unit,
    onOpenSubmission: (com.mediaghor.datahook.data.remote.dto.Submission) -> Unit,
) {
    val tabState = remember { mutableStateOf(Routes.FEED) }

    Scaffold(
        bottomBar = {
            BottomNavBar(
                navController = navController,
                selectedTab = tabState.value,
                onTabSelected = { tabState.value = it },
                onNavigateTo = { route ->
                    navController.navigate(route) {
                        popUpTo(navController.graph.startDestinationId) { inclusive = false }
                        launchSingleTop = true
                    }
                },
            )
        },
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Routes.FEED,
        ) {
            composable(Routes.FEED) {
                HomeScreen(
                    onOpenEndpoints = { navController.navigate(Routes.ENDPOINTS) },
                    onCreateEndpoint = { navController.navigate(Routes.CREATE_ENDPOINT) },
                    onOpenSettings = { navController.navigate(Routes.SETTINGS) },
                    onOpenSubmission = onOpenSubmission,
                    onOpenEndpoint = onOpenEndpoint,
                )
            }
            composable(Routes.ENDPOINTS) {
                EndpointsScreen(
                    onOpenEndpoint = onOpenEndpoint,
                    onCreateEndpoint = { navController.navigate(Routes.CREATE_ENDPOINT) },
                    onBack = { /* no-op: bottom nav handles navigation */ },
                )
            }
            composable(Routes.SETTINGS) {
                SettingsScreen(
                    onBack = { /* no-op: bottom nav handles navigation */ },
                )
            }
        }
    }
}

/**
 * Custom bottom navigation bar with three destinations: Feed, Endpoints, Settings.
 */
@Composable
private fun BottomNavBar(
    navController: androidx.navigation.NavHostController,
    selectedTab: String,
    onTabSelected: (String) -> Unit,
    onNavigateTo: (String) -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .padding(horizontal = 8.dp)
            .padding(bottom = 8.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            NavItem(
                label = "Feed",
                selected = selectedTab == Routes.FEED,
                onClick = {
                    onTabSelected(Routes.FEED)
                    onNavigateTo(Routes.FEED)
                },
            )
            NavItem(
                label = "Endpoints",
                selected = selectedTab == Routes.ENDPOINTS,
                onClick = {
                    onTabSelected(Routes.ENDPOINTS)
                    onNavigateTo(Routes.ENDPOINTS)
                },
            )
            NavItem(
                label = "Settings",
                selected = selectedTab == Routes.SETTINGS,
                onClick = {
                    onTabSelected(Routes.SETTINGS)
                    onNavigateTo(Routes.SETTINGS)
                },
            )
        }
    }
}

@Composable
private fun NavItem(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val contentColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
    Column(
        modifier = Modifier
            .clickable(onClick = onClick)
            .size(48.dp, 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = contentColor,
        )
    }
}



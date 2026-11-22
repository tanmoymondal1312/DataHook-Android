package com.mediaghor.datahook.ui.navigation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Dns
import androidx.compose.material.icons.outlined.RssFeed
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.mediaghor.datahook.data.remote.dto.Submission
import com.mediaghor.datahook.data.repository.AuthRepository
import com.mediaghor.datahook.ui.auth.AuthScreen
import com.mediaghor.datahook.ui.common.FullScreenLoading
import com.mediaghor.datahook.ui.common.LocalSnackbarController
import com.mediaghor.datahook.ui.common.SnackbarController
import com.mediaghor.datahook.ui.dashboard.CreateEndpointScreen
import com.mediaghor.datahook.ui.dashboard.EndpointsScreen
import com.mediaghor.datahook.ui.endpoint.EndpointDetailScreen
import com.mediaghor.datahook.ui.endpoint.SubmissionDetailScreen
import com.mediaghor.datahook.ui.home.HomeScreen
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

    // Register the FCM token whenever we hold a session. Login alone is not
    // enough: on a cold start `fetchFcmToken()` can come back null while Firebase
    // is still initialising, which left the device unregistered and the user with
    // no push notifications.
    androidx.compose.runtime.LaunchedEffect(loggedIn) {
        if (loggedIn == true) {
            val fcmToken = com.mediaghor.datahook.util.fetchFcmToken()
                ?: return@LaunchedEffect
            authRepository.registerDevice(fcmToken)
        }
    }

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
                    .padding(horizontal = 16.dp),
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
                onOpenEndpoint = { id ->
                    navController.navigate(Routes.endpointDetail(id))
                },
                onOpenSubmission = { submission ->
                    navController.navigate(Routes.submissionDetail(SubmissionCodec.encode(submission)))
                },
                onCreateEndpoint = {
                    navController.navigate(Routes.CREATE_ENDPOINT)
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
 *
 * IMPORTANT: this shell owns its **own** [NavHostController]. A nested `NavHost`
 * must never share a controller with its parent — doing so makes Navigation
 * re-bind the parent's `ViewModelStore` while its back stack is already
 * populated, which crashes with
 * `IllegalStateException: ViewModelStore should be set before setGraph call`.
 */
@Composable
private fun BottomNavHost(
    onOpenEndpoint: (Int) -> Unit,
    onOpenSubmission: (Submission) -> Unit,
    onCreateEndpoint: () -> Unit,
) {
    val tabNavController = rememberNavController()
    val backStackEntry by tabNavController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    fun switchTab(route: String) {
        if (currentRoute == route) return
        tabNavController.navigate(route) {
            popUpTo(tabNavController.graph.startDestinationId) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            BottomNavBar(
                selectedTab = currentRoute,
                onTabSelected = { route -> switchTab(route) },
            )
        },
    ) { padding ->
        NavHost(
            navController = tabNavController,
            startDestination = Routes.FEED,
            modifier = Modifier.padding(bottom = padding.calculateBottomPadding()),
        ) {
            composable(Routes.FEED) {
                HomeScreen(
                    onOpenEndpoints = { switchTab(Routes.ENDPOINTS) },
                    onCreateEndpoint = onCreateEndpoint,
                    onOpenSettings = { switchTab(Routes.SETTINGS) },
                    onOpenSubmission = onOpenSubmission,
                    onOpenEndpoint = onOpenEndpoint,
                )
            }
            composable(Routes.ENDPOINTS) {
                EndpointsScreen(
                    onOpenEndpoint = onOpenEndpoint,
                    onCreateEndpoint = onCreateEndpoint,
                    onBack = { tabNavController.popBackStack() },
                )
            }
            composable(Routes.SETTINGS) {
                SettingsScreen(
                    onBack = { tabNavController.popBackStack() },
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
    selectedTab: String?,
    onTabSelected: (String) -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp)
            .padding(bottom = 8.dp)
            .height(64.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            NavItem(
                label = "Feed",
                icon = Icons.Outlined.RssFeed,
                selected = selectedTab == Routes.FEED,
                onClick = { onTabSelected(Routes.FEED) },
            )
            NavItem(
                label = "Endpoints",
                icon = Icons.Outlined.Dns,
                selected = selectedTab == Routes.ENDPOINTS,
                onClick = { onTabSelected(Routes.ENDPOINTS) },
            )
            NavItem(
                label = "Settings",
                icon = Icons.Outlined.Tune,
                selected = selectedTab == Routes.SETTINGS,
                onClick = { onTabSelected(Routes.SETTINGS) },
            )
        }
    }
}

@Composable
private fun NavItem(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val contentColor = if (selected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Column(
        modifier = Modifier
            .clickable(onClick = onClick)
            .width(84.dp)
            .height(48.dp)
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier = Modifier
                .size(width = 40.dp, height = 24.dp)
                .padding(horizontal = 6.dp),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = contentColor,
                modifier = Modifier.size(20.dp),
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = contentColor,
            maxLines = 1,
        )
    }
}

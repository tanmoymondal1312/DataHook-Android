package com.mediaghor.datahook.ui.dashboard

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.DataObject
import androidx.compose.material.icons.outlined.Inbox
import androidx.compose.material.icons.outlined.Layers
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mediaghor.datahook.R
import com.mediaghor.datahook.data.remote.dto.Endpoint
import com.mediaghor.datahook.di.AppViewModelProvider
import com.mediaghor.datahook.ui.common.EmptyState
import com.mediaghor.datahook.ui.common.ErrorBanner
import com.mediaghor.datahook.ui.common.FullScreenLoading

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EndpointsScreen(
    onOpenEndpoint: (Int) -> Unit,
    onCreateEndpoint: () -> Unit,
    onBack: () -> Unit,
    viewModel: DashboardViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    // Silently refresh when returning to the list (e.g. after create/delete),
    // but skip the very first resume since the ViewModel already loads on init.
    var firstResume by remember { mutableStateOf(true) }
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        if (firstResume) firstResume = false else viewModel.load()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Endpoints") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onCreateEndpoint,
                icon = { Icon(Icons.Outlined.Add, contentDescription = null) },
                text = { Text("New endpoint") },
            )
        },
    ) { padding ->
        PullToRefreshBox(
            isRefreshing = state.refreshing,
            onRefresh = viewModel::refresh,
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
        ) {
            when {
                state.initialLoading -> FullScreenLoading()

                state.endpoints.isEmpty() && state.error != null -> EmptyState(
                    icon = Icons.Outlined.Inbox,
                    title = "Couldn't load endpoints",
                    subtitle = state.error!!,
                    actionLabel = "Retry",
                    onAction = viewModel::load,
                )

                state.endpoints.isEmpty() -> EmptyState(
                    icon = Icons.Outlined.DataObject,
                    title = "Create your first API",
                    subtitle = "Endpoints give you a shareable ingest URL and a secret key. Data POSTed to it shows up right here.",
                    actionLabel = "New endpoint",
                    onAction = onCreateEndpoint,
                )

                else -> LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize(),
                ) {
                    if (state.error != null) {
                        item(key = "error") { ErrorBanner(state.error!!) }
                    }
                    items(state.endpoints, key = { it.id }) { endpoint ->
                        EndpointCard(endpoint = endpoint, onClick = { onOpenEndpoint(endpoint.id) })
                    }
                    item(key = "fab_spacer") { Spacer(Modifier.height(72.dp)) }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EndpointCard(endpoint: Endpoint, onClick: () -> Unit) {
    ElevatedCard(
        onClick = onClick,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.elevatedCardColors(),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(18.dp)) {
            Text(
                text = endpoint.name.ifBlank { "Untitled endpoint" },
                style = MaterialTheme.typography.titleMedium,
            )
            if (!endpoint.description.isNullOrBlank()) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = endpoint.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                )
            }
            Spacer(Modifier.height(14.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                StatChip(
                    icon = Icons.Outlined.Inbox,
                    value = endpoint.submissionCount.toString(),
                    label = if (endpoint.submissionCount == 1) "submission" else "submissions",
                )
                Spacer(Modifier.width(20.dp))
                StatChip(
                    icon = Icons.Outlined.Layers,
                    value = endpoint.attributeCount.toString(),
                    label = if (endpoint.attributeCount == 1) "attribute" else "attributes",
                )
            }
        }
    }
}

@Composable
private fun StatChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    value: String,
    label: String,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint = MaterialTheme.colorScheme.primary,
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
        )
        Spacer(Modifier.width(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

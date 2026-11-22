package com.mediaghor.datahook.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Inbox
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mediaghor.datahook.R
import com.mediaghor.datahook.data.remote.dto.FeedItem
import com.mediaghor.datahook.data.remote.dto.Submission
import com.mediaghor.datahook.data.remote.dto.displayValue
import com.mediaghor.datahook.di.AppViewModelProvider
import com.mediaghor.datahook.ui.common.DataHeaderThumbnail
import com.mediaghor.datahook.ui.common.EmptyState
import com.mediaghor.datahook.ui.common.ErrorBanner
import com.mediaghor.datahook.ui.common.FullScreenLoading
import com.mediaghor.datahook.ui.common.PrimaryButton
import com.mediaghor.datahook.util.formatTimestamp

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onOpenEndpoints: () -> Unit,
    onCreateEndpoint: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenSubmission: (Submission) -> Unit,
    onOpenEndpoint: (Int) -> Unit,
    viewModel: FeedViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var menuOpen by remember { mutableStateOf(false) }
    var searchOpen by remember { mutableStateOf(false) }

    // The empty state carries the create CTA; the FAB is hidden at the same time so
    // the screen never offers two identical actions.
    val showCreateCta = state.items.isEmpty() && state.search.isBlank() &&
        state.error == null && !state.initialLoading

    var firstResume by remember { mutableStateOf(true) }
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        if (firstResume) firstResume = false else viewModel.load()
    }

    val listState = rememberLazyListState()
    val shouldLoadMore by remember {
        derivedStateOf {
            val last = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            val total = listState.layoutInfo.totalItemsCount
            total > 0 && last >= total - 2
        }
    }
    androidx.compose.runtime.LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore) viewModel.loadMore()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.Start) {
                        Text(
                            text = "Activity",
                            style = MaterialTheme.typography.titleLarge,
                        )
                        Text(
                            text = "Your latest incoming data",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                },
                actions = {
                    if (!searchOpen) {
                        IconButton(onClick = { searchOpen = true }) {
                            Icon(Icons.Outlined.Search, contentDescription = "Search")
                        }
                    }
                    IconButton(onClick = { menuOpen = true }) {
                        Icon(Icons.Outlined.MoreVert, contentDescription = "More")
                    }
                    DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                        DropdownMenuItem(
                            text = { Text("Settings") },
                            onClick = { menuOpen = false; onOpenSettings() },
                        )
                        DropdownMenuItem(
                            text = { Text("Sign out") },
                            onClick = { menuOpen = false; viewModel.logout() },
                        )
                    }
                },
            )
        },
        floatingActionButton = {
            // Exactly one create affordance at a time: the empty state carries the
            // "Create Endpoint" CTA when there is nothing to show yet.
            if (!showCreateCta) {
                ExtendedFloatingActionButton(
                    onClick = onCreateEndpoint,
                    containerColor = MaterialTheme.colorScheme.primary,
                    icon = { Icon(Icons.Outlined.Add, contentDescription = null) },
                    text = { Text("New Endpoint") },
                )
            }
        },
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .fillMaxSize(),
        ) {
            // Animated search bar
            if (searchOpen) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                ) {
                    OutlinedTextField(
                        value = state.search,
                        onValueChange = viewModel::onSearchChange,
                        label = { Text("Search submissions...") },
                        singleLine = true,
                        trailingIcon = {
                            if (state.search.isNotEmpty()) {
                                IconButton(onClick = { viewModel.onSearchChange("") }) {
                                    Icon(Icons.Outlined.Close, contentDescription = "Clear search")
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.End) {
                        TextButton(
                            onClick = { searchOpen = false; viewModel.onSearchChange("") },
                        ) {
                            Text("Cancel")
                        }
                    }
                }
            }

            PullToRefreshBox(
                isRefreshing = state.refreshing,
                onRefresh = viewModel::refresh,
                modifier = Modifier.fillMaxSize(),
            ) {
                when {
                    state.initialLoading -> FullScreenLoading()

                    state.items.isEmpty() && state.error != null -> EmptyState(
                        icon = Icons.Outlined.Inbox,
                        title = "Couldn't load your data",
                        subtitle = state.error!!,
                        actionLabel = "Retry",
                        onAction = viewModel::load,
                    )

                    state.items.isEmpty() && state.search.isNotBlank() -> EmptyState(
                        icon = Icons.Outlined.SearchOff,
                        title = "No matches",
                        subtitle = "No submissions match \"${state.search}\".",
                    )

                    state.items.isEmpty() -> EmptyState(
                        icon = Icons.Outlined.Inbox,
                        title = "No submissions yet",
                        subtitle = "Create an endpoint and POST to its ingest URL — everything that comes in shows up here.",
                        actionLabel = "Create Endpoint",
                        onAction = onCreateEndpoint,
                    )

                    else -> LazyColumn(
                        state = listState,
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 96.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        if (state.error != null && state.items.isEmpty()) {
                            item(key = "error") { ErrorBanner(state.error!!) }
                        } else if (state.error != null) {
                            item(key = "error") { ErrorBanner(state.error!!) }
                        }
                        items(state.items, key = { "${it.endpointId}-${it.id}" }) { item ->
                            FeedCard(
                                item = item,
                                onOpen = { onOpenSubmission(item.toSubmission()) },
                                onOpenEndpoint = onOpenEndpoint,
                            )
                        }
                        if (state.loadingMore) {
                            item(key = "loading_more") {
                                Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                                    CircularProgressIndicator(Modifier.size(24.dp), strokeWidth = 2.dp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FeedCard(
    item: FeedItem,
    onOpen: () -> Unit,
    onOpenEndpoint: (Int) -> Unit,
) {
    var menuOpen by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onOpen),
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = item.endpointName.first().uppercase(),
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
                        ),
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
                if (item.headerImage.isNotBlank()) Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        text = item.endpointName.ifBlank { "Endpoint" },
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                    )
                    Text(
                        text = formatTimestamp(item.createdAt),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                IconButton(onClick = { menuOpen = true }) {
                    Icon(Icons.Outlined.MoreVert, contentDescription = "More options", modifier = Modifier.size(20.dp))
                }
                DropdownMenu(
                    expanded = menuOpen,
                    onDismissRequest = { menuOpen = false },
                ) {
                    DropdownMenuItem(
                        text = { Text("View Details") },
                        onClick = { menuOpen = false; onOpenEndpoint(item.endpointId) },
                    )
                    DropdownMenuItem(
                        text = { Text("Delete") },
                        onClick = { menuOpen = false },
                        enabled = false,
                    )
                }
            }
            Spacer(Modifier.height(14.dp))

            DataHeaderThumbnail(item.headerImage, size = 80)
            Spacer(Modifier.height(14.dp))

            if (item.data.isEmpty()) {
                Text(
                    text = "(empty payload)",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                val entries = item.data.entries.toList()
                entries.take(3).forEach { (key, value) ->
                    Row(modifier = Modifier.padding(vertical = 2.dp)) {
                        Text(
                            text = key,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontFamily = FontFamily.Monospace,
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.width(100.dp),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = value.displayValue(),
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
                if (entries.size > 3) {
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "+${entries.size - 3} more fields",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
        }
    }
}

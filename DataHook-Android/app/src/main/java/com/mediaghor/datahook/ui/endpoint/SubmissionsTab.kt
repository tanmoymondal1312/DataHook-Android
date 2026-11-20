package com.mediaghor.datahook.ui.endpoint

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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Inbox
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mediaghor.datahook.data.remote.dto.Submission
import com.mediaghor.datahook.data.remote.dto.displayValue
import com.mediaghor.datahook.ui.common.DataHeaderThumbnail
import com.mediaghor.datahook.ui.common.EmptyState
import com.mediaghor.datahook.ui.common.ErrorBanner
import com.mediaghor.datahook.ui.common.FullScreenLoading
import com.mediaghor.datahook.util.formatTimestamp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubmissionsTab(
    state: EndpointDetailUiState,
    viewModel: EndpointDetailViewModel,
    onCopy: (String) -> Unit,
    onOpenSubmission: (Submission) -> Unit,
) {
    val subs = state.submissions
    val listState = rememberLazyListState()

    val shouldLoadMore by remember {
        derivedStateOf {
            val last = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            val total = listState.layoutInfo.totalItemsCount
            total > 0 && last >= total - 2
        }
    }
    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore) viewModel.loadMoreSubmissions()
    }

    Column(Modifier.fillMaxSize()) {
        OutlinedTextField(
            value = subs.search,
            onValueChange = viewModel::onSearchChange,
            label = { Text("Search submissions") },
            singleLine = true,
            trailingIcon = {
                if (subs.search.isNotEmpty()) {
                    IconButton(onClick = { viewModel.onSearchChange("") }) {
                        Icon(Icons.Outlined.Close, contentDescription = "Clear search")
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        )

        PullToRefreshBox(
            isRefreshing = subs.refreshing,
            onRefresh = viewModel::refreshSubmissions,
            modifier = Modifier.fillMaxSize(),
        ) {
            when {
                subs.loading -> FullScreenLoading()

                subs.items.isEmpty() && subs.error != null -> EmptyState(
                    icon = Icons.Outlined.Inbox,
                    title = "Couldn't load submissions",
                    subtitle = subs.error!!,
                    actionLabel = "Retry",
                    onAction = viewModel::retrySubmissions,
                )

                subs.items.isEmpty() && subs.search.isNotBlank() -> EmptyState(
                    icon = Icons.Outlined.SearchOff,
                    title = "No matches",
                    subtitle = "No submissions match \"${subs.search}\".",
                )

                subs.items.isEmpty() -> EmptyState(
                    icon = Icons.Outlined.Inbox,
                    title = "No submissions yet",
                    subtitle = "POST to your ingest URL and submissions will show up here in real time.",
                )

                else -> LazyColumn(
                    state = listState,
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize(),
                ) {
                    items(subs.items, key = { it.id }) { submission ->
                        SubmissionCard(
                            submission = submission,
                            onCopy = onCopy,
                            onOpen = { onOpenSubmission(submission) },
                            onDelete = { viewModel.deleteSubmission(submission) },
                        )
                    }
                    if (subs.loadingMore) {
                        item(key = "loading_more") {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(16.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                CircularProgressIndicator(Modifier.size(24.dp), strokeWidth = 2.dp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SubmissionCard(
    submission: Submission,
    onCopy: (String) -> Unit,
    onOpen: () -> Unit,
    onDelete: () -> Unit,
) {
    var confirmDelete by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onOpen),
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                DataHeaderThumbnail(submission.headerImage, size = 36)
                if (submission.headerImage.isNotBlank()) Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        text = formatTimestamp(submission.createdAt),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
                IconButton(onClick = { confirmDelete = true }) {
                    Icon(
                        Icons.Outlined.DeleteOutline,
                        contentDescription = "Delete",
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.error,
                    )
                }
            }
            Spacer(Modifier.height(14.dp))

            if (submission.data.isEmpty()) {
                Text(
                    text = "(empty payload)",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                val entries = submission.data.entries.toList()
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

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text("Delete submission?") },
            text = { Text("This permanently removes this submission.") },
            confirmButton = {
                TextButton(onClick = { confirmDelete = false; onDelete() }) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { confirmDelete = false }) { Text("Cancel") }
            },
        )
    }
}

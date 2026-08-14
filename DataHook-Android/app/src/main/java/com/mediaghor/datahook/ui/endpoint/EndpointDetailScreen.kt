package com.mediaghor.datahook.ui.endpoint

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Autorenew
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Inbox
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mediaghor.datahook.data.remote.dto.EndpointDetail
import com.mediaghor.datahook.data.remote.dto.EndpointStats
import com.mediaghor.datahook.di.AppViewModelProvider
import com.mediaghor.datahook.ui.common.CodeBlock
import com.mediaghor.datahook.ui.common.MiniBarChart
import com.mediaghor.datahook.ui.common.CopyableField
import com.mediaghor.datahook.ui.common.EmptyState
import com.mediaghor.datahook.ui.common.FullScreenLoading
import com.mediaghor.datahook.ui.common.LocalSnackbarController
import com.mediaghor.datahook.ui.common.rememberCopyAction
import com.mediaghor.datahook.util.Exporter

private val TAB_TITLES = listOf("Overview", "Attributes", "Snippets", "Submissions")

// Matches Endpoint.notify_title max_length on the backend.
private const val NOTIFY_TITLE_MAX = 100

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EndpointDetailScreen(
    initialTab: Int,
    onBack: () -> Unit,
    onOpenSubmission: (com.mediaghor.datahook.data.remote.dto.Submission) -> Unit,
    viewModel: EndpointDetailViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val copy = rememberCopyAction()
    val snackbar = LocalSnackbarController.current

    var selectedTab by remember { mutableStateOf(initialTab.coerceIn(0, 3)) }
    // Photo picker: read the chosen image's bytes + MIME type, then upload.
    val logoPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia(),
    ) { uri ->
        if (uri != null) {
            val resolver = context.contentResolver
            val mime = resolver.getType(uri) ?: "image/png"
            val bytes = runCatching {
                resolver.openInputStream(uri)?.use { it.readBytes() }
            }.getOrNull()
            if (bytes != null) {
                viewModel.uploadLogo(bytes, mime)
            } else {
                snackbar.show("Couldn't read that image")
            }
        }
    }

    var showRotateDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var exportMenu by remember { mutableStateOf(false) }

    LaunchedEffect(state.message) {
        state.message?.let { snackbar.show(it); viewModel.consumeMessage() }
    }
    LaunchedEffect(state.deleted) {
        if (state.deleted) onBack()
    }
    LaunchedEffect(state.exportPayload) {
        state.exportPayload?.let { payload ->
            val file = Exporter.writeBytes(context, payload.fileName, payload.bytes)
            val intent = Exporter.shareIntent(context, file, payload.mimeType)
            context.startActivity(Intent.createChooser(intent, "Share export"))
            viewModel.consumeExport()
        }
    }
    LaunchedEffect(selectedTab) {
        if (selectedTab == 3) viewModel.ensureSubmissionsLoaded()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = state.detail?.name?.ifBlank { "Endpoint" } ?: "Endpoint",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { exportMenu = true }, enabled = !state.exporting) {
                        if (state.exporting) {
                            CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Outlined.FileDownload, contentDescription = "Export")
                        }
                    }
                    DropdownMenu(expanded = exportMenu, onDismissRequest = { exportMenu = false }) {
                        DropdownMenuItem(
                            text = { Text("Export as CSV") },
                            onClick = { exportMenu = false; viewModel.export("csv") },
                        )
                        DropdownMenuItem(
                            text = { Text("Export as JSON") },
                            onClick = { exportMenu = false; viewModel.export("json") },
                        )
                    }
                },
            )
        },
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .fillMaxSize(),
        ) {
            TabRow(selectedTabIndex = selectedTab) {
                TAB_TITLES.forEachIndexed { index, title ->
                    // Low-level Tab overload: the `text` slot forces 16.dp of
                    // horizontal padding, which leaves the longer labels too
                    // little room and wraps them onto a second line.
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        modifier = Modifier.height(48.dp),
                    ) {
                        Text(
                            text = title,
                            modifier = Modifier.padding(horizontal = 4.dp),
                            style = MaterialTheme.typography.labelMedium,
                            maxLines = 1,
                            softWrap = false,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }

            when {
                state.loading && state.detail == null -> FullScreenLoading()

                state.detail == null && state.error != null -> EmptyState(
                    icon = Icons.Outlined.Inbox,
                    title = "Couldn't load endpoint",
                    subtitle = state.error!!,
                    actionLabel = "Retry",
                    onAction = viewModel::load,
                )

                state.detail != null -> when (selectedTab) {
                    0 -> OverviewTab(
                        detail = state.detail!!,
                        stats = state.stats,
                        notifyUpdating = state.notifyUpdating,
                        notifyTitleSaving = state.notifyTitleSaving,
                        onNotifyTitleSave = viewModel::setNotifyTitle,
                        logoSaving = state.logoSaving,
                        onLogoPick = {
                            logoPicker.launch(
                                PickVisualMediaRequest(
                                    ActivityResultContracts.PickVisualMedia.ImageOnly,
                                ),
                            )
                        },
                        onLogoRemove = viewModel::removeLogo,
                        rotating = state.rotating,
                        onCopy = copy,
                        onNotifyChange = viewModel::setNotify,
                        onRotateClick = { showRotateDialog = true },
                        onDeleteClick = { showDeleteDialog = true },
                    )
                    1 -> AttributesTab(state = state, viewModel = viewModel)
                    2 -> SnippetsTab(detail = state.detail!!, onCopy = copy)
                    3 -> SubmissionsTab(
                        state = state,
                        viewModel = viewModel,
                        onCopy = copy,
                        onOpenSubmission = onOpenSubmission,
                    )
                }
            }
        }
    }

    if (showRotateDialog) {
        AlertDialog(
            onDismissRequest = { showRotateDialog = false },
            title = { Text("Rotate API key?") },
            text = {
                Text("The current key stops working immediately. Update any integrations with the new key afterwards.")
            },
            confirmButton = {
                TextButton(onClick = {
                    showRotateDialog = false
                    viewModel.rotateKey()
                }) { Text("Rotate") }
            },
            dismissButton = {
                TextButton(onClick = { showRotateDialog = false }) { Text("Cancel") }
            },
        )
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete endpoint?") },
            text = { Text("This permanently deletes the endpoint and all of its submissions. This can't be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    viewModel.deleteEndpoint()
                }) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") }
            },
        )
    }
}

@Composable
private fun OverviewTab(
    detail: EndpointDetail,
    stats: EndpointStats?,
    notifyUpdating: Boolean,
    notifyTitleSaving: Boolean,
    onNotifyTitleSave: (String) -> Unit,
    logoSaving: Boolean,
    onLogoPick: () -> Unit,
    onLogoRemove: () -> Unit,
    rotating: Boolean,
    onCopy: (String) -> Unit,
    onNotifyChange: (Boolean) -> Unit,
    onRotateClick: () -> Unit,
    onDeleteClick: () -> Unit,
) {
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
    ) {
        if (stats != null) {
            AnalyticsSection(stats)
            Spacer(Modifier.height(24.dp))
            HorizontalDivider()
            Spacer(Modifier.height(16.dp))
        }

        CopyableField(label = "INGEST URL", value = detail.ingestUrl, onCopy = onCopy)
        Spacer(Modifier.height(16.dp))
        CopyableField(label = "API KEY", value = detail.apiKey, onCopy = onCopy)

        Spacer(Modifier.height(12.dp))
        OutlinedButton(
            onClick = onRotateClick,
            enabled = !rotating,
            shape = RoundedCornerShape(12.dp),
        ) {
            if (rotating) {
                CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
            } else {
                Icon(Icons.Outlined.Autorenew, contentDescription = null, modifier = Modifier.size(18.dp))
            }
            Spacer(Modifier.width(8.dp))
            Text("Rotate API key")
        }

        Spacer(Modifier.height(24.dp))
        HorizontalDivider()
        Spacer(Modifier.height(12.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("Push notifications", style = MaterialTheme.typography.titleMedium)
                Text(
                    "Get a notification for every new submission.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(Modifier.width(12.dp))
            Switch(
                checked = detail.notifyOnSubmit,
                onCheckedChange = onNotifyChange,
                enabled = !notifyUpdating,
            )
        }

        // Only meaningful while notifications are on.
        if (detail.notifyOnSubmit) {
            Spacer(Modifier.height(16.dp))
            NotificationTitleField(
                value = detail.notifyTitle,
                defaultTitle = "New submission · ${detail.name}",
                saving = notifyTitleSaving,
                onSave = onNotifyTitleSave,
            )

            Spacer(Modifier.height(20.dp))
            NotificationLogoField(
                logoUrl = detail.notifyLogoUrl,
                saving = logoSaving,
                onPick = onLogoPick,
                onRemove = onLogoRemove,
            )
        }

        Spacer(Modifier.height(24.dp))
        HorizontalDivider()
        Spacer(Modifier.height(12.dp))

        TextButton(
            onClick = onDeleteClick,
            colors = androidx.compose.material3.ButtonDefaults.textButtonColors(
                contentColor = MaterialTheme.colorScheme.error,
            ),
        ) {
            Icon(Icons.Outlined.DeleteOutline, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("Delete endpoint")
        }
    }
}

@Composable
private fun AnalyticsSection(stats: EndpointStats) {
    Text("Activity", style = MaterialTheme.typography.titleMedium)
    Spacer(Modifier.height(12.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        StatTile(Modifier.weight(1f), stats.total, "Total")
        StatTile(Modifier.weight(1f), stats.today, "Today")
        StatTile(Modifier.weight(1f), stats.last7Days, "7 days")
    }
    if (stats.daily.isNotEmpty()) {
        Spacer(Modifier.height(14.dp))
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(Modifier.padding(16.dp)) {
                Text(
                    "Last ${stats.daily.size} days",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(12.dp))
                MiniBarChart(
                    values = stats.daily.map { it.count },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(88.dp),
                )
            }
        }
    }
}

@Composable
private fun StatTile(modifier: Modifier, value: Int, label: String) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        modifier = modifier,
    ) {
        Column(Modifier.padding(14.dp)) {
            Text(value.toString(), style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(2.dp))
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

/**
 * Notification logo: preview, replace and remove.
 *
 * The logo becomes the notification's *large* icon — Android will not let a
 * server-supplied image be the small status-bar icon, so that stays the app's
 * built-in drawable.
 */
@Composable
private fun NotificationLogoField(
    logoUrl: String,
    saving: Boolean,
    onPick: () -> Unit,
    onRemove: () -> Unit,
) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceContainerHighest),
            contentAlignment = Alignment.Center,
        ) {
            if (logoUrl.isNotBlank()) {
                AsyncImage(
                    model = logoUrl,
                    contentDescription = "Notification logo",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
            } else {
                Icon(
                    Icons.Outlined.Image,
                    contentDescription = null,
                    modifier = Modifier.size(22.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        Spacer(Modifier.width(16.dp))
        Column(Modifier.weight(1f)) {
            Text("Notification logo", style = MaterialTheme.typography.titleMedium)
            Text(
                if (logoUrl.isBlank()) {
                    "Optional. Shown beside the notification text."
                } else {
                    "Shown beside the notification text."
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }

    Spacer(Modifier.height(8.dp))
    Row {
        OutlinedButton(onClick = onPick, enabled = !saving) {
            if (saving) {
                CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.dp)
            } else {
                Text(if (logoUrl.isBlank()) "Upload logo" else "Replace")
            }
        }
        if (logoUrl.isNotBlank()) {
            Spacer(Modifier.width(8.dp))
            TextButton(onClick = onRemove, enabled = !saving) { Text("Remove") }
        }
    }
}

/**
 * Editor for the endpoint's custom notification title. Saving is explicit —
 * the button only appears once the text differs from what the server holds,
 * so there is no per-keystroke PATCH. Clearing the field restores the default.
 */
@Composable
private fun NotificationTitleField(
    value: String,
    defaultTitle: String,
    saving: Boolean,
    onSave: (String) -> Unit,
) {
    // Re-seed the field whenever the server value changes (e.g. after a save).
    var draft by remember(value) { mutableStateOf(value) }
    val dirty = draft.trim() != value.trim()

    Column(Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = draft,
            onValueChange = { if (it.length <= NOTIFY_TITLE_MAX) draft = it },
            label = { Text("Notification title") },
            placeholder = { Text(defaultTitle) },
            singleLine = true,
            enabled = !saving,
            supportingText = {
                Text(
                    if (draft.isBlank()) {
                        "Leave empty to use \"$defaultTitle\""
                    } else {
                        "${draft.length}/$NOTIFY_TITLE_MAX"
                    },
                )
            },
            modifier = Modifier.fillMaxWidth(),
        )

        if (dirty) {
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                TextButton(onClick = { draft = value }, enabled = !saving) {
                    Text("Cancel")
                }
                Spacer(Modifier.width(8.dp))
                OutlinedButton(onClick = { onSave(draft) }, enabled = !saving) {
                    if (saving) {
                        CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.dp)
                    } else {
                        Text("Save title")
                    }
                }
            }
        }
    }
}

@Composable
private fun SnippetsTab(
    detail: EndpointDetail,
    onCopy: (String) -> Unit,
) {
    val snippets = listOf(
        "JS fetch" to detail.snippets.jsFetch,
        "cURL" to detail.snippets.curl,
        "HTML form" to detail.snippets.htmlForm,
    )
    var selected by remember { mutableStateOf(0) }
    val code = snippets[selected].second

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            snippets.forEachIndexed { index, (label, _) ->
                FilterChip(
                    selected = selected == index,
                    onClick = { selected = index },
                    label = { Text(label) },
                )
            }
        }
        Spacer(Modifier.height(16.dp))
        CodeBlock(code = code.ifBlank { "// No snippet available" })
        Spacer(Modifier.height(12.dp))
        OutlinedButton(
            onClick = { onCopy(code) },
            enabled = code.isNotBlank(),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Copy ${snippets[selected].first}")
        }
    }
}

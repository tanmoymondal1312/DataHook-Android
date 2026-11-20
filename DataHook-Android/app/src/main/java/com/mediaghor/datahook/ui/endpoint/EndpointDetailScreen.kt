package com.mediaghor.datahook.ui.endpoint

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
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
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mediaghor.datahook.data.remote.dto.EndpointDetail
import com.mediaghor.datahook.data.remote.dto.EndpointStats
import com.mediaghor.datahook.di.AppViewModelProvider
import com.mediaghor.datahook.ui.common.CodeBlock
import com.mediaghor.datahook.ui.common.CopyableField
import com.mediaghor.datahook.ui.common.DataHeaderThumbnail
import com.mediaghor.datahook.ui.common.DestructiveButton
import com.mediaghor.datahook.ui.common.EmptyState
import com.mediaghor.datahook.ui.common.FullScreenLoading
import com.mediaghor.datahook.ui.common.LocalSnackbarController
import com.mediaghor.datahook.ui.common.PrimaryButton
import com.mediaghor.datahook.ui.common.StatusChip
import com.mediaghor.datahook.ui.common.ChipRole
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
    val copy = com.mediaghor.datahook.ui.common.rememberCopyAction()
    val snackbar = LocalSnackbarController.current

    var selectedTab by remember { mutableStateOf(initialTab.coerceIn(0, 3)) }
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
                    Column(horizontalAlignment = Alignment.Start) {
                        Text(
                            text = state.detail?.name?.ifBlank { "Endpoint" } ?: "Endpoint",
                            style = MaterialTheme.typography.titleLarge,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
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
            PrimaryTabRow(selectedTabIndex = selectedTab) {
                TAB_TITLES.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        modifier = Modifier.height(48.dp),
                    ) {
                        Text(
                            text = title,
                            modifier = Modifier.padding(horizontal = 4.dp),
                            style = MaterialTheme.typography.labelLarge,
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
            .padding(16.dp),
    ) {
        if (stats != null) {
            AnalyticsSection(stats)
            Spacer(Modifier.height(20.dp))
        }

        SectionCard {
            Column {
                Text(
                    text = "Ingest URL",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(Modifier.height(8.dp))
                val ctx = androidx.compose.ui.platform.LocalContext.current
                CopyableField(
                    label = "URL",
                    value = detail.ingestUrl,
                    onCopy = onCopy,
                    onShare = {
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, detail.ingestUrl)
                        }
                        ctx.startActivity(Intent.createChooser(shareIntent, "Share ingest URL"))
                    },
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "POST JSON or form data to this URL",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Spacer(Modifier.height(16.dp))

        SectionCard {
            Column {
                Text(
                    text = "API Key",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(Modifier.height(8.dp))
                CopyableField(
                    label = "Key",
                    value = detail.apiKey,
                    onCopy = onCopy,
                    mask = true,
                )
                Spacer(Modifier.height(8.dp))
                PrimaryButton(
                    text = "Rotate API Key",
                    onClick = onRotateClick,
                    loading = rotating,
                    icon = Icons.Outlined.Autorenew,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Keep this key secret. Anyone with this key can submit data.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Spacer(Modifier.height(20.dp))

        SectionCard {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            text = "Push notifications",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            text = "Receive a notification for every new submission.",
                            style = MaterialTheme.typography.bodySmall,
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
            }
        }

        if (detail.notifyOnSubmit) {
            Spacer(Modifier.height(16.dp))
            SectionCard {
                Column {
                    Text(
                        text = "Notification title",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(8.dp))
                    NotificationTitleField(
                        value = detail.notifyTitle,
                        defaultTitle = "New submission · ${detail.name}",
                        saving = notifyTitleSaving,
                        onSave = onNotifyTitleSave,
                    )
                    Spacer(Modifier.height(16.dp))
                    NotificationLogoField(
                        logoUrl = detail.notifyLogoUrl,
                        saving = logoSaving,
                        onPick = onLogoPick,
                        onRemove = onLogoRemove,
                    )
                }
            }
        }

        Spacer(Modifier.height(20.dp))
        DestructiveButton(
            text = "Delete endpoint",
            onClick = onDeleteClick,
            modifier = Modifier.fillMaxWidth(),
            filled = true,
        )
    }
}

@Composable
private fun SectionCard(content: @Composable () -> Unit) {
    com.mediaghor.datahook.ui.common.DataHookCard(
        modifier = Modifier.fillMaxWidth(),
        content = content,
    )
}

@Composable
private fun AnalyticsSection(stats: EndpointStats) {
    Column {
        Text(
            text = "Activity",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.SpaceBetween) {
            StatCard(
                value = stats.total.toString(),
                label = "Total",
                icon = Icons.Outlined.Inbox,
            )
            StatCard(
                value = stats.today.toString(),
                label = "Today",
                icon = Icons.Outlined.Inbox,
            )
            StatCard(
                value = stats.last7Days.toString(),
                label = "7 days",
                icon = Icons.Outlined.Inbox,
            )
        }
        if (stats.daily.isNotEmpty()) {
            Spacer(Modifier.height(20.dp))
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text(
                        text = "Last ${stats.daily.size} days",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(12.dp))
                    com.mediaghor.datahook.ui.common.MiniBarChart(
                        values = stats.daily.map { it.count },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(88.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun StatCard(
    value: String,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
) {
    com.mediaghor.datahook.ui.common.StatCard(
        value = value,
        label = label,
        icon = icon,
    )
}

/**
 * Notification logo: preview, replace and remove.
 */
@Composable
private fun NotificationLogoField(
    logoUrl: String,
    saving: Boolean,
    onPick: () -> Unit,
    onRemove: () -> Unit,
) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surface),
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
            PrimaryButton(
                text = if (logoUrl.isBlank()) "Upload logo" else "Replace",
                onClick = onPick,
                enabled = !saving,
                loading = saving,
            )
            if (logoUrl.isNotBlank()) {
                Spacer(Modifier.width(8.dp))
                TextActionButton(
                    text = "Remove",
                    onClick = onRemove,
                )
            }
        }
    }
}

/**
 * Editor for the endpoint's custom notification title.
 */
@Composable
private fun NotificationTitleField(
    value: String,
    defaultTitle: String,
    saving: Boolean,
    onSave: (String) -> Unit,
) {
    var draft by remember { mutableStateOf(value) }
    val dirty = draft.trim() != value.trim()

    Column(Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = draft,
            onValueChange = { if (it.length <= NOTIFY_TITLE_MAX) draft = it },
            label = { Text("Title") },
            placeholder = { Text(defaultTitle) },
            singleLine = true,
            enabled = !saving,
            supportingText = {
                if (draft.isBlank()) {
                    Text("Leave empty to use \"$defaultTitle\"")
                } else {
                    Text("${draft.length}/$NOTIFY_TITLE_MAX")
                }
            },
            modifier = Modifier.fillMaxWidth(),
        )

        if (dirty) {
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                TextActionButton(
                    text = "Cancel",
                    onClick = { draft = value },
                )
                Spacer(Modifier.width(8.dp))
                OutlinedButton(
                    onClick = { onSave(draft) },
                    enabled = !saving,
                    shape = RoundedCornerShape(12.dp),
                ) {
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

/** Text action button. */
@Composable
private fun TextActionButton(
    text: String,
    onClick: () -> Unit,
) {
    com.mediaghor.datahook.ui.common.TextActionButton(
        text = text,
        onClick = onClick,
    )
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
            .padding(16.dp),
    ) {
        SectionCard {
            Column {
                Text(
                    text = "Code snippets",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Copy and paste into your site or app to start sending data.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.SpaceBetween) {
                    snippets.forEachIndexed { index, (label, _) ->
                        com.mediaghor.datahook.ui.common.StatusChip(
                            text = label,
                            role = if (selected == index) com.mediaghor.datahook.ui.common.ChipRole.Primary else com.mediaghor.datahook.ui.common.ChipRole.Secondary,
                            modifier = Modifier
                                .clickable { selected = index },
                        )
                    }
                }
                Spacer(Modifier.height(16.dp))
                com.mediaghor.datahook.ui.common.CodeBlock(
                    code = code.ifBlank { "// No snippet available" },
                    onCopy = { onCopy(code) },
                )
                Spacer(Modifier.height(12.dp))
                PrimaryButton(
                    text = "Copy ${snippets[selected].first}",
                    onClick = { onCopy(code) },
                    enabled = code.isNotBlank(),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
        Spacer(Modifier.height(16.dp))
        SectionCard {
            Column {
                Text(
                    text = "Important",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "API key is not included in public HTML forms. Use a secure backend proxy when authentication is required.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

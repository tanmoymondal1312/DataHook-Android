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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Inbox
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.outlined.Title
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.mediaghor.datahook.data.remote.dto.Attribute
import com.mediaghor.datahook.ui.common.EmptyState
import com.mediaghor.datahook.ui.common.ErrorBanner
import com.mediaghor.datahook.ui.common.PrimaryButton
import com.mediaghor.datahook.ui.common.StatusChip
import com.mediaghor.datahook.ui.common.ChipRole
import com.mediaghor.datahook.util.AttributeType
import com.mediaghor.datahook.util.isValidKey
import com.mediaghor.datahook.util.slugify

/** The three display toggles an attribute carries, passed around as one unit. */
data class AttributeFlags(
    val showInNotification: Boolean = false,
    val showAsSubtitle: Boolean = false,
    val showAsDataHeader: Boolean = false,
)

@Composable
fun AttributesTab(
    state: EndpointDetailUiState,
    viewModel: EndpointDetailViewModel,
) {
    var showSheet by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<Attribute?>(null) }

    LaunchedEffect(state.attributeSavedTick) {
        if (state.attributeSavedTick > 0) {
            showSheet = false
            editing = null
        }
    }

    Column(Modifier.fillMaxSize()) {
        if (state.attributes.isEmpty()) {
            EmptyState(
                icon = Icons.Outlined.Inbox,
                title = "No attributes yet",
                subtitle = "Define the typed fields your endpoint should accept, e.g. name, email, phone.",
                actionLabel = "Add attribute",
                onAction = { editing = null; showSheet = true },
                modifier = Modifier.fillMaxSize(),
            )
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize(),
            ) {
                items(state.attributes, key = { it.id }) { attr ->
                    AttributeRow(
                        attribute = attr,
                        onEdit = { editing = attr; showSheet = true },
                        onDelete = { viewModel.deleteAttribute(attr) },
                    )
                }
                item { Spacer(Modifier.height(80.dp)) }
            }
        }

        PrimaryButton(
            text = "Add attribute",
            onClick = { editing = null; showSheet = true },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            icon = Icons.Outlined.Add,
        )
    }

    if (showSheet) {
        AttributeSheet(
            existing = editing,
            saving = state.attributeSaving,
            error = state.attributeError,
            onDismiss = { showSheet = false; editing = null },
            onSave = { label, key, type, required, flags ->
                viewModel.saveAttribute(
                    editing, label, key, type.apiValue, required, flags,
                )
            },
        )
    }
}

@Composable
private fun AttributeRow(
    attribute: Attribute,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    var confirmDelete by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = attribute.label,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        if (attribute.required) {
                            Spacer(Modifier.width(6.dp))
                            StatusChip("Required", role = ChipRole.Error)
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = attribute.key,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontFamily = FontFamily.Monospace,
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(Modifier.width(8.dp))
                        StatusChip(
                            text = AttributeType.fromApi(attribute.type).label,
                            role = ChipRole.Secondary,
                        )
                        if (attribute.showInNotification) {
                            Spacer(Modifier.width(6.dp))
                            Icon(
                                Icons.Outlined.NotificationsActive,
                                contentDescription = "Shown in notifications",
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.primary,
                            )
                        }
                        if (attribute.showAsSubtitle) {
                            Spacer(Modifier.width(6.dp))
                            Icon(
                                Icons.Outlined.Title,
                                contentDescription = "Notification subtitle",
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.primary,
                            )
                        }
                        if (attribute.showAsDataHeader) {
                            Spacer(Modifier.width(6.dp))
                            Icon(
                                Icons.Outlined.Image,
                                contentDescription = "Data header image",
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.primary,
                            )
                        }
                    }
                }
                Row {
                    IconButton(onClick = onEdit) {
                        Icon(
                            Icons.Outlined.Edit,
                            contentDescription = "Edit",
                            modifier = Modifier.size(20.dp),
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
            }
        }
    }

    if (confirmDelete) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text("Delete attribute?") },
            text = { Text("Remove \"${attribute.label}\" from this endpoint?") },
            confirmButton = {
                androidx.compose.material3.TextButton(onClick = {
                    confirmDelete = false
                    onDelete()
                }) { Text("Delete") }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(onClick = { confirmDelete = false }) {
                    Text("Cancel")
                }
            },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AttributeSheet(
    existing: Attribute?,
    saving: Boolean,
    error: String?,
    onDismiss: () -> Unit,
    onSave: (
        label: String,
        key: String,
        type: AttributeType,
        required: Boolean,
        flags: AttributeFlags,
    ) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var label by remember { mutableStateOf(existing?.label ?: "") }
    var key by remember { mutableStateOf(existing?.key ?: "") }
    var keyEdited by remember { mutableStateOf(existing != null) }
    var type by remember { mutableStateOf(AttributeType.fromApi(existing?.type ?: "text")) }
    var required by remember { mutableStateOf(existing?.required ?: false) }
    var showInNotification by remember { mutableStateOf(existing?.showInNotification ?: false) }
    var showAsSubtitle by remember { mutableStateOf(existing?.showAsSubtitle ?: false) }
    var showAsDataHeader by remember { mutableStateOf(existing?.showAsDataHeader ?: false) }
    var typeMenuOpen by remember { mutableStateOf(false) }

    val keyValid = isValidKey(key)
    val canSave = label.isNotBlank() && keyValid && !saving

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(
            Modifier
                .fillMaxWidth()
                .imePadding()
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp),
        ) {
            Text(
                if (existing == null) "Add attribute" else "Edit attribute",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(Modifier.height(20.dp))

            OutlinedTextField(
                value = label,
                onValueChange = {
                    label = it
                    if (!keyEdited) key = slugify(it)
                },
                label = { Text("Field label") },
                placeholder = { Text("e.g. Phone Number") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = key,
                onValueChange = { keyEdited = true; key = it },
                label = { Text("JSON key") },
                singleLine = true,
                isError = key.isNotBlank() && !keyValid,
                supportingText = {
                    if (key.isNotBlank() && !keyValid) {
                        Text("Lowercase letters, numbers, underscores; no spaces.")
                    } else {
                        Text("Sent as the JSON field name.")
                    }
                },
                textStyle = MaterialTheme.typography.bodyLarge.copy(fontFamily = FontFamily.Monospace),
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(16.dp))

            ExposedDropdownMenuBox(
                expanded = typeMenuOpen,
                onExpandedChange = { typeMenuOpen = it },
            ) {
                OutlinedTextField(
                    value = type.label,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Field type") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeMenuOpen) },
                    modifier = Modifier
                        .menuAnchor(androidx.compose.material3.ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                        .fillMaxWidth(),
                )
                ExposedDropdownMenu(
                    expanded = typeMenuOpen,
                    onDismissRequest = { typeMenuOpen = false },
                ) {
                    AttributeType.entries.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option.label) },
                            onClick = { type = option; typeMenuOpen = false },
                        )
                    }
                }
            }
            Spacer(Modifier.height(16.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Required", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                Switch(checked = required, onCheckedChange = { required = it })
            }
            Spacer(Modifier.height(16.dp))

            val isImage = type == AttributeType.IMAGE

            SwitchRow(
                title = "Show in notification",
                subtitle = if (isImage) {
                    "Show this image in the push notification."
                } else {
                    "Include this field's value in the notification text."
                },
                checked = showInNotification,
                onCheckedChange = { showInNotification = it },
            )

            SwitchRow(
                title = "Show as subtitle",
                subtitle = "Use this value as the notification's small header line. Only one field per endpoint can be the subtitle.",
                checked = showAsSubtitle,
                onCheckedChange = { showAsSubtitle = it },
            )

            if (isImage) {
                SwitchRow(
                    title = "Show as data header",
                    subtitle = "Use this image across the app — as the banner on the submission and as the thumbnail in lists.",
                    checked = showAsDataHeader,
                    onCheckedChange = { showAsDataHeader = it },
                )
            }

            if (error != null) {
                Spacer(Modifier.height(16.dp))
                ErrorBanner(error)
            }

            Spacer(Modifier.height(24.dp))
            Button(
                onClick = {
                    onSave(
                        label, key, type, required,
                        AttributeFlags(
                            showInNotification = showInNotification,
                            showAsSubtitle = showAsSubtitle,
                            showAsDataHeader = showAsDataHeader && type == AttributeType.IMAGE,
                        ),
                    )
                },
                enabled = canSave,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
            ) {
                if (saving) {
                    androidx.compose.material3.CircularProgressIndicator(
                        Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                } else {
                    Text(if (existing == null) "Add attribute" else "Save changes")
                }
            }
        }
    }
}

@Composable
private fun SwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Spacer(Modifier.height(4.dp))
    Row(verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Spacer(Modifier.width(12.dp))
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

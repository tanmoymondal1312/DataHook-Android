package com.mediaghor.datahook.ui.endpoint

import android.content.ClipData
import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Inbox
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.mediaghor.datahook.data.remote.dto.Submission
import com.mediaghor.datahook.data.remote.dto.displayValue
import com.mediaghor.datahook.ui.common.CodeBlock
import com.mediaghor.datahook.ui.common.DataHeaderBanner
import com.mediaghor.datahook.ui.common.EmptyState
import com.mediaghor.datahook.ui.common.LinkifiedValue
import com.mediaghor.datahook.util.formatTimestamp
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubmissionDetailScreen(
    submission: Submission?,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val clipboard = LocalClipboard.current
    val copy = com.mediaghor.datahook.ui.common.rememberCopyAction()
    val showRawJsonState = remember(submission) { mutableStateOf(false) }

    val jsonInstance = Json { ignoreUnknownKeys = true }
    val submissionJson = remember(submission) {
        submission?.let { s ->
            jsonInstance.encodeToString(Submission.serializer(), s)
        } ?: ""
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Submission") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { copy(submissionJson) },
                        enabled = submissionJson.isNotBlank(),
                    ) {
                        Icon(
                            Icons.Outlined.ContentCopy,
                            contentDescription = "Copy JSON",
                            modifier = Modifier.size(20.dp),
                        )
                    }
                    IconButton(
                        onClick = {
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, submissionJson)
                            }
                            context.startActivity(Intent.createChooser(intent, "Share submission JSON"))
                        },
                        enabled = submissionJson.isNotBlank(),
                    ) {
                        Icon(
                            Icons.Outlined.Share,
                            contentDescription = "Share JSON",
                            modifier = Modifier.size(20.dp),
                        )
                    }
                },
            )
        },
    ) { padding ->
        if (submission == null) {
            EmptyState(
                icon = Icons.Outlined.Inbox,
                title = "Submission unavailable",
                subtitle = "This submission could not be opened.",
                modifier = Modifier.padding(padding),
            )
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (submission.headerImage.isNotBlank()) {
                item(key = "header") { DataHeaderBanner(submission.headerImage) }
            }

            item(key = "meta") {
                Column {
                    Text(
                        text = formatTimestamp(submission.createdAt),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = submission.createdAt ?: "",
                        style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            if (submission.data.isEmpty()) {
                item(key = "empty") {
                    Text(
                        text = "(empty payload)",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            } else {
                val entries = submission.data.entries.toList()
                item(key = "fields") {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainer,
                        ),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Column(Modifier.padding(vertical = 4.dp)) {
                            entries.forEachIndexed { index, (key, value) ->
                                FieldRow(
                                    label = key,
                                    value = value.displayValue(),
                                    onCopy = { copy(value.displayValue()) },
                                )
                                if (index != entries.lastIndex) {
                                    HorizontalDivider(
                                        modifier = Modifier.padding(horizontal = 16.dp),
                                        color = MaterialTheme.colorScheme.outlineVariant,
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item(key = "raw-json-header") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Raw JSON",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    TextButton(
                        onClick = { showRawJsonState.value = !showRawJsonState.value },
                    ) {
                        Text(if (showRawJsonState.value) "Collapse" else "Expand")
                    }
                }
            }

            if (showRawJsonState.value) {
                item(key = "raw-json") {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                        ),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        CodeBlock(
                            code = submissionJson,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FieldRow(label: String, value: String, onCopy: () -> Unit) {
    Row(
        modifier = Modifier.padding(start = 16.dp, top = 12.dp, bottom = 12.dp, end = 4.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(4.dp))
            LinkifiedValue(value = value, style = MaterialTheme.typography.bodyLarge)
        }
        IconButton(onClick = onCopy) {
            Icon(
                Icons.Outlined.ContentCopy,
                contentDescription = "Copy $label",
                modifier = Modifier.size(18.dp),
            )
        }
    }
}

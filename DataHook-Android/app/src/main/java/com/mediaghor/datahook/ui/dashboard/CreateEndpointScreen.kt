package com.mediaghor.datahook.ui.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mediaghor.datahook.di.AppViewModelProvider
import com.mediaghor.datahook.ui.common.DataHookTextField
import com.mediaghor.datahook.ui.common.ErrorBanner
import com.mediaghor.datahook.ui.common.PrimaryButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateEndpointScreen(
    onBack: () -> Unit,
    onCreated: (Int) -> Unit,
    viewModel: CreateEndpointViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(state.createdId) {
        state.createdId?.let { onCreated(it) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create Endpoint") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(20.dp),
        ) {
            Text(
                text = "Set up a new data collection point.",
                style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(24.dp))

            DataHookTextField(
                value = state.name,
                onValueChange = viewModel::onName,
                label = "Endpoint name",
                placeholder = "e.g. Contact Form",
                isError = state.nameError != null,
                errorText = state.nameError,
                keyboardType = androidx.compose.ui.text.input.KeyboardType.Text,
                imeAction = androidx.compose.ui.text.input.ImeAction.Next,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(16.dp))

            DataHookTextField(
                value = state.description,
                onValueChange = viewModel::onDescription,
                label = "Description",
                placeholder = "What is this endpoint used for?",
                singleLine = false,
                maxLines = 3,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(8.dp))

            Text(
                text = "You can add typed fields after creating the endpoint.",
                style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
            )

            if (state.error != null) {
                Spacer(Modifier.height(16.dp))
                ErrorBanner(state.error!!)
            }

            Spacer(Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                TextButton(onClick = onBack, enabled = !state.loading) {
                    Text("Cancel")
                }
                PrimaryButton(
                    text = "Create Endpoint",
                    onClick = viewModel::create,
                    loading = state.loading,
                )
            }
        }
    }
}

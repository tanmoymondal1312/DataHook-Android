package com.mediaghor.datahook.ui.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mediaghor.datahook.R
import com.mediaghor.datahook.di.AppViewModelProvider
import com.mediaghor.datahook.ui.common.DataHookIconButton
import com.mediaghor.datahook.ui.common.ErrorBanner
import com.mediaghor.datahook.ui.common.PrimaryButton
import com.mediaghor.datahook.ui.common.SecondaryButton

@Composable
fun AuthScreen(
    onAuthenticated: () -> Unit,
    viewModel: AuthViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    if (state.authenticated) {
        androidx.compose.runtime.LaunchedEffect(Unit) { onAuthenticated() }
    }

    val showPasswordState = remember { mutableStateOf(false) }
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
            .navigationBarsPadding(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(top = 48.dp, bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Logo
            Image(
                painter = painterResource(R.drawable.dh_logo),
                contentDescription = "DataHook logo",
                modifier = Modifier.size(80.dp),
            )

            Spacer(Modifier.height(24.dp))

            Text(
                text = "DataHook",
                style = MaterialTheme.typography.headlineSmall,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Manage your endpoints and incoming data.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.height(40.dp))

            // Form — constrained width on large screens
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(min = 280.dp, max = 420.dp),
            ) {
                Text(
                    text = if (state.isRegister) "Create your account" else "Welcome back",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = if (state.isRegister) {
                        "Join DataHook and start collecting data in minutes."
                    } else {
                        "Sign in to access your endpoints and submissions."
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(24.dp))

                if (state.isRegister) {
                    authTextField(
                        value = state.name,
                        onValueChange = viewModel::onName,
                        label = "Full name",
                        placeholder = "John Doe",
                        isError = state.nameError != null,
                        errorText = state.nameError,
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Text,
                        imeAction = androidx.compose.ui.text.input.ImeAction.Next,
                        leadingIcon = Icons.Filled.Person,
                    )
                    Spacer(Modifier.height(16.dp))
                }

                authTextField(
                    value = state.email,
                    onValueChange = viewModel::onEmail,
                    label = "Email address",
                    placeholder = "you@example.com",
                    isError = state.emailError != null,
                    errorText = state.emailError,
                    keyboardType = androidx.compose.ui.text.input.KeyboardType.Email,
                    imeAction = androidx.compose.ui.text.input.ImeAction.Next,
                    leadingIcon = Icons.Filled.Email,
                )
                Spacer(Modifier.height(16.dp))

                authTextField(
                    value = state.password,
                    onValueChange = viewModel::onPassword,
                    label = "Password",
                    placeholder = "••••••••",
                    isError = state.passwordError != null,
                    errorText = state.passwordError,
                    keyboardType = androidx.compose.ui.text.input.KeyboardType.Password,
                    imeAction = androidx.compose.ui.text.input.ImeAction.Done,
                    leadingIcon = Icons.Filled.Lock,
                    trailingIcon = null,
                    isPassword = true,
                )

                if (state.error != null) {
                    Spacer(Modifier.height(16.dp))
                    ErrorBanner(state.error!!)
                }

                Spacer(Modifier.height(24.dp))

                PrimaryButton(
                    text = if (state.isRegister) "Create Account" else "Sign In",
                    onClick = viewModel::submit,
                    loading = state.loading,
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(Modifier.height(20.dp))

                HorizontalDivider(Modifier.fillMaxWidth())
                Spacer(Modifier.height(20.dp))

                SecondaryButton(
                    text = "Continue with Google",
                    onClick = { viewModel.signInWithGoogle(context) },
                    enabled = !state.loading,
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(Modifier.height(16.dp))
                TextButton(onClick = viewModel::toggleMode, enabled = !state.loading) {
                    Text(
                        if (state.isRegister) {
                            "Already have an account? Sign in"
                        } else {
                            "Don't have an account? Sign up"
                        },
                    )
                }
            }
        }
    }
}

/**
 * Auth-styled text field with optional leading icon and password toggle.
 */
@Composable
private fun authTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String? = null,
    isError: Boolean = false,
    errorText: String? = null,
    keyboardType: androidx.compose.ui.text.input.KeyboardType = androidx.compose.ui.text.input.KeyboardType.Text,
    imeAction: androidx.compose.ui.text.input.ImeAction = androidx.compose.ui.text.input.ImeAction.Unspecified,
    leadingIcon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    trailingIcon: (() -> Unit)? = null,
    isPassword: Boolean = false,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val localShowPasswordState = remember { mutableStateOf(false) }
    val localShowPassword = localShowPasswordState.value
    val effectiveTransformation: VisualTransformation = if (isPassword && !localShowPassword) {
        PasswordVisualTransformation()
    } else {
        VisualTransformation.None
    }

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        placeholder = { placeholder?.let { Text(it) } },
        singleLine = true,
        isError = isError,
        supportingText = {
            if (errorText != null) {
                Text(errorText, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error)
            }
        },
        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
            keyboardType = keyboardType,
            imeAction = imeAction,
        ),
        visualTransformation = effectiveTransformation,
        leadingIcon = {
            if (leadingIcon != null) {
                Row(
                    modifier = Modifier.size(40.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = leadingIcon,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
        },
        trailingIcon = {
            if (trailingIcon != null) {
                trailingIcon()
            } else if (isPassword) {
                DataHookIconButton(
                    onClick = { localShowPasswordState.value = !localShowPasswordState.value },
                    contentDescription = if (localShowPasswordState.value) "Hide password" else "Show password",
                    icon = if (localShowPasswordState.value) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                )
            }
        },
        modifier = modifier,
        enabled = enabled,
    )
}

/** Secondary button (e.g. "Continue with Google"). */
@Composable
private fun SecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(
            contentColor = MaterialTheme.colorScheme.primary,
        ),
        shape = com.mediaghor.datahook.ui.common.ShapeCard,
        modifier = modifier.height(50.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(text, style = MaterialTheme.typography.labelLarge)
        }
    }
}

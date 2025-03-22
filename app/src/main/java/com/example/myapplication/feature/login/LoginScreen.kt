package com.example.myapplication.feature.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.myapplication.ui.theme.MyApplicationTheme

@Composable
internal fun LoginScreen(viewModel: LoginViewModel, navigateToHomeScreen: () -> Unit) {
    val state by viewModel.loginState.collectAsState()

    if(state.isValidated){
        navigateToHomeScreen()
    }

    if (state.showErrorDialog) {
        AlertDialog(
            onDismissRequest = {
                viewModel.processIntent(LoginIntent.DismissError)
            },
            title = { Text("Login Error") },
            text = {
                state.errorMessage?.let { Text(it) }
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.processIntent(LoginIntent.DismissError)
                }) {
                    Text("OK")
                }
            }
        )
    }

    LoginScaffold(
        state = state,
        onUsernameChange = { viewModel.processIntent(LoginIntent.UpdateUsername(it)) },
        onPasswordChange = { viewModel.processIntent(LoginIntent.UpdatePassword(it)) },
        onLoginClick = { viewModel.processIntent(LoginIntent.Login) }
    )
}

@Composable
private fun LoginScaffold(
    state: LoginState,
    onUsernameChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onLoginClick: () -> Unit
) {
    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .imePadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.weight(0.5f))
            LoginHeader()
            Spacer(modifier = Modifier.height(24.dp))
            LoginCredentialsFields(
                username = state.username,
                password = state.password,
                isEnabled = !state.isLoading,
                onUsernameChange = onUsernameChange,
                onPasswordChange = onPasswordChange
            )
            Spacer(modifier = Modifier.weight(0.5f))
            LoginButton(
                isLoading = state.isLoading,
                isEnabled = state.isContinueButtonEnabled() && !state.isLoading,
                onClick = onLoginClick
            )
        }
    }
}

@Composable
private fun LoginHeader() {
    Text(
        text = "Login",
        style = MaterialTheme.typography.headlineMedium
    )
}

@Composable
private fun LoginCredentialsFields(
    username: String,
    password: String,
    isEnabled: Boolean,
    onUsernameChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit
) {
    UsernameField(
        username = username,
        isEnabled = isEnabled,
        onUsernameChange = onUsernameChange
    )

    Spacer(modifier = Modifier.height(16.dp))

    PasswordField(
        password = password,
        isEnabled = isEnabled,
        onPasswordChange = onPasswordChange
    )
}

@Composable
private fun UsernameField(
    username: String,
    isEnabled: Boolean,
    onUsernameChange: (String) -> Unit
) {
    OutlinedTextField(
        value = username,
        onValueChange = onUsernameChange,
        label = { Text("Username") },
        modifier = Modifier.fillMaxWidth(),
        keyboardOptions = KeyboardOptions(
            imeAction = ImeAction.Next
        ),
        singleLine = true,
        enabled = isEnabled
    )
}

@Composable
private fun PasswordField(
    password: String,
    isEnabled: Boolean,
    onPasswordChange: (String) -> Unit
) {
    OutlinedTextField(
        value = password,
        onValueChange = onPasswordChange,
        label = { Text("Password") },
        modifier = Modifier.fillMaxWidth(),
        visualTransformation = PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Password,
            imeAction = ImeAction.Done
        ),
        singleLine = true,
        enabled = isEnabled
    )
}

@Composable
private fun LoginButton(
    isLoading: Boolean,
    isEnabled: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        enabled = isEnabled,
        modifier = Modifier.fillMaxWidth()
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = MaterialTheme.colorScheme.onPrimary
            )
        } else {
            Text("Login")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenLoadingPreview() {
    MyApplicationTheme {
        val loadingState = LoginState(
            username = "user@example.com",
            password = "password123"
        )
        LoginScaffold(
            state = loadingState,
            onUsernameChange = {},
            onPasswordChange = {},
            onLoginClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenErrorPreview() {
    MyApplicationTheme {
        val state = LoginState(
            username = "user@example.com",
            password = "password123",
            showErrorDialog = true,
            errorMessage = "Invalid username or password"
        )

        if (state.showErrorDialog) {
            AlertDialog(
                onDismissRequest = {},
                title = { Text("Login Error") },
                text = {
                    state.errorMessage?.let { Text(it) }
                },
                confirmButton = {
                    TextButton(onClick = {}) {
                        Text("OK")
                    }
                }
            )
        }

        LoginScaffold(
            state = state,
            onUsernameChange = {},
            onPasswordChange = {},
            onLoginClick = {}
        )
    }
}
package com.echoon.app.ui.login

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.echoon.app.preferences.AuthRepository
import kotlinx.coroutines.launch

@Composable
fun LoginRoute(
    authRepository: AuthRepository,
    onLoginSuccess: () -> Unit,
) {
    val view = LocalView.current
    val scope = rememberCoroutineScope()
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }
    var isError by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .background(MaterialTheme.colorScheme.surface)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "EchoOn",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary,
        )
        Text(
            text = "Sign in or create an account",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 8.dp),
        )

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = username,
            onValueChange = {
                username = it
                message = null
                isError = false
            },
            label = { Text("Username") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                focusedLabelColor = MaterialTheme.colorScheme.primary,
                unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                cursorColor = MaterialTheme.colorScheme.primary,
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
            ),
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = {
                password = it
                message = null
                isError = false
            },
            label = { Text("Password") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                focusedLabelColor = MaterialTheme.colorScheme.primary,
                unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                cursorColor = MaterialTheme.colorScheme.primary,
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
            )
        )

        if (message != null) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = message!!,
                color = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                view.performHapticFeedback(android.view.HapticFeedbackConstants.CLOCK_TICK)
                message = null
                isError = false
                isLoading = true
                scope.launch {
                    val result = authRepository.signUp(username, password)
                    isLoading = false
                    result.fold(
                        onSuccess = { onLoginSuccess() },
                        onFailure = {
                            message = it.message ?: "Failed"
                            isError = true
                        },
                    )
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(16.dp),
            enabled = !isLoading,
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.height(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                )
            } else {
                Text("Create account")
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        TextButton(
            onClick = {
                view.performHapticFeedback(android.view.HapticFeedbackConstants.CLOCK_TICK)
                message = null
                isError = false
                isLoading = true
                scope.launch {
                    val result = authRepository.login(username, password)
                    isLoading = false
                    result.fold(
                        onSuccess = { onLoginSuccess() },
                        onFailure = {
                            message = it.message ?: "Failed"
                            isError = true
                        },
                    )
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading,
        ) {
            Text(
                text = "Log in",
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

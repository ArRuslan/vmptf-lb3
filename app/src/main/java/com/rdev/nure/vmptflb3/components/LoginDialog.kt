package com.rdev.nure.vmptflb3.components

import android.content.Context.MODE_PRIVATE
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import com.rdev.nure.vmptflb3.api.Client
import com.rdev.nure.vmptflb3.api.requests.LoginRequest
import com.rdev.nure.vmptflb3.api.services.AuthService
import kotlinx.coroutines.launch

@Composable
fun LoginDialog(show: MutableState<Boolean>, loggedIn: MutableState<Boolean>) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val articlesApi = Client.getClient().create(AuthService::class.java)
    var isLoading by remember { mutableStateOf(false) }

    var emailText by remember { mutableStateOf("") }
    var passwordText by remember { mutableStateOf("") }

    fun login() {
        coroutineScope.launch {
            if(isLoading)
                return@launch

            isLoading = true

            val body = articlesApi.login(LoginRequest(emailText, passwordText)).body()
                ?: return@launch

            val prefs = context.getSharedPreferences("auth_info", MODE_PRIVATE)
            prefs.edit().putString("authToken", body.token).putLong("expiresAt", body.expires_at).apply()

            isLoading = false

            loggedIn.value = true
            show.value = false
        }
    }

    if (show.value)
        AlertDialog(
            onDismissRequest = { show.value = false },
            title = {
                Text(text = "Login")
            },
            text = {
                Column {
                    TextField(
                        value = emailText,
                        onValueChange = {emailText = it},
                        label = {
                            Text(text = "Email")
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    )
                    TextField(
                        value = passwordText,
                        onValueChange = {passwordText = it},
                        label = {
                            Text(text = "Password")
                        },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = ::login,
                ) {
                    Text(text = "Login")
                }
            },
            dismissButton = {
                Button(
                    onClick = {
                        show.value = false
                    }
                ) {
                    Text(text = "Cancel")
                }
            },
        )
}
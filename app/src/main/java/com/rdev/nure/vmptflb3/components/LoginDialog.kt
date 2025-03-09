package com.rdev.nure.vmptflb3.components

import android.content.Context.MODE_PRIVATE
import android.util.Log
import android.widget.Toast
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
import com.rdev.nure.vmptflb3.api.entities.Article
import com.rdev.nure.vmptflb3.api.getApiClient
import com.rdev.nure.vmptflb3.api.getErrorResponse
import com.rdev.nure.vmptflb3.api.handleResponse
import com.rdev.nure.vmptflb3.api.requests.CreateArticleRequest
import com.rdev.nure.vmptflb3.api.requests.LoginRequest
import com.rdev.nure.vmptflb3.api.responses.AuthResponse
import com.rdev.nure.vmptflb3.api.responses.PaginationResponse
import com.rdev.nure.vmptflb3.api.services.AuthService
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

private val authApi: AuthService = getApiClient().create(AuthService::class.java)

@Composable
fun LoginDialog(show: MutableState<Boolean>, loggedIn: MutableState<Boolean>) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var isLoading by remember { mutableStateOf(false) }

    var emailText by remember { mutableStateOf("") }
    var passwordText by remember { mutableStateOf("") }

    fun login(networkRetry: Boolean = false) {
        coroutineScope.launch {
            if(isLoading && !networkRetry)
                return@launch
            if(isLoading)
                delay(1000)

            isLoading = true

            val success = handleResponse(
                successResponse = {
                    val prefs = context.getSharedPreferences("auth_info", MODE_PRIVATE)
                    prefs.edit().putString("authToken", it.token).putLong("expiresAt", it.expires_at).putLong("userId", it.user.id).apply()
                },
                errorResponse = {
                    isLoading = false
                    Toast.makeText(context, it.errors[0], Toast.LENGTH_SHORT).show()
                },
                onHttpError = {
                    isLoading = false
                    Toast.makeText(context, "Failed to log in!", Toast.LENGTH_SHORT).show()
                },
                onNetworkError = {
                    Toast.makeText(context, "No internet connection!", Toast.LENGTH_SHORT).show()
                    login(true)
                },
            ) {
                authApi.login(LoginRequest(emailText, passwordText))
            }

            if(!success)
                return@launch

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
                    enabled = !isLoading,
                ) {
                    Text(text = "Login")
                }
            },
            dismissButton = {
                Button(
                    onClick = {
                        show.value = false
                    },
                    enabled = !isLoading,
                ) {
                    Text(text = "Cancel")
                }
            },
        )
}
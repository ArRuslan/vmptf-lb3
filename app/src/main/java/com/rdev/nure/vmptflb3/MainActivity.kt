package com.rdev.nure.vmptflb3

import android.content.Context.MODE_PRIVATE
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.rdev.nure.vmptflb3.components.ArticlesList
import com.rdev.nure.vmptflb3.components.LoginDialog
import com.rdev.nure.vmptflb3.ui.theme.VMPtFLb3Theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContent {
            VMPtFLb3Theme {
                MainActivityComponent()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainActivityComponent() {
    val context = LocalContext.current

    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val searchValue = remember { mutableStateOf("") }
    val showLogin = remember { mutableStateOf(false) }

    val prefs = context.getSharedPreferences("auth_info", MODE_PRIVATE)
    if(
        (prefs.contains("expiresAt") && prefs.getLong("expiresAt", 0) < (System.currentTimeMillis() / 1000))
        || !prefs.contains("authToken") || !prefs.contains("expiresAt")
    )
        prefs.edit().remove("authToken").remove("expiresAt").apply()

    val loggedIn = remember { mutableStateOf(prefs.contains("authToken")) }

    if(showLogin.value)
        LoginDialog(show = showLogin, loggedIn = loggedIn)

    Scaffold(
        topBar = {
            var isSearch by remember { mutableStateOf(false) }

            Crossfade(
                targetState = isSearch,
                label = "Search"
            ) { target ->
                if (!target) {
                    TopAppBar(
                        title = {
                            Text(
                                text = if(searchValue.value.isNotBlank())
                                    "Articles - ${searchValue.value}"
                                else
                                    "Articles"
                            )
                        },
                        actions = {
                            IconButton(onClick = { isSearch = !isSearch }) {
                                Icon(Icons.Filled.Search, contentDescription = "Search")
                            }
                            if(loggedIn.value)
                                IconButton(
                                    onClick = {
                                        prefs.edit().remove("authToken").remove("expiresAt").apply()
                                        loggedIn.value = false
                                    }
                                ) {
                                    Icon(Icons.Outlined.Lock, contentDescription = "Logout")
                                }
                            else
                                IconButton(
                                    onClick = { showLogin.value = true }
                                ) {
                                    Icon(Icons.Filled.AccountCircle, contentDescription = "Login")
                                }
                        },
                        scrollBehavior = scrollBehavior,
                    )
                } else {
                    TextField(
                        modifier = Modifier
                            .fillMaxWidth()
                            .windowInsetsPadding(TopAppBarDefaults.windowInsets),
                        value = searchValue.value,
                        placeholder = { Text("Enter article title") },
                        onValueChange = { searchValue.value = it },
                        leadingIcon = {
                            IconButton(onClick = { isSearch = !isSearch }) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Close"
                                )
                            }
                        },
                        singleLine = true,
                        trailingIcon = if (searchValue.value.isNotBlank()) {
                            {
                                IconButton(onClick = { searchValue.value = "" }) {
                                    Icon(Icons.Filled.Close, contentDescription = "Close")
                                }
                            }
                        } else {
                            null
                        }
                    )
                }
            }
        },
        floatingActionButton = if (loggedIn.value) {
            {
                FloatingActionButton(onClick = { }) {
                    Icon(Icons.Default.Add, contentDescription = "Add")
                }
            }
        } else {
            {}
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier.padding(innerPadding),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            ArticlesList(title = searchValue)
        }
    }
}

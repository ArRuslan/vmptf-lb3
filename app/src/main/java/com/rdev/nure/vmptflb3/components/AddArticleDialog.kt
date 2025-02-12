package com.rdev.nure.vmptflb3.components

import android.content.Context.MODE_PRIVATE
import android.widget.Toast
import androidx.compose.foundation.layout.Column
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
import com.rdev.nure.vmptflb3.api.entities.Article
import com.rdev.nure.vmptflb3.api.entities.Category
import com.rdev.nure.vmptflb3.api.getApiClient
import com.rdev.nure.vmptflb3.api.requests.CreateArticleRequest
import com.rdev.nure.vmptflb3.api.services.ArticleService
import kotlinx.coroutines.launch

private val articlesApi: ArticleService = getApiClient().create(ArticleService::class.java)

@Composable
fun AddArticleDialog(show: MutableState<Boolean>, articles: MutableState<List<Article>>? = null) {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("auth_info", MODE_PRIVATE)
    val coroutineScope = rememberCoroutineScope()

    val authToken = prefs.getString("authToken", "")!!
    var isLoading by remember { mutableStateOf(false) }

    var titleText by remember { mutableStateOf("") }
    var articleText by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<Category?>(null) }

    fun create() {
        coroutineScope.launch {
            if(isLoading)
                return@launch

            isLoading = true

            val body = articlesApi.createArticle(CreateArticleRequest(titleText, articleText, selectedCategory!!.id), "Bearer $authToken").body()
                ?: return@launch

            if(articles != null)
                articles.value = listOf(body) + articles.value;

            isLoading = false
            show.value = false

            Toast.makeText(context, "Article created successfully!", Toast.LENGTH_SHORT).show()
        }
    }

    if (show.value)
        AlertDialog(
            onDismissRequest = { show.value = false },
            title = {
                Text(text = "Create article")
            },
            text = {
                Column {
                    TextField(
                        value = titleText,
                        onValueChange = { titleText = it },
                        label = {
                            Text(text = "Title")
                        },
                        singleLine = true,
                    )
                    AutoCompleteCategoryField(
                        fieldLabel = "Category",
                        onSuggestionSelected = { selectedCategory = it },
                    )
                    TextField(
                        value = articleText,
                        onValueChange = { articleText = it },
                        label = {
                            Text(text = "Text")
                        },
                        singleLine = false,
                    )

                }
            },
            confirmButton = {
                Button(
                    onClick = ::create,
                    enabled = !isLoading,
                ) {
                    Text(text = "Create")
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
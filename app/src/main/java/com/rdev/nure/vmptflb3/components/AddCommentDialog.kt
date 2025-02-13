package com.rdev.nure.vmptflb3.components

import android.content.Context.MODE_PRIVATE
import android.widget.Toast
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
import com.rdev.nure.vmptflb3.api.entities.Comment
import com.rdev.nure.vmptflb3.api.getApiClient
import com.rdev.nure.vmptflb3.api.handleResponse
import com.rdev.nure.vmptflb3.api.requests.CreateArticleRequest
import com.rdev.nure.vmptflb3.api.requests.PostCommentRequest
import com.rdev.nure.vmptflb3.api.services.CommentService
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val commentsApi: CommentService = getApiClient().create(CommentService::class.java)

@Composable
fun AddCommentDialog(articleId: Long, show: MutableState<Boolean>, commentCount: MutableState<Long>? = null, comments: MutableState<List<Comment>>? = null) {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("auth_info", MODE_PRIVATE)
    val coroutineScope = rememberCoroutineScope()

    val authToken = prefs.getString("authToken", "")!!
    var isLoading by remember { mutableStateOf(false) }

    var commentText by remember { mutableStateOf("") }

    fun post(networkRetry: Boolean = false) {
        coroutineScope.launch {
            if(isLoading && !networkRetry)
                return@launch
            if(isLoading)
                delay(1000)

            isLoading = true

            val success = handleResponse(
                successResponse = {
                    if(comments != null)
                        comments.value = listOf(it) + comments.value;
                    if(commentCount != null)
                        commentCount.value += 1;
                },
                errorResponse = {
                    Toast.makeText(context, it.errors[0], Toast.LENGTH_SHORT).show()
                },
                onHttpError = {
                    isLoading = false
                    Toast.makeText(context, "Failed to create article!", Toast.LENGTH_SHORT).show()
                },
                onNetworkError = {
                    Toast.makeText(context, "No internet connection!", Toast.LENGTH_SHORT).show()
                    post(true)
                },
            ) {
                commentsApi.postComment(articleId, PostCommentRequest(commentText), "Bearer $authToken")
            }

            if(!success)
                return@launch

            isLoading = false
            show.value = false
        }
    }

    if (show.value)
        AlertDialog(
            onDismissRequest = { show.value = false },
            title = {
                Text(text = "Post comment")
            },
            text = {
                TextField(
                    value = commentText,
                    onValueChange = { commentText = it },
                    label = {
                        Text(text = "Text")
                    },
                    singleLine = false,
                )
            },
            confirmButton = {
                Button(
                    onClick = ::post,
                    enabled = !isLoading,
                ) {
                    Text(text = "Post")
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
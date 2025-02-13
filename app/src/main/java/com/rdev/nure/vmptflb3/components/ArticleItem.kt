package com.rdev.nure.vmptflb3.components

import android.annotation.SuppressLint
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rdev.nure.vmptflb3.ArticleActivity
import com.rdev.nure.vmptflb3.api.entities.Article
import com.rdev.nure.vmptflb3.api.entities.Category
import com.rdev.nure.vmptflb3.api.entities.User
import com.rdev.nure.vmptflb3.api.getApiClient
import com.rdev.nure.vmptflb3.api.handleResponse
import com.rdev.nure.vmptflb3.api.requests.PostCommentRequest
import com.rdev.nure.vmptflb3.api.services.CommentService
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat

@SuppressLint("SimpleDateFormat")
private val dateFormat = SimpleDateFormat("dd.MM.yyyy 'at' HH:mm")
private val commentApi: CommentService = getApiClient().create(CommentService::class.java)

@Composable
fun ArticleItem(
    article: Article,
    fetchCommentsCount: Boolean = true,
    isInList: Boolean = true,
    publisherState: MutableState<User?>? = null,
    categoryState: MutableState<Category?>? = null,
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var commentsCount by remember { mutableLongStateOf(0) }

    fun loadComments(networkRetry: Boolean = false) {
        if(!fetchCommentsCount) return;

        coroutineScope.launch {
            if(networkRetry)
                delay(1000)

            handleResponse(
                successResponse = { commentsCount = it.count },
                errorResponse = {},
                onHttpError = {},
                onNetworkError = { loadComments(true) },
            ) {
                commentApi.fetchComments(articleId = article.id, page = 1, pageSize = 1)
            }
        }
    }

    LaunchedEffect(Unit) {
        loadComments()
    }

    Column(
        modifier = if (isInList)
            Modifier.padding(4.dp).fillMaxWidth().clickable {
                val intent = Intent(context, ArticleActivity::class.java)
                intent.putExtra("article", article)
                context.startActivity(intent)
            }
        else Modifier.padding(4.dp).fillMaxWidth()
    ) {
        Text(text = article.title, fontSize = 18.sp)
        Row(
            //horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = dateFormat.format(article.created_at * 1000L),
                fontSize = 12.sp,
                color = Color.Gray,
            )
            ClickableText(
                text = AnnotatedString(
                    " by ${article.publisher.name}",
                    spanStyles = listOf(
                        AnnotatedString.Range(
                            SpanStyle(color = Color.Blue, fontWeight = FontWeight.Bold),
                            4,
                            4 + article.publisher.name.length,
                        ),
                    ),
                ),
                style = TextStyle(
                    fontSize = 12.sp,
                    color = Color.Gray,
                ),
                onClick = {
                    if(publisherState == null)
                        return@ClickableText
                    publisherState.value = article.publisher
                }
            )
            ClickableText(
                text = AnnotatedString(
                    " in ${article.category.name}",
                    spanStyles = listOf(
                        AnnotatedString.Range(
                            SpanStyle(color = Color.Blue, fontWeight = FontWeight.Bold),
                            4,
                            4 + article.category.name.length,
                        ),
                    ),
                ),
                style = TextStyle(
                    fontSize = 12.sp,
                    color = Color.Gray,
                ),
                onClick = {
                    if(categoryState == null)
                        return@ClickableText
                    categoryState.value = article.category
                }
            )
            if(commentsCount > 0)
                Text(
                    text = ", $commentsCount comments",
                    fontSize = 12.sp,
                    color = Color.Gray,
                )
        }
        Text(text = article.text, fontSize = 14.sp, color = Color.DarkGray)
    }
}
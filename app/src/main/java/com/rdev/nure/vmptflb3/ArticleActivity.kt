package com.rdev.nure.vmptflb3

import android.app.Activity
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.ContextWrapper
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.rdev.nure.vmptflb3.api.entities.Article
import com.rdev.nure.vmptflb3.api.entities.Comment
import com.rdev.nure.vmptflb3.api.getApiClient
import com.rdev.nure.vmptflb3.api.getErrorResponse
import com.rdev.nure.vmptflb3.api.handleResponse
import com.rdev.nure.vmptflb3.api.requests.PostCommentRequest
import com.rdev.nure.vmptflb3.api.responses.ErrorResponse
import com.rdev.nure.vmptflb3.api.services.ArticleService
import com.rdev.nure.vmptflb3.api.services.CommentService
import com.rdev.nure.vmptflb3.components.AddCommentDialog
import com.rdev.nure.vmptflb3.components.ArticleItem
import com.rdev.nure.vmptflb3.components.EditArticleDialog
import com.rdev.nure.vmptflb3.components.InfiniteScrollLazyColumn
import com.rdev.nure.vmptflb3.ui.theme.VMPtFLb3Theme
import kotlinx.coroutines.launch

class ArticleActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if(!intent.hasExtra("article")) {
            finish()
            return
        }

        val article = intent.getParcelableExtra<Article>("article")
        if(article == null) {
            finish()
            return
        }

        enableEdgeToEdge()
        setContent {
            VMPtFLb3Theme {
                ArticleActivityComponent(article)
            }
        }
    }
}

fun Context.getActivity(): Activity? = when (this) {
    is AppCompatActivity -> this
    is ComponentActivity -> this
    is ContextWrapper -> baseContext.getActivity()
    else -> null
}

private val articlesApi: ArticleService = getApiClient().create(ArticleService::class.java)
private val commentsApi: CommentService = getApiClient().create(CommentService::class.java)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArticleActivityComponent(article: Article) {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("auth_info", MODE_PRIVATE)
    if(
        (prefs.contains("expiresAt") && prefs.getLong("expiresAt", 0) < (System.currentTimeMillis() / 1000))
        || !prefs.contains("authToken") || !prefs.contains("expiresAt") || !prefs.contains("userId")
    )
        prefs.edit().remove("authToken").remove("expiresAt").remove("userId").apply()

    val loggedIn = remember { mutableStateOf(prefs.contains("authToken")) }
    val authToken = prefs.getString("authToken", "")!!
    val userId = remember { mutableLongStateOf(prefs.getLong("userId", 0)) }
    val showPostComment = remember { mutableStateOf(false) }
    val showEditArticle = remember { mutableStateOf(false) }
    val articleMut = remember { mutableStateOf(article) }

    var hasMore by remember { mutableStateOf(true) }
    var page by remember { mutableIntStateOf(1) }
    val totalCommentsCount = remember { mutableLongStateOf(0) }
    val comments = remember { mutableStateOf(listOf<Comment>()) }
    val commentsState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }

    fun loadMoreComments() {
        coroutineScope.launch {
            if(!hasMore || isLoading) {
                return@launch
            }

            isLoading = true

            val body = commentsApi.fetchComments(articleId = article.id, pageSize = 1, page = page).body()
                ?: return@launch

            if(body.result.isEmpty()) {
                hasMore = false
                isLoading = false
                return@launch
            }

            totalCommentsCount.longValue = body.count;
            comments.value += body.result;
            page++

            isLoading = false
        }
    }

    if(showPostComment.value)
        AddCommentDialog(
            articleId = article.id,
            show = showPostComment,
            commentCount = totalCommentsCount,
            comments = comments,
        )
    if(showEditArticle.value)
        EditArticleDialog(
            show = showEditArticle,
            article = articleMut,
        )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row (
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        IconButton(onClick = { context.getActivity()!!.finish() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                        Text(text = articleMut.value.title)
                    }
                },
                scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(),
                actions = {
                    if(article.publisher.id == userId.longValue) {
                        IconButton(onClick = {
                            showEditArticle.value = true
                        }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit")
                        }
                        IconButton(onClick = {
                            coroutineScope.launch {
                                val resp = articlesApi.deleteArticle(article.id, "Bearer $authToken")
                                if(resp.code() == 204) {
                                    context.getActivity()!!.finish()
                                    return@launch
                                }

                                if(resp.code() < 400) {
                                    Toast.makeText(context, "Unknown error", Toast.LENGTH_SHORT).show()
                                    return@launch
                                }

                                resp.getErrorResponse()?.let {
                                    if(it.errors.isEmpty())
                                        Toast.makeText(context, "Unknown error", Toast.LENGTH_SHORT).show()
                                    else
                                        Toast.makeText(context, it.errors[0], Toast.LENGTH_SHORT).show()
                                } ?: run {
                                    Toast.makeText(context, "Unknown error", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete")
                        }
                    }
                }
            )
        },
        floatingActionButton = if (loggedIn.value) {
            {
                FloatingActionButton(onClick = { showPostComment.value = true }) {
                    Icon(Icons.Default.Add, contentDescription = "Add")
                }
            }
        } else {
            {}
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxWidth()
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            ArticleItem(article = articleMut.value, fetchCommentsCount = false, isInList = false)
            Text(
                text = "${totalCommentsCount.longValue} comments"
            )
            InfiniteScrollLazyColumn(
                items = comments.value,
                loadMoreItems = ::loadMoreComments,
                listState = commentsState,
                isLoading = isLoading,
                modifier = Modifier
                    .padding(4.dp)
                    .fillMaxWidth(),
            )
        }
    }
}
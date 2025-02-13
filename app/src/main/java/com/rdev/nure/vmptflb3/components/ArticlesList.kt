package com.rdev.nure.vmptflb3.components

import android.widget.Toast
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.rdev.nure.vmptflb3.api.entities.Article
import com.rdev.nure.vmptflb3.api.entities.Category
import com.rdev.nure.vmptflb3.api.entities.User
import com.rdev.nure.vmptflb3.api.getApiClient
import com.rdev.nure.vmptflb3.api.handleResponse
import com.rdev.nure.vmptflb3.api.services.ArticleService
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


private val articlesApi: ArticleService = getApiClient().create(ArticleService::class.java)

@Composable
fun ArticlesList(title: State<String>, publisher: MutableState<User?>, category: MutableState<Category?>) {
    val context = LocalContext.current
    var hasMore by remember { mutableStateOf(true) }
    var page by remember { mutableIntStateOf(1) }

    var articles by remember { mutableStateOf(listOf<Article>()) }
    val articlesState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }
    var loadingJob by remember { mutableStateOf<Job?>(null) }

    fun loadMoreItems(reset: Boolean = false, networkRetry: Boolean = false) {
        loadingJob?.cancel()
        loadingJob = coroutineScope.launch {
            if(!hasMore && !reset)
                return@launch
            if(networkRetry)
                delay(1000)

            if(reset) {
                page = 1
                hasMore = true
            }

            isLoading = true

            val success = handleResponse(
                successResponse = {
                    if(it.result.isEmpty()) {
                        hasMore = false
                        isLoading = false
                        return@handleResponse
                    }

                    articles = if(reset) it.result else articles + it.result;
                    page++
                },
                errorResponse = {
                    Toast.makeText(context, it.errors[0], Toast.LENGTH_SHORT).show()
                },
                onHttpError = {
                    isLoading = false
                    Toast.makeText(context, "Failed to fetch articles!", Toast.LENGTH_SHORT).show()
                },
                onNetworkError = {
                    Toast.makeText(context, "No internet connection!", Toast.LENGTH_SHORT).show()
                    loadMoreItems(reset, true)
                },
            ) {
                articlesApi.fetchArticles(
                    title = title.value,
                    pageSize = 1,
                    page = page,
                    publisherId = publisher.value?.id,
                    categoryId = category.value?.id,
                )
            }

            if(!success)
                return@launch

            isLoading = false
        }
    }

    LaunchedEffect(title.value, publisher.value, category.value) {
        loadMoreItems(true)
        articlesState.scrollToItem(0)
    }

    InfiniteScrollLazyColumn(
        items = articles,
        loadMoreItems = ::loadMoreItems,
        listState = articlesState,
        isLoading = isLoading,
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth(),

        articlesPublisherState = publisher,
        articlesCategoryState = category,
    )
}
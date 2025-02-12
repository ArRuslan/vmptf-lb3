package com.rdev.nure.vmptflb3.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.rdev.nure.vmptflb3.api.getApiClient
import com.rdev.nure.vmptflb3.api.entities.Article
import com.rdev.nure.vmptflb3.api.services.ArticleService
import kotlinx.coroutines.launch


val articlesApi: ArticleService = getApiClient().create(ArticleService::class.java)

@Composable
fun ArticlesList(title: State<String>) {
    var hasMore by remember { mutableStateOf(true) }
    var page by remember { mutableIntStateOf(1) }

    var articles by remember { mutableStateOf(listOf<Article>()) }
    val articlesState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }

    fun loadMoreItems(reset: Boolean = false) {
        coroutineScope.launch {
            if((!hasMore && !reset) || isLoading) {
                return@launch
            }

            if(reset) {
                page = 1
                hasMore = true
            }

            isLoading = true

            val body = articlesApi.fetchArticles(title = title.value, pageSize = 1, page = page).body()
                ?: return@launch

            if(body.count == 0L) {
                hasMore = false
                isLoading = false
                return@launch
            }

            articles = if(reset) body.result else articles + body.result;
            page++

            isLoading = false
        }
    }

    LaunchedEffect(title.value) {
        loadMoreItems(true)
        articlesState.scrollToItem(0)
    }

    ArticlesInfiniteScrollLazyColumn(
        items = articles,
        loadMoreItems = ::loadMoreItems,
        listState = articlesState,
        isLoading = isLoading,
        modifier = Modifier.padding(8.dp),
    )
}
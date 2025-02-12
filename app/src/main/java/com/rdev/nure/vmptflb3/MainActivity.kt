package com.rdev.nure.vmptflb3

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.layout
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.rdev.nure.vmptflb3.ui.theme.VMPtFLb3Theme
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.jetbrains.annotations.Nullable
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.text.SimpleDateFormat
import kotlin.math.roundToInt

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContent {
            VMPtFLb3Theme {
                AppScaffold()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppScaffold() {
    var presses by remember { mutableIntStateOf(0) }
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val searchValue = remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            var isSearch by remember { mutableStateOf(false) }

            Crossfade(
                targetState = isSearch,
                label = "Search"
            ) { target ->
                if (!target) {
                    TopAppBar(
                        title = { Text("Articles") },
                        actions = {
                            IconButton(onClick = { isSearch = !isSearch }) {
                                Icon(Icons.Filled.Search, contentDescription = "Search")
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
        floatingActionButton = {
            FloatingActionButton(onClick = { presses++ }) {
                Icon(Icons.Default.Add, contentDescription = "Add")
            }
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

@Composable
fun ArticlesList(title: State<String>) {
    val client = Retrofit.Builder().baseUrl("http://192.168.0.111:3000").addConverterFactory(GsonConverterFactory.create()).build();
    val articlesApi = client.create(ApiService::class.java)

    var hasMore by remember { mutableStateOf(true) }
    var page by remember { mutableIntStateOf(1) }

    var articles by remember { mutableStateOf(listOf<Article>()) }
    val articlesState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }

    fun loadMoreItems(reset: Boolean = false) {
        coroutineScope.launch {
            if(!hasMore || isLoading) {
                return@launch
            }

            if(reset) {
                page = 1
                hasMore = true
            }

            isLoading = true

            val body = articlesApi.fetchArticles(title = title.value, page_size = 1, page = page).body()
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

    PaginatedLazyColumn(
        items = articles,
        loadMoreItems = ::loadMoreItems,
        listState = articlesState,
        isLoading = isLoading,
        modifier = Modifier.padding(8.dp),
    )
}

@Composable
fun PaginatedLazyColumn(
    items: List<Article>,
    loadMoreItems: () -> Unit,
    listState: LazyListState,
    buffer: Int = 2,
    isLoading: Boolean,
    modifier: Modifier = Modifier
) {
    val dateFormat = SimpleDateFormat("dd.MM.yyyy 'at' HH:mm")
    val shouldLoadMore = remember {
        derivedStateOf {
            val totalItemsCount = listState.layoutInfo.totalItemsCount
            val lastVisibleItemIndex = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0

            Log.i("why", "($lastVisibleItemIndex >= ($totalItemsCount - $buffer) && !$isLoading) = ${lastVisibleItemIndex >= (totalItemsCount - buffer) && !isLoading}")

            lastVisibleItemIndex >= (totalItemsCount - buffer) && !isLoading
        }
    }

    LaunchedEffect(items) {
        snapshotFlow { shouldLoadMore.value }
            .distinctUntilChanged()
            .filter { it }
            .collect {
                loadMoreItems()
            }
    }
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        state = listState
    ) {
        itemsIndexed(
            items,
            contentType = { _, _ -> Article::class },
            key = { _, article -> article.hashCode() },
        ) { _, article ->
            Column {
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
                        onClick = {}
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
                        onClick = {}
                    )
                }
                Text(text = article.text, fontSize = 14.sp, color = Color.DarkGray)
            }
            HorizontalDivider(modifier = Modifier.padding(8.dp))
        }

        if (isLoading) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}

interface ApiService {
    @GET("/articles/search")
    suspend fun fetchArticles(
        @Query("title") title: String? = null,
        @Query("page") page: Int? = null,
        @Query("page_size") page_size: Int? = null,
    ): Response<PaginationResponse<Article>>
}

interface ApiDataSource {
    suspend fun fetchArticles(
        title: String? = null,
        page: Int? = null,
        page_size: Int? = null,
    ): PaginationResponse<Article>
}

data class PaginationResponse<T>(
    val count: Long,
    val result: List<T>,
)

data class Article(
    val id: Long,
    val title: String,
    val text: String,
    val created_at: Long,
    val publisher: User,
    val category: Category,
)

data class User(
    val id: Long,
    val name: String
)

data class Category(
    val id: Long,
    val name: String
)
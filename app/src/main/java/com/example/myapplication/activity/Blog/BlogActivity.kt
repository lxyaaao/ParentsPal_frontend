package com.example.myapplication.activity.Blog

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Recommend
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.myapplication.R
import com.example.myapplication.activity.Main.BottomNavigationBar
import com.example.myapplication.activity.Main.HomeScreen
import com.example.myapplication.activity.Main.NavItem
import com.example.myapplication.activity.Main.PersonScreen
import com.example.myapplication.activity.Main.QuestionAnswerScreen
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.example.myapplication.utils.NetworkUtils.sendGetRequest
import com.google.gson.Gson

class BlogActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                BlogScreen(this)
            }
        }
    }
}

@Composable
private fun BlogScreen(activity: Activity) {
    val navController = rememberNavController()
    var selectedTabIndex by remember { mutableStateOf(0) }

    var isMenuExpanded by remember { mutableStateOf(false) }

    Scaffold(
            topBar = { CenterAlignedTopAppBarExample(activity, isMenuExpanded) {
                newState -> isMenuExpanded = newState
            } },
            bottomBar = { BottomNavigationBar(navController) }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = NavItem.Share.route,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(NavItem.Home.route) { HomeScreen(activity) }
            composable(NavItem.Share.route) { BlogMainScreen(activity) }
            composable(NavItem.QuestionAnswer.route) { QuestionAnswerScreen(activity) }
            composable(NavItem.Person.route) { PersonScreen(activity) }
        }
        Scaffold(
            topBar = {
                TabRow(
                    selectedTabIndex = selectedTabIndex,
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ) {
                    val tabTitles = listOf("分享", "问答")
                    tabTitles.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTabIndex == index,
                            onClick = {
                                selectedTabIndex = index
                            },
                            text = { Text(title) }
                        )
                    }
                }
            },
            modifier = Modifier
                .padding(paddingValues)
                .pointerInput(Unit) {
                    detectTapGestures(onTap = {
                        isMenuExpanded = false
                    })
                }
        ) {
            paddingValuesIn ->
            Column(modifier = Modifier.padding(paddingValuesIn)) {
                when (selectedTabIndex) {
                    0 -> TabContent1(activity)
                    1 -> TabContent2()
                }
            }
            Row(modifier = Modifier.padding(paddingValuesIn)) {
                SideRail(activity, isMenuExpanded)
            }
        }
    }
}


@Composable
fun TabContent1(activity: Activity) {
    val sharedPreferences: SharedPreferences =
        activity.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)

    var articles: List<Article> by remember { mutableStateOf(emptyList()) }
    val selectedItem = sharedPreferences.getInt("selectedItem", 0)
    val parentId = sharedPreferences.getInt("parentId", 0)

    FetchLikedArticles(activity)
    FetchLikedComments(activity)

    var apiString = ""

    if (selectedItem == 0) {
        apiString = "api/article/hot"
    }
    if (selectedItem == 1) {
        apiString = "api/saved-article/$parentId"
    }

    LaunchedEffect(Unit) {
        val response = sendGetRequest(apiString)
        try {
            val gson = Gson()
            val apiResponse = gson.fromJson(response, GetArticleResponse::class.java)
            articles = if (!apiResponse.success) {
                emptyList()
            } else {
                apiResponse.data
            }
        } catch (e: Exception) {
            println("Json error: $response")
        }
    }

    if(articles.isNotEmpty()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            items(articles) { blogContent ->
                BlogContentCard(blogContent, onClick = {
                    val editor = sharedPreferences.edit()
                    editor.putInt("articleId", blogContent.articleId)
                    editor.putBoolean("refreshCommentState", false)
                    editor.putString("blogPage", "blog")
                    editor.apply()

                    val intent = Intent(activity, MyBlogCheckActivity::class.java)
                    activity.startActivity(intent)
                    activity.finish()
                })
            }
        }
    }
}

@Composable
fun TabContent2() {

}

private fun BlogMainScreen(activity: Activity) {

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CenterAlignedTopAppBarExample(
    activity: Activity, isMenuExpanded: Boolean, onMenuStateChange: (Boolean) -> Unit
) {
    CenterAlignedTopAppBar(
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            titleContentColor = MaterialTheme.colorScheme.primary,
        ),
        title = {
            Text(
                "Blog",
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        navigationIcon = {
                    IconButton(onClick = { onMenuStateChange(!isMenuExpanded) }) {
                        Icon(
                            imageVector = Icons.Filled.Menu,
                            contentDescription = "Localized description"
                        )
                    }
                },
        actions = {
                    IconButton(onClick = {
                        val intent = Intent(activity, SearchActivity::class.java)
                        activity.startActivity(intent)
                        activity.finish()
                    }) {
                        Icon(
                            imageVector = Icons.Filled.Search,
                            contentDescription = "Localized description"
                            )
                        }
                    },
    )
}

@Composable
fun SideRail(activity: Activity, isMenuExpanded: Boolean) {
    val sharedPreferences: SharedPreferences =
        activity.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)

    var selectedItem by remember { mutableStateOf(sharedPreferences.getInt("selectedItem", 0)) }

    if (isMenuExpanded) {
        NavigationRail(
            containerColor = Color.White
        ) {
            NavigationRailItem(
                icon = { Icon(Icons.Filled.Recommend, contentDescription = "推荐") },
                label = { Text("推荐") },
                selected = selectedItem == 0,
                onClick = {
                    selectedItem = 0

                    val editor = sharedPreferences.edit()
                    editor.putInt("selectedItem", selectedItem)
                    editor.apply()

                    val intent = Intent(activity, BlogActivity::class.java)
                    activity.startActivity(intent)
                    activity.finish()
                }
            )
            Spacer(modifier = Modifier.height(8.dp))

            NavigationRailItem(
                icon = { Icon(Icons.Filled.Bookmark, contentDescription = "收藏") },
                label = { Text("收藏") },
                selected = selectedItem == 1,
                onClick = {
                    selectedItem = 1

                    val editor = sharedPreferences.edit()
                    editor.putInt("selectedItem", selectedItem)
                    editor.apply()

                    val intent = Intent(activity, BlogActivity::class.java)
                    activity.startActivity(intent)
                    activity.finish()
                }
            )
            Spacer(modifier = Modifier.height(8.dp))

            NavigationRailItem(
                icon = { Icon(Icons.Filled.Person, contentDescription = "我的") },
                label = { Text("我的") },
                selected = selectedItem == 2,
                onClick = {
                    selectedItem = 2

                    val intent = Intent(activity, BlogMineActivity::class.java)
                    activity.startActivity(intent)
                    activity.finish()
                }
            )
        }
    }
}

@Composable
fun BlogContentCard(article: Article, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .background(Color.White, shape = RoundedCornerShape(8.dp))
            .border(1.dp, Color.LightGray, shape = RoundedCornerShape(8.dp))
            .clickable { onClick() }
    ) {
        Column (modifier = Modifier.padding(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 4.dp)) {
            // 头像和昵称
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(id = R.drawable.baseline_account_circle_24),
                    contentDescription = "Avatar",
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .padding(4.dp),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = article.username)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(text = article.title)

            Spacer(modifier = Modifier.height(8.dp))

            Text(text = article.content, color = Color.Gray)

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = article.time.substring(0, 10),
                    modifier = Modifier.weight(1f),
                    fontSize = 12.sp,
                    textAlign = TextAlign.Start
                )

                Text(
                    fontSize = 12.sp,
                    text = "Likes: ${article.likes}",
                    textAlign = TextAlign.End
                )
            }
        }
    }
}

@Composable
fun FetchLikedArticles(activity: Activity) {
    val sharedPreferences: SharedPreferences =
        activity.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)

    val parentId = sharedPreferences.getInt("parentId", 0)

    var likedArticles: List<Article> by remember { mutableStateOf(emptyList()) }
    var savedArticles: List<Article> by remember { mutableStateOf(emptyList()) }
    var likedArticleIds: HashSet<Int> by remember { mutableStateOf(hashSetOf()) }
    var savedArticleIds: HashSet<Int> by remember { mutableStateOf(hashSetOf()) }

    val apiLike = "api/liked-article/$parentId"
    val apiSave = "api/saved-article/$parentId"
    LaunchedEffect(Unit) {
        val responseLike = sendGetRequest(apiLike)
        try {
            val gson = Gson()
            val apiResponse = gson.fromJson(responseLike, GetArticleResponse::class.java)
            println(apiResponse)
            likedArticles = if (!apiResponse.success) {
                emptyList()
            } else {
                apiResponse.data
            }
            likedArticleIds = likedArticles.map { it.articleId }.toHashSet()
        } catch (e: Exception) {
            println("Json error: $responseLike")
        }

        val responseSave = sendGetRequest(apiSave)
        try {
            val gson = Gson()
            val apiResponse = gson.fromJson(responseSave, GetArticleResponse::class.java)
            savedArticles = if (!apiResponse.success) {
                emptyList()
            } else {
                apiResponse.data
            }
            savedArticleIds = savedArticles.map { it.articleId }.toHashSet()
        } catch (e: Exception) {
            println("Json error: $responseSave")
        }
    }

    val editor = sharedPreferences.edit()
    editor.putStringSet("likedArticleIds", likedArticleIds.map { it.toString() }.toSet())
    editor.putStringSet("savedArticleIds", savedArticleIds.map { it.toString() }.toSet())
    editor.apply()
}

@Composable
fun FetchLikedComments(activity: Activity,) {
    val sharedPreferences: SharedPreferences =
        activity.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)

    val parentId = sharedPreferences.getInt("parentId", 0)

    var likedComments: List<Comment> by remember { mutableStateOf(emptyList()) }
    var likedCommentIds: HashSet<Int> by remember { mutableStateOf(hashSetOf()) }

    val apiLike = "api/liked-comment?userId=$parentId"
    LaunchedEffect(Unit) {
        val responseLike = sendGetRequest(apiLike)
        try {
            val gson = Gson()
            val apiResponse = gson.fromJson(responseLike, CommentResponse::class.java)
            println(apiResponse)
            likedComments = if (!apiResponse.success) {
                emptyList()
            } else {
                apiResponse.data
            }
            likedCommentIds = likedComments.map { it.commentId }.toHashSet()
        } catch (e: Exception) {
            println("Json error: $responseLike")
        }
    }

    val editor = sharedPreferences.edit()
    editor.putStringSet("likedCommentIds", likedCommentIds.map { it.toString() }.toSet())
    editor.apply()
}

data class GetArticleResponse(
    val data: List<Article>,
    val success: Boolean,
    val errorMsg: String?
)

data class Article(
    val articleId: Int,
    val userId: Int,
    val username: String,
    val title: String,
    val content: String,
    var likes: Int,
    val saves: Int,
    val time: String
)
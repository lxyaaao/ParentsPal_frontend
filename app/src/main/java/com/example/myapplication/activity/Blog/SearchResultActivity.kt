package com.example.myapplication.activity.Blog

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.myapplication.activity.Main.RegisterResponse
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.example.myapplication.utils.NetworkUtils.sendGetRequest
import com.example.myapplication.utils.NetworkUtils.sendPostRequestWithRequest
import com.google.gson.Gson
import org.json.JSONObject

class SearchResultActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                SearchResultScreen(this)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchResultScreen(activity: Activity) {
    val sharedPreferences: SharedPreferences =
        activity.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)
    val editor = sharedPreferences.edit()

    var backFlag by remember { mutableStateOf(false) }

    val title by remember { mutableStateOf(sharedPreferences.getString("searchResult", " ") ?: " ") }
    var selectedTabIndex by remember { mutableIntStateOf(sharedPreferences.getInt("selectedTabIndex", 0)) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                ),
                title = {
                    Text(
                        title,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { backFlag = true }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Localized description"
                        )
                    }
                }
            )
        },
        content = { paddingValues ->
            Column(modifier = Modifier.padding(paddingValues)) {
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

                when (selectedTabIndex) {
                    0 -> TabResultContent(title, activity, 0)
                    1 -> TabResultContent(title, activity, 1)
                }
            }
        }
    )

    if (backFlag) {
        editor.putString("searchResult", "")
        editor.apply()
        val intent = Intent(activity, BlogActivity::class.java)
        activity.startActivity(intent)
        activity.finish()
    }

}

@Composable
fun TabResultContent(title: String, activity: Activity, number: Int) {
    FetchLikedArticles(activity, number)
    FetchLikedComments(activity, number)

    var articles: List<Article> by remember { mutableStateOf(emptyList()) }

    val urlPart = if (number == 0) {
        "article"
    } else {
        "qna"
    }

    val apiPath = "api/$urlPart/search?queryKeyword=$title"
    LaunchedEffect(Unit) {
        val response = sendGetRequest(apiPath)
        println("api=$apiPath, response=$response")
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
                BlogContentCard(activity, blogContent, onClick = {
                    val sharedPreferences: SharedPreferences =
                        activity.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)
                    val editor = sharedPreferences.edit()
                    if (number == 0) {
                        editor.putInt("articleId", blogContent.articleId)
                    } else {
                        editor.putInt("articleId", blogContent.qnaId)
                    }
                    editor.putBoolean("refreshCommentState", false)
                    editor.putString("blogPage", "search")
                    editor.putInt("selectedTabIndex", number)
                    editor.apply()

                    val intent = Intent(activity, MyBlogCheckActivity::class.java)
                    activity.startActivity(intent)
                    activity.finish()
                })
            }
        }
    }
}

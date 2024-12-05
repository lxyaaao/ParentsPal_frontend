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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.myapplication.activity.Main.BottomNavigationBar
import com.example.myapplication.activity.Main.HomeScreen
import com.example.myapplication.activity.Main.Immunization
import com.example.myapplication.activity.Main.ImmunizationResponse
import com.example.myapplication.activity.Main.NavItem
import com.example.myapplication.activity.Main.PersonScreen
import com.example.myapplication.activity.Main.QuestionAnswerScreen
import com.example.myapplication.activity.Main.saveImmunizations
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.example.myapplication.utils.NetworkUtils.sendGetRequest
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BlogMineActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                BlogMineScreen(this)
            }
        }
    }
}

@Composable
private fun BlogMineScreen(activity: Activity) {
    val navController = rememberNavController()
    var selectedTabIndex by remember { mutableStateOf(0) }

    Scaffold(
        topBar = { CenterAlignedTopAppBarExample(activity) },
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
            modifier = Modifier.padding(paddingValues)
        ) {
                paddingValuesIn ->
            Column(modifier = Modifier.padding(paddingValuesIn)) {
                when (selectedTabIndex) {
                    0 -> MyTabContent1(activity)
                    1 -> MyTabContent2(activity)
                }
            }
        }


    }
}


@Composable
fun MyTabContent1(activity: Activity) {
    MyShareContent(activity)
}

@Composable
fun MyTabContent2(activity: Activity) {
    MyAskContent(activity)
}

private fun BlogMainScreen(activity: Activity) {

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CenterAlignedTopAppBarExample(
    activity: Activity
) {
    var addClick by remember { mutableStateOf(false) }

    CenterAlignedTopAppBar(
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            titleContentColor = MaterialTheme.colorScheme.primary,
        ),
        title = {
            Text(
                "我的发表",
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        navigationIcon = {
            IconButton(onClick = {
                val intent = Intent(activity, BlogActivity::class.java)
                activity.startActivity(intent)
                activity.finish() }
            ) {
                Icon(
                    imageVector = Icons.Filled.ArrowBack,
                    contentDescription = "Back"
                )
            }
        },
        actions = {
            IconButton(onClick = { addClick = true }) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "添加"
                )
            }
        }
    )

    if (addClick) {
        val sharedPreferences: SharedPreferences =
            activity.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putInt("articleId", 0).apply()

        val intent = Intent(activity, BlogAddActivity::class.java)
        activity.startActivity(intent)
        activity.finish()
    }
}

@Composable
fun MyShareContent(activity: Activity) {
    val sharedPreferences: SharedPreferences =
        activity.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)

    val parentId = sharedPreferences.getInt("parentId", 0)
    var articles: List<Article> by remember { mutableStateOf(emptyList()) }

    val apiString = "user-article/$parentId"
    CoroutineScope(Dispatchers.IO).launch {
        val response = sendGetRequest(apiString)
        try {
            val gson = Gson()
            val apiResponse = gson.fromJson(response, GetArticleResponse::class.java)
            articles = apiResponse.data
        } catch (e: Exception) {
            println("Json error: $response")
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        items(articles.reversed()) { blogContent ->
            BlogContentCard(blogContent, onClick = {
                val editor = sharedPreferences.edit()
                editor.putInt("articleId", blogContent.articleId).apply()

                val intent = Intent(activity, MyBlogCheckActivity::class.java)
                activity.startActivity(intent)
                activity.finish()
            })
        }
    }
}

@Composable
fun MyAskContent(activity: Activity) {


}

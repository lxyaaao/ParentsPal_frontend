package com.example.myapplication.activity.Blog

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.material.icons.filled.Favorite
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
                    // 监听点击事件
                    detectTapGestures(onTap = {
                        isMenuExpanded = false
                    })
                }
        ) {
            paddingValuesIn ->
            Column(modifier = Modifier.padding(paddingValuesIn)) {
                when (selectedTabIndex) {
                    0 -> TabContent1()
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
fun TabContent1() {
    val shareContents = listOf(
        BlogContent(R.drawable.baseline_account_circle_24, "User1", "This is the first blog post!", listOf("Nice post!", "Thanks for sharing!"), "2024-10-22"),
        BlogContent(R.drawable.baseline_account_circle_24, "User2", "This is another blog post!", listOf("Interesting!", "I learned something new!"), "2024-10-23"),
        BlogContent(R.drawable.baseline_account_circle_24, "User3", "This is the third blog post and it is longer!", listOf(), "2024-10-23"),
    )

    ShareContent(shareContents)
}

@Composable
fun TabContent2() {
    val askContents = listOf(
        BlogContent(R.drawable.baseline_account_circle_24, "User1", "This is the first question!", listOf("Ans1"), "2024-10-22"),
        BlogContent(R.drawable.baseline_account_circle_24, "User2", "This is another question!", listOf("Ans1", "Ans2"), "2024-10-23"),
    )

    AskContent(askContents)
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
    var selectedItem by remember { mutableStateOf(0) }

    if (isMenuExpanded) {
        NavigationRail(
            containerColor = Color.White
        ) {
            // NavigationRailItem 1
            NavigationRailItem(
                icon = { Icon(Icons.Filled.Recommend, contentDescription = "推荐") },
                label = { Text("推荐") },
                selected = selectedItem == 0,
                onClick = {
                    selectedItem = 0
                    // 处理点击事件
                }
            )
            Spacer(modifier = Modifier.height(8.dp))
            // NavigationRailItem 2
            NavigationRailItem(
                icon = {
                    Icon(
                        Icons.Filled.Favorite,
                        contentDescription = "关注"
                    )
                },
                label = { Text("关注") },
                selected = selectedItem == 1,
                onClick = {
                    selectedItem = 1
                    // 处理点击事件
                }
            )
            Spacer(modifier = Modifier.height(8.dp))
            // NavigationRailItem 3
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
fun ShareContent(shareContents: List<BlogContent>) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        items(shareContents.reversed()) { blogContent ->
            BlogContentCard(blogContent, onClick = { })
        }
    }
}

@Composable
fun AskContent(askContents: List<BlogContent>) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        items(askContents.reversed()) { blogContent ->
            BlogContentCard(blogContent, onClick = { })
        }
    }
}

@Composable
fun BlogContentCard(blogContent: BlogContent, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .background(Color.White, shape = RoundedCornerShape(8.dp))
            .border(1.dp, Color.LightGray, shape = RoundedCornerShape(8.dp))
            .clickable { onClick() }
    ) {
        Column (modifier = Modifier.padding(16.dp)) {
            // 头像和昵称
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(id = blogContent.avatarUrl),
                    contentDescription = "Avatar",
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .padding(4.dp),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = blogContent.username)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 博客内容
            Text(text = blogContent.content)

            Spacer(modifier = Modifier.height(8.dp))

            // 评论
            for (comment in blogContent.comments) {
                Text(text = comment, color = Color.Gray)
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Posted on: ${blogContent.timestamp}", fontSize = 12.sp)
        }
    }
}

data class BlogContent(
    val avatarUrl: Int,
    val username: String,
    val content: String,
    val comments: List<String>,
    val timestamp: String
)
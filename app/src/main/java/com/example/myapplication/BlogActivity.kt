package com.example.myapplication

import android.app.Activity
import android.content.Context
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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Recommend
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialogDefaults.containerColor
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteType.Companion.NavigationRail
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.myapplication.ui.theme.MyApplicationTheme
import kotlinx.coroutines.launch

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
                                selectedTabIndex = index // 处理点击事件，例如切换内容
                            },
                            text = { Text(title) }
                        )
                    }
                }
            },
            modifier = Modifier.padding(paddingValues)
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
                SideRail(isMenuExpanded)
            }
        }


    }
}


@Composable
fun TabContent1() {
    ShareContent()
}

@Composable
fun TabContent2() {
    AskContent()
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
fun SideRail(isMenuExpanded: Boolean) {
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
                    // 处理点击事件
                }
            )
        }
    }
}

@Composable
fun ShareContent() {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp)
    ) {
        items(5) { index -> // 假设有5篇博客
            BlogPost(
                avatarResId = R.drawable.baseline_account_circle_24, // 替换为实际头像URL
                username = "User ${index + 1}",
                content = "这是第 ${index + 1} 篇博客。",
                comments = listOf("评论1", "评论2") // 示例评论
            )
        }
    }
}

@Composable
fun AskContent() {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp)
    ) {
        items(5) { index -> // 假设有5篇博客
            BlogPost(
                avatarResId = R.drawable.baseline_account_circle_24, // 替换为实际头像URL
                username = "User ${index + 1}",
                content = "这是第 ${index + 1} 个问题。",
                comments = listOf("回答1", "回答2") // 示例评论
            )
        }
    }
}

@Composable
fun BlogPost(avatarResId: Int, username: String, content: String, comments: List<String>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .background(Color.White, shape = RoundedCornerShape(8.dp))
            .border(1.dp, Color.LightGray, shape = RoundedCornerShape(8.dp))
            .padding(16.dp)
    ) {
        // 头像和昵称
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(id = avatarResId),
                contentDescription = "Avatar",
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .padding(4.dp),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = username)
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 博客内容
        Text(text = content)

        Spacer(modifier = Modifier.height(8.dp))

        // 评论
        for (comment in comments) {
            Text(text = comment, color = Color.Gray)
        }
    }
}
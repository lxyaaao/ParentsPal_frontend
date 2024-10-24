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
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.myapplication.ui.theme.MyApplicationTheme

class MeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MeScreen(this)
            }
        }
    }
}

@Composable
private fun MeScreen(activity: Activity) {
    val navController = rememberNavController()
    Scaffold(
        bottomBar = { BottomNavigationBar(navController) }
    ) { innerPadding ->
        CenterAlignedTopAppBarExample()
        NavHost(
            navController = navController,
            startDestination = NavItem.Person.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(NavItem.Home.route) { HomeScreen(activity) }
            composable(NavItem.Share.route) { ShareScreen(activity) }
            composable(NavItem.QuestionAnswer.route) { QuestionAnswerScreen(activity) }
            composable(NavItem.Person.route) { MeMainScreen(activity) }
        }
    }
}

@Composable
private fun MeMainScreen(activity: Activity) {
    val nickname = getNicknameFromSharedPreferences(activity)

    Column(modifier = Modifier.padding(32.dp)) {
        Spacer(modifier = Modifier.height(48.dp))
        ProfileSection(
            avatarResId = R.drawable.baseline_account_circle_24,
            nickname = nickname,
            activity
        )
    }
}

@Composable
fun ProfileSection(avatarResId: Int, nickname: String, activity: Activity) {
    var showImage by remember { mutableStateOf(false) }  // 控制大图是否显示
    var showEditButton by remember { mutableStateOf(false) }  // 控制“编辑资料”按钮是否显示

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalArrangement = Arrangement.Start // 左对齐
    ) {
        // 头像
        Image(
            painter = painterResource(id = avatarResId),
            contentDescription = "Avatar",
            modifier = Modifier
                .size(64.dp) // 头像大小
                .clip(CircleShape) // 圆形头像
                .clickable { showImage = true }  // 点击头像显示对话框
                .padding(4.dp),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.width(16.dp))

        // 昵称
        Text(
            text = nickname,
            fontSize = 20.sp,
            modifier = Modifier
                .align(Alignment.CenterVertically) // 垂直居中对齐
                .clickable { showEditButton = true }  // 点击显示“编辑资料”按钮
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Spacer(modifier = Modifier.height(16.dp))
        ButtonWithBars("我的关注") {
        }
        ButtonWithBars("我的粉丝") {
        }
        Divider(color = Color.LightGray, thickness = 1.dp)

        Spacer(modifier = Modifier.height(24.dp))
        ButtonWithBars("我的打卡") {
            val intent = Intent(activity, DailyLogActivity::class.java)
            activity.startActivity(intent)
            activity.finish()
        }
        ButtonWithBars("我的发表") {
            val intent = Intent(activity, BlogMineActivity::class.java)
            activity.startActivity(intent)
            activity.finish()
        }
        ButtonWithBars("我的私信") {
        }
        Divider(color = Color.LightGray, thickness = 1.dp)

        Spacer(modifier = Modifier.height(24.dp))
        ButtonWithBars("个人资料") {
        }
        ButtonWithBars("专业认证") {
            val intent = Intent(activity, CertificationActivity::class.java)
            activity.startActivity(intent)
            activity.finish()
        }
        Divider(color = Color.LightGray, thickness = 1.dp)

        Spacer(modifier = Modifier.height(24.dp))
        ButtonWithBars("设置") {
        }
        Divider(color = Color.LightGray, thickness = 1.dp)
    }

    // 如果 showDialog 为 true，则显示大图
    if (showImage || showEditButton) {
        val intent = Intent(activity, EditProfileActivity::class.java)
        activity.startActivity(intent)
        activity.finish()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CenterAlignedTopAppBarExample() {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),

        topBar = {
            CenterAlignedTopAppBar(
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                ),
                title = {
                    Text(
                        "Me",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                scrollBehavior = scrollBehavior,
            )
        },
    ) { innerPadding ->
        ScrollContent(innerPadding)
    }
}

@Composable
fun ButtonWithBars(buttonText: String, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Divider(color = Color.LightGray, thickness = 1.dp)

        // 按钮
        Button(
            onClick = onClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RectangleShape,
            colors = ButtonDefaults.buttonColors(Color.White)
        ) {
            Text(
                text = buttonText,
                color = Color.Black,
                textAlign = TextAlign.Center
            )
        }
    }
}
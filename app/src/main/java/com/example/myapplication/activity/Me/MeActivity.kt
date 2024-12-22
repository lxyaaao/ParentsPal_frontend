package com.example.myapplication.activity.Me

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.myapplication.R
import com.example.myapplication.activity.Blog.BlogMineActivity
import com.example.myapplication.activity.Main.BottomNavigationBar
import com.example.myapplication.activity.Main.DailyLogActivity
import com.example.myapplication.activity.Main.HomeScreen
import com.example.myapplication.activity.Main.NavItem
import com.example.myapplication.activity.Main.QuestionAnswerScreen
import com.example.myapplication.activity.Main.ScrollContent
import com.example.myapplication.activity.Main.ShareScreen
import com.example.myapplication.activity.Main.updateProfile
import com.example.myapplication.ui.theme.MyApplicationTheme
import java.io.File

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
    val name = getNameFromSharedPreferences(activity)

    updateProfile(activity)


    Column(modifier = Modifier.padding(32.dp)) {
        Spacer(modifier = Modifier.height(60.dp))
        ProfileSection(
            avatarResId = R.drawable.photo1,
            name = name,
            activity
        )

        Column(modifier = Modifier.fillMaxSize()) {
            Spacer(modifier = Modifier.height(24.dp))
            ButtonWithBars("宝宝信息") {
                val intent = Intent(activity, BabyActivity::class.java)
                activity.startActivity(intent)
                activity.finish()
            }
            Divider(color = Color.LightGray, thickness = 1.dp)

            Spacer(modifier = Modifier.height(24.dp))
            ButtonWithBars("好友列表") {
                val intent = Intent(activity, UserListActivity::class.java)
                activity.startActivity(intent)
                activity.finish()
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
            Divider(color = Color.LightGray, thickness = 1.dp)

            Spacer(modifier = Modifier.height(24.dp))
            ButtonWithBars("专业认证") {
                val intent = Intent(activity, CertificationActivity::class.java)
                activity.startActivity(intent)
                activity.finish()
            }
            Divider(color = Color.LightGray, thickness = 1.dp)

            Spacer(modifier = Modifier.height(24.dp))
            ButtonWithBars("设置") {
                val intent = Intent(activity, SettingActivity::class.java)
                activity.startActivity(intent)
                activity.finish()
            }
            Divider(color = Color.LightGray, thickness = 1.dp)
        }
    }
}

@Composable
fun ProfileSection(avatarResId: Int, name: String, activity: Activity) {
    var showImage by remember { mutableStateOf(false) }  // 控制大图是否显示
    var showEditButton by remember { mutableStateOf(false) }  // 控制“编辑资料”按钮是否显示

    val sharedPreferences: SharedPreferences =
        activity.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)
    val context = LocalContext.current
    val parentId = sharedPreferences.getInt("parentId", 0)

    var localImagePath by remember { mutableStateOf<String?>(null) }
    val file = File(context.cacheDir, "downloaded_image_$parentId.jpg")
    localImagePath = file.absolutePath

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalArrangement = Arrangement.Start // 左对齐
    ) {
        val image = remember(localImagePath) {
            BitmapFactory.decodeFile(localImagePath)?.asImageBitmap()
        } ?: ImageVector.vectorResource(id = R.drawable.photo9) // 使用矢量图
        Image(
            imageVector = image as? ImageVector ?: ImageVector.vectorResource(id = R.drawable.photo9), // 强制转换为 ImageVector
            contentDescription = "Avatar",
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .clickable { showImage = true },
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.width(16.dp))

        // 昵称
        Text(
            text = name,
            fontSize = 20.sp,
            modifier = Modifier
                .align(Alignment.CenterVertically) // 垂直居中对齐
                .align(Alignment.CenterVertically) // 垂直居中对齐
                .clickable { showEditButton = true }  // 点击显示“编辑资料”按钮
        )
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
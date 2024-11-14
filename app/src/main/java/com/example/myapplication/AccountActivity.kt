package com.example.myapplication

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.myapplication.ui.theme.MyApplicationTheme

class AccountActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                AccountScreen(this)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AccountScreen(activity: Activity) {
    var backFlag by remember { mutableStateOf(false) }
    var passwordDialog by remember { mutableStateOf(false) }

    val sharedPreferences: SharedPreferences =
        activity.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                ),
                title = {
                    Text(
                        "账户设置",
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
                },
            )
        },
        content = { paddingValues ->
            // 主内容，确保遵守padding
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(paddingValues)
                    .padding(start = 16.dp)
            ) {
                Divider(color = Color.LightGray, thickness = 1.dp)

                ButtonWithTwoTexts(
                    leftText = "ID",
                    rightText = " ",
                    onClick = {}
                )

                Spacer(modifier = Modifier.height(24.dp))
                Divider(color = Color.LightGray, thickness = 1.dp)

                ButtonWithTwoTexts(
                    leftText = "电话",
                    rightText = sharedPreferences.getString("phoneNumber", " ") ?: " ",
                    onClick = {}
                )

                Divider(color = Color.LightGray, thickness = 1.dp)

                ButtonWithTwoTexts(
                    leftText = "修改密码",
                    rightText = " ",
                    onClick = {passwordDialog = true}
                )
            }
        }
    )

    if (backFlag) {
        val intent = Intent(activity, SettingActivity::class.java)
        activity.startActivity(intent)
        activity.finish()
    }

    if(passwordDialog) {
        PasswordChangeDialog(onDismiss = { passwordDialog = false })
    }
}

@Composable
fun PasswordChangeDialog(onDismiss: () -> Unit) {
    var oldPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var newErrorMessage by remember { mutableStateOf("") } // 用于显示错误消息

    // 弹出对话框
    AlertDialog(
        onDismissRequest = { onDismiss() }, // 点击外部关闭
        title = { Text(text = "修改密码") },
        text = {
            Column {
                // 显示旧密码输入框
                TextField(
                    value = oldPassword,
                    onValueChange = { oldPassword = it },
                    label = { Text("旧密码") },
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = PasswordVisualTransformation() // 密码遮挡
                )
                Spacer(modifier = Modifier.height(8.dp))

                // 显示新密码输入框
                TextField(
                    value = newPassword,
                    onValueChange = { newPassword = it },
                    label = { Text("新密码") },
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = PasswordVisualTransformation()
                )
                Spacer(modifier = Modifier.height(8.dp))

                // 确认新密码输入框
                TextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = { Text("确认新密码") },
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = PasswordVisualTransformation()
                )
                Spacer(modifier = Modifier.height(8.dp))

                // 显示错误消息（如果有的话）
                if (newErrorMessage.isNotEmpty()) {
                    Text(text = newErrorMessage, color = Color.Red)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (newPassword == confirmPassword) {
                    // 新密码一致，关闭对话框
                    onDismiss()
                } else {
                    // 新密码不一致，显示错误消息
                    newErrorMessage = "两次输入的新密码不同"
                }
            }) {
                Text("确认")
            }
        },
        dismissButton = {
            TextButton(onClick = { onDismiss() }) {
                Text("取消")
            }
        }
    )
}
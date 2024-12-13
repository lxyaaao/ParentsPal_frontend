package com.example.myapplication.activity.Me

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.myapplication.api.LoginRequest
import com.example.myapplication.api.LoginResponse
import com.example.myapplication.api.RetrofitClient
import com.example.myapplication.ui.theme.MyApplicationTheme
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

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
    val id = sharedPreferences.getInt("parentId", 0) ?: 0
    val phoneNumber = sharedPreferences.getString("phoneNumber", "") ?: ""

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
                    rightText = "$id",
                    onClick = {}
                )

                Spacer(modifier = Modifier.height(24.dp))
                Divider(color = Color.LightGray, thickness = 1.dp)

                ButtonWithTwoTexts(
                    leftText = "电话",
                    rightText = phoneNumber,
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
        PasswordChangeDialog(phoneNumber, activity, onDismiss = { passwordDialog = false })
    }
}

@Composable
fun PasswordChangeDialog(phoneNumber: String, activity: Activity, onDismiss: () -> Unit) {
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
                checkPassword(phoneNumber, oldPassword,
                        newPassword, confirmPassword, activity) {success ->
                    if (success) {
                        onDismiss()
                    }
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

fun checkPassword(phoneNumber: String, oldPassword: String,
                  newPassword: String, confirmPassword: String,
                  activity: Activity, callback: (Boolean) -> Unit) {
    val request = LoginRequest(phoneNumber = phoneNumber, password = oldPassword)

    RetrofitClient.apiService.login(request).enqueue(object : Callback<LoginResponse> {
        override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
            if (response.isSuccessful) {
                val loginResponse = response.body()
                if (loginResponse != null) {
                    if (loginResponse.status) {
                        if (newPassword == confirmPassword) {
                            // TODO: post changePassword message to backend
                            // changePassword(oldPassword, newPassword)
                            callback(true)
                        } else {
                            Toast.makeText(activity, "新密码和确认密码不匹配", Toast.LENGTH_SHORT).show()
                            callback(false)
                        }
                    } else {
                        Toast.makeText(activity, loginResponse.message, Toast.LENGTH_SHORT).show()
                        callback(false)
                    }
                }
            } else {
                Toast.makeText(activity, "网络错误，请稍后再试", Toast.LENGTH_SHORT).show()
                callback(false)
            }
        }

        override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
            Toast.makeText(activity, "网络错误，请稍后再试", Toast.LENGTH_SHORT).show()
            callback(false)
        }
    })
}

fun changePassword(oldPassword: String, newPassword: String): Boolean {
//    val response = RetrofitClient.apiService.changePassword(
//        ChangePasswordRequest(
//            oldPassword = oldPassword,
//            newPassword = newPassword
//        )
//    )
//
//    if (response.isSuccessful) {
//        // 处理成功逻辑
//        val responseBody = response.body()
//        if (responseBody?.status == true) {
//            return true
//        }
//    }
    return false
}

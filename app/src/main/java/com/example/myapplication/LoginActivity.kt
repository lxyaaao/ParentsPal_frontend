package com.example.myapplication

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.example.myapplication.api.LoginResponse
import com.example.myapplication.api.LoginRequest
import com.example.myapplication.api.RegisterRequest
import com.example.myapplication.api.RetrofitClient
import retrofit2.Response
import retrofit2.Call
import retrofit2.Callback


class LoginActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 检查是否已经登录
        if (isLoggedIn()) {
            // 如果已登录，则跳转到主界面
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
            return
        }

        setContent {
            MyApplicationTheme {
                LoginScreen(this, onLoginSuccess = {
                    // Navigate to MainActivity on successful login
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    this.overridePendingTransition(0, 0)
                    finish()
                })
            }
        }
    }

    private fun isLoggedIn(): Boolean {
        val sharedPreferences = getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)
        return sharedPreferences.getBoolean("isLoggedIn", false)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(activity: Activity, onLoginSuccess: () -> Unit) {
    // Replace with actual login UI and logic
    var tele by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                ),
                title = {
                    Text(
                        "登录",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
            )
        },
        content = { paddingValues ->
            // 主内容，确保遵守padding
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentSize(Alignment.Center)
                    .padding(paddingValues),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center) {
                Spacer(modifier = Modifier.height(16.dp))
                TextField(
                    value = tele,
                    onValueChange = { tele = it },
                    label = { Text("电话") },
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
                Spacer(modifier = Modifier.height(16.dp))
                TextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("密码") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = {
                    loginLogic(tele, password, activity, onLoginSuccess)
                },
                    modifier = Modifier.align(Alignment.CenterHorizontally)) {
                    Text("登入")
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxSize() // 占满整个屏幕
                    .padding(bottom = 16.dp) // 保证按钮不会紧贴屏幕底部，留下一些距离
            ) {
                // 透明背景按钮，位于底部中央
                TextButton(
                    onClick = {
                        // 跳转到下一个界面
                        val intent = Intent(activity, SignUpActivity::class.java)
                        activity.startActivity(intent)
                        activity.finish()
                    },
                    modifier = Modifier
                        .align(Alignment.BottomCenter) // 保证按钮在屏幕底部中央
                ) {
                    Text(
                        text = "没有账户？点我注册",
                        fontSize = 12.sp, // 设置较小字体
                        color = Color.Gray // 设置文字颜色
                    )
                }
            }
        }
    )
}

fun loginLogic(phoneNumber: String, password: String, activity: Activity, onLoginSuccess: () -> Unit) {
    val request = LoginRequest(phoneNumber = phoneNumber, password = password)

    RetrofitClient.apiService.login(request).enqueue(object : Callback<LoginResponse> {
        override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
            if (response.isSuccessful) {
                val loginResponse = response.body()
                if (loginResponse != null) {
                    if (loginResponse.status) {
                        saveLoginStatus(activity, true)
                        onLoginSuccess()
                    } else {
                        Toast.makeText(activity, loginResponse.message, Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(activity, "网络错误，请稍后再试", Toast.LENGTH_SHORT).show()
            }
        }

        override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
            Toast.makeText(activity, "网络错误，请稍后再试", Toast.LENGTH_SHORT).show()
        }
    })
}

fun saveLoginStatus(activity: Activity, isLoggedIn: Boolean) {
    val sharedPreferences =
        activity.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)
    val editor = sharedPreferences.edit()
    editor.putBoolean("isLoggedIn", isLoggedIn)
    editor.apply()
}

fun saveInfo(activity: Activity, phoneNumber: String) {
    val sharedPreferences =
        activity.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)
    val editor = sharedPreferences.edit()
    editor.putString("phoneNumber", phoneNumber)
    editor.apply()
}
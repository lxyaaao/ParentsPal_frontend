package com.example.myapplication.activity.Main

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
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
import com.example.myapplication.api.Baby
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.example.myapplication.utils.NetworkUtils.sendPostRequestWithRequest
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject

data class RegisterResponse(
    val message: String,
    val status: Boolean,
    val parentId: Int,
    val parentName: String,
    val babies: List<Baby>?
)

class SignUpActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContent {
            MyApplicationTheme {
                SignUpScreen(this, onLoginSuccess = {
                    // Navigate to MainActivity on successful login
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    this.overridePendingTransition(0, 0)
                    finish()
                })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpScreen(activity: Activity, onLoginSuccess: () -> Unit) {
    // Replace with actual login UI and logic
    var tele by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }

    var errorDialog by remember { mutableStateOf(false) }
    var backFlag by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                ),
                title = {
                    Text(
                        "注册",
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
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentSize(Alignment.Center)
                    .padding(paddingValues),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center) {
                Spacer(modifier = Modifier.height(16.dp))
                TextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("名字") },
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFFF1E3E6),
                        unfocusedContainerColor = Color(0xFFF1E3E6),
                    ),
                )
                Spacer(modifier = Modifier.height(16.dp))
                TextField(
                    value = tele,
                    onValueChange = { tele = it },
                    label = { Text("电话") },
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFFF1E3E6),
                        unfocusedContainerColor = Color(0xFFF1E3E6),
                    ),
                )
                Spacer(modifier = Modifier.height(16.dp))
                TextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("密码") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFFF1E3E6),
                        unfocusedContainerColor = Color(0xFFF1E3E6),
                    ),
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = {
                    if (tele.length == 11) {
                        CoroutineScope(Dispatchers.Main).launch{
                            if (registerUser(name, tele, password, activity)) {
                                saveLoginStatus(activity, true)
                                saveTele(activity, tele)
                                onLoginSuccess()
                            }
                        }
                    } else {
                        errorDialog = true
                    }
                },
                    modifier = Modifier.align(Alignment.CenterHorizontally)) {
                    Text("注册")
                }
            }
        }
    )

    if (backFlag) {
        val intent = Intent(activity, LoginActivity::class.java)
        activity.startActivity(intent)
        activity.finish()
    }

    if(errorDialog) {
        AlertDialog(
            onDismissRequest = { errorDialog = false },
            text = { Text(
                text = "不是有效的电话号码！",
                modifier = Modifier.padding(16.dp)
            ) },confirmButton = {
                TextButton(
                    onClick = {
                        errorDialog = false
                    }
                ) {
                    Text("确认")
                }
            },
        )
    }
}

suspend fun registerUser(name: String, phoneNumber: String, password: String, activity: Activity): Boolean {
    try {
        val apiPath = "api/appuser/register"

        val requestBody = JSONObject().apply {
            put("name", name)
            put("phoneNumber", phoneNumber)
            put("password", password)
        }

        val responseString = sendPostRequestWithRequest(apiPath, requestBody.toString())

        val gson = Gson()
        val response = gson.fromJson(responseString, RegisterResponse::class.java)
        println("Parsed response: $response")

        if (response.status) {
            val babiesList = response.babies ?: emptyList()
            saveUser(activity, response.parentId, response.parentName, babiesList)
            return true
        } else {
            withContext(Dispatchers.Main) {
                Toast.makeText(activity, "Error: ${response.message}", Toast.LENGTH_SHORT).show()
            }
        }
    } catch (e: Exception) {
        println("Error")
    }

    return false
}
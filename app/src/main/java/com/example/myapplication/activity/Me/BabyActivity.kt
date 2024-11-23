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
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.R
import com.example.myapplication.activity.Main.RegisterResponse
import com.example.myapplication.activity.Main.saveUser
import com.example.myapplication.api.Baby
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.example.myapplication.utils.NetworkUtils.sendPostRequestWithRequest
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject

class BabyActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                BabyScreen(this)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BabyScreen(activity: Activity) {
    var backFlag by remember { mutableStateOf(false) }
    var nameClick by remember { mutableStateOf(false) }
    var genderClick by remember { mutableStateOf(false) }
    var birthClick by remember { mutableStateOf(false) }
    val sharedPreferences: SharedPreferences =
        activity.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)

    var name by remember { mutableStateOf(sharedPreferences.getString("babyName", "宝宝名字") ?: "宝宝名字") }
    var babyGender by remember { mutableStateOf(sharedPreferences.getString("babyGender", "") ?: "") }
    var babyBirth by remember { mutableStateOf(sharedPreferences.getString("babyBirthdate", "") ?: "") }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                ),
                title = {
                    Text(
                        "宝宝信息",
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
                    .padding(paddingValues)
                    .padding(start = 16.dp)
            ) {
                ButtonWithTwoTexts(
                    leftText = "宝宝名字",
                    rightText = sharedPreferences.getString("babyName", "宝宝名字") ?: "宝宝名字",
                    onClick = { nameClick = true }
                )

                Divider(color = Color.LightGray, thickness = 1.dp)

                ButtonWithTwoTexts(
                    leftText = "宝宝性别",
                    rightText = sharedPreferences.getString("babyGender", "") ?: "",
                    onClick = { genderClick = true }
                )

                Divider(color = Color.LightGray, thickness = 1.dp)

                ButtonWithTwoTexts(
                    leftText = "宝宝生日",
                    rightText = sharedPreferences.getString("babyBirthdate", "") ?: "",
                    onClick = { birthClick = true }
                )
            }
        }
    )

    LaunchedEffect(backFlag) {
        if (backFlag) {
            val parentId = sharedPreferences.getInt("parentId", 0)
            val babyId = sharedPreferences.getInt("babyId", 0)

            if (babyId == 0) {
                CoroutineScope(Dispatchers.Main).launch {
                    addBaby(parentId, name, babyGender, babyBirth, activity)
                }
            }

            val intent = Intent(activity, MeActivity::class.java)
            activity.startActivity(intent)
            activity.finish()
        }
    }

    if (nameClick) {
        NameInputDialog(initialName = name,
            onDismiss = { nameClick = false },
            onConfirm = { newName ->
                val editor = sharedPreferences.edit()
                editor.putString("babyName", newName)
                editor.apply()
                nameClick = false })
    }

    if (genderClick) {
        GenderInputDialog(onDismiss = { genderClick = false },
            onConfirm = { newGender ->
                val editor = sharedPreferences.edit()
                editor.putString("babyGender", newGender)
                editor.apply()
                genderClick = false })
    }

    if (birthClick) {
        BirthInputDialog(onDismiss = { birthClick = false },
            onConfirm = { newBirthdate ->
                val editor = sharedPreferences.edit()
                editor.putString("babyBirthdate", newBirthdate)
                editor.apply()
                birthClick = false })
    }
}

@Composable
fun NameInputDialog(
    initialName: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var name by remember { mutableStateOf(TextFieldValue(initialName)) }

    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text(text = "修改名字") },
        text = {
            TextField(
                value = name,
                onValueChange = { name = it },
                placeholder = { Text(text = "输入新名字", color = Color.Gray) }
            )
        },
        confirmButton = {
            Button(onClick = { onConfirm(name.text) }) {
                Text(text = "确定")
            }
        },
        dismissButton = {
            Button(onClick = { onDismiss() }) {
                Text(text = "取消")
            }
        }
    )
}

@Composable
fun GenderInputDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text(text = "选择性别") },
        text = { },
        confirmButton = {
            Button(onClick = { onConfirm("Male") }) {
                Text(text = "Male")
            }
        },
        dismissButton = {
            Button(onClick = { onConfirm("Female") }) {
                Text(text = "Female")
            }
        }
    )
}

@Composable
fun BirthInputDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var date by remember { mutableStateOf(TextFieldValue("")) }

    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text(text = "填写生日") },
        text = {
            TextField(
                value = date,
                onValueChange = { date = it },
                placeholder = { Text(text = "输入生日，格式如 0000-00-00", color = Color.Gray) }
            )
        },
        confirmButton = {
            Button(onClick = {
                if (date.text.length == 10 &&  date.text[4] == '-' && date.text[7] == '-') {
                    onConfirm(date.text)
                } else {
                    date = TextFieldValue("")
                }}) {
                Text(text = "确定")
            }
        },
        dismissButton = {
            Button(onClick = { onDismiss() }) {
                Text(text = "取消")
            }
        }
    )
}

suspend fun addBaby(parentId: Int, name: String, babyGender: String, babyBirth: String, activity: Activity) {
    val apiPath = "api/v1/appuser/$parentId/babies"

    val requestBody = JSONObject().apply {
        put("name", name)
        put("gender", babyGender)
        put("birthdate", babyBirth)
        put("photoUrl", "None")
    }

    val responseString = sendPostRequestWithRequest(apiPath, requestBody.toString())

    val gson = Gson()
    val response = gson.fromJson(responseString, Baby::class.java)
    println("Parsed response: $response")

    val sharedPreferences =
        activity.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)
    val editor = sharedPreferences.edit()
    editor.putInt("babyId", response.id.toInt())

}
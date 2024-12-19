package com.example.myapplication.activity.Me

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.icu.text.SimpleDateFormat
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
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
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.example.myapplication.utils.sendPatchRequest
import com.google.android.material.datepicker.MaterialDatePicker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.util.Date
import java.util.Locale

class EditProfileActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                EditProfileScreen(this)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditProfileScreen(activity: Activity) {
    var backFlag by remember { mutableStateOf(false) }
    var profileClick by remember { mutableStateOf(false) }
    var nameClick by remember { mutableStateOf(false) }
    val sharedPreferences: SharedPreferences =
        activity.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)

    var name by remember { mutableStateOf(sharedPreferences.getString("name", "昵称") ?: "昵称") }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                ),
                title = {
                    Text(
                        "编辑资料",
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
                ButtonWithTextAndIcon(
                    text = "头像",
                    icon = painterResource(id = R.drawable.photo1),  // 替换为实际图片资源
                    onClick = { profileClick = true }
                )

                Divider(color = Color.LightGray, thickness = 1.dp)

                ButtonWithTwoTexts(
                    leftText = "名字",
                    rightText = name,
                    onClick = { nameClick = true }
                )
            }
        }
    )

    if (backFlag) {
        val intent = Intent(activity, MeActivity::class.java)
        activity.startActivity(intent)
        activity.finish()
    }

    if (profileClick) {
        AlertDialog(
            onDismissRequest = { profileClick = false },
            confirmButton = {
                Button(onClick = { profileClick = true }) {
                    Text("更换头像")
                }
            },
            text = {
                Image(
                    painter = painterResource(id = R.drawable.photo1),
                    contentDescription = "Large Avatar",
                    modifier = Modifier
                        .size(300.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            }
        )
    }

    if (nameClick) {
        NameChangeDialog(initialName = name,
            onDismiss = { nameClick = false },
            onConfirm = { newName ->
                name = newName
                saveNameToSharedPreferences(activity, newName)

                val parentId = sharedPreferences.getInt("parentId", 0)
                val apiString = "api/appuser/${parentId}/change-name"

                CoroutineScope(Dispatchers.IO).launch {
                    val requestBody = JSONObject().apply {
                        put("newName", newName)
                    }

                    println(requestBody)

                    val response = sendPatchRequest(apiString, requestBody.toString())
                    try {
                        println(response)
                    } catch (e: Exception) {
                        println("Json error: $response")
                    }
                }

                nameClick = false })
    }
}

@Composable
fun ButtonWithTextAndIcon(text: String, icon: Painter, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .background(Color.White)
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = text, fontSize = 18.sp, modifier = Modifier.weight(1f))

            Image(
                painter = icon,
                contentDescription = null,
                modifier = Modifier.size(36.dp)
                    .clip(CircleShape)
            )
        }
    }
}

@Composable
fun ButtonWithTwoTexts(leftText: String, rightText: String, onClick: () -> Unit, color: Boolean = true) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .background(if (color) Color.White else Color.Transparent)
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = leftText, fontSize = 18.sp, modifier = Modifier.weight(1f))

            Text(text = rightText, fontSize = 18.sp, textAlign = TextAlign.End)
        }
    }
}

@Composable
fun NameChangeDialog(
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

fun saveNameToSharedPreferences(activity: Activity, name: String) {
    val sharedPreferences: SharedPreferences =
        activity.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)
    val editor = sharedPreferences.edit()
    editor.putString("name", name)
    editor.apply()
}

fun getNameFromSharedPreferences(activity: Activity): String {
    val sharedPreferences: SharedPreferences =
        activity.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)
    return sharedPreferences.getString("name", "昵称") ?: "昵称"
}
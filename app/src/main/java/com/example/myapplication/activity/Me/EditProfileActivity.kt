package com.example.myapplication.activity.Me

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

class EditProfileActivity : ComponentActivity() {
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
    var genderClick by remember { mutableStateOf(false) }
    var birthClick by remember { mutableStateOf(false) }
    val sharedPreferences: SharedPreferences =
        activity.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)

    var name by remember { mutableStateOf(sharedPreferences.getString("name", "宝宝名字") ?: "宝宝名字") }

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
                    icon = painterResource(id = R.drawable.baseline_account_circle_24),  // 替换为实际图片资源
                    onClick = { profileClick = true }
                )

                Divider(color = Color.LightGray, thickness = 1.dp)

                ButtonWithTwoTexts(
                    leftText = "名字",
                    rightText = name,
                    onClick = {  }
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
                    painter = painterResource(id = R.drawable.baseline_account_circle_24),
                    contentDescription = "Large Avatar",
                    modifier = Modifier
                        .size(300.dp) // 大图大小
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            }
        )
    }

    if (nameClick) {
        NameInputDialog(initialName = name,
            onDismiss = { nameClick = false },
            onConfirm = { newName ->
                name = newName
                saveNameToSharedPreferences(activity, newName)
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
                modifier = Modifier.size(32.dp)
            )
        }
    }
}

@Composable
fun ButtonWithTwoTexts(leftText: String, rightText: String, onClick: () -> Unit) {
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
            Text(text = leftText, fontSize = 18.sp, modifier = Modifier.weight(1f))

            Text(text = rightText, fontSize = 18.sp, textAlign = TextAlign.End)
        }
    }
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
    return sharedPreferences.getString("name", "宝宝名字") ?: "宝宝名字"
}
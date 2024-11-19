package com.example.myapplication.activity.Me

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.myapplication.ui.theme.MyApplicationTheme

class CertificationActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                CertificationScreen(this)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CertificationScreen(activity: Activity) {
    val sharedPreferences: SharedPreferences =
        activity.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)

    var backFlag by remember { mutableStateOf(false) }
    var confirmClick by remember { mutableStateOf(false) }

    var nameClick by remember { mutableStateOf(false) }
    var expertName by remember { mutableStateOf(sharedPreferences.getString("expertName", " ") ?: " ") }

    var jobClick by remember { mutableStateOf(false) }
    var job by remember { mutableStateOf(sharedPreferences.getString("job", " ") ?: " ") }

    var certificationClick by remember { mutableStateOf(false) }
    var certification by remember { mutableStateOf(sharedPreferences.getString("job", " ") ?: " ") }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                ),
                title = {
                    Text(
                        "专业认证",
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
                actions = {
                    IconButton(onClick = { confirmClick = true }) {
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = "确认"
                        )
                    }
                }
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
                ButtonWithTwoTexts(
                    leftText = "名字",
                    rightText = expertName,
                    onClick = { nameClick = true }
                )

                Divider(color = Color.LightGray, thickness = 1.dp)

                ButtonWithTwoTexts(
                    leftText = "职业",
                    rightText = job,
                    onClick = { jobClick = true }
                )

                Divider(color = Color.LightGray, thickness = 1.dp)

                ButtonWithTwoTexts(
                    leftText = "上传证书",
                    rightText = "",
                    onClick = { certificationClick = true }
                )
            }
        }
    )

    if (backFlag) {
        val intent = Intent(activity, MeActivity::class.java)
        activity.startActivity(intent)
        activity.finish()
    }

    if (confirmClick) {
        saveToSharedPreferences(activity, expertName, job)

        val intent = Intent(activity, MeActivity::class.java)
        activity.startActivity(intent)
        activity.finish()
    }

    if (nameClick) {
        ExpertNameInputDialog(initialName = "",
            onDismiss = { nameClick = false },
            onConfirm = { newExpertName ->
                expertName = newExpertName
                nameClick = false })
    }

    if (jobClick) {
        JobInputDialog(initialJob = "",
            onDismiss = { jobClick = false },
            onConfirm = { newJob ->
                job = newJob
                jobClick = false })
    }

    if (certificationClick) {
        AlertDialog(
            onDismissRequest = { certificationClick = false },
            confirmButton = {
                Button(onClick = { certificationClick = true }) {
                    Text("上传新证书")
                }
            },
            text = {
                // 证书显示的相关逻辑
            }
        )
    }
}

@Composable
fun ExpertNameInputDialog(
    initialName: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var expertName by remember { mutableStateOf(TextFieldValue(initialName)) }

    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text(text = "修改名字") },
        text = {
            TextField(
                value = expertName,
                onValueChange = { expertName = it },
                placeholder = { Text(text = "输入真实姓名", color = Color.Gray) }
            )
        },
        confirmButton = {
            Button(onClick = { onConfirm(expertName.text) }) {
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
fun JobInputDialog(
    initialJob: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var job by remember { mutableStateOf(TextFieldValue(initialJob)) }

    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text(text = "修改职业") },
        text = {
            TextField(
                value = job,
                onValueChange = { job = it },
                placeholder = { Text(text = "输入您的职业", color = Color.Gray) }
            )
        },
        confirmButton = {
            Button(onClick = { onConfirm(job.text) }) {
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


fun saveToSharedPreferences(activity: Activity, expertName: String, job: String) {
    val sharedPreferences: SharedPreferences =
        activity.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)
    val editor = sharedPreferences.edit()
    editor.putString("expertName", expertName)
    editor.putString("job", job)
    editor.apply()
}
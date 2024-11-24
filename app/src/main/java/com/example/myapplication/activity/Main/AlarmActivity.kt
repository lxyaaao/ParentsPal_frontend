package com.example.myapplication.activity.Main

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.material3.CenterAlignedTopAppBar
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.myapplication.api.Baby
import com.example.myapplication.api.RetrofitClient
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.example.myapplication.utils.NetworkUtils.sendGetRequest
import com.example.myapplication.utils.NetworkUtils.sendPostRequestWithRequest
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONException
import org.json.JSONObject
import java.time.LocalDate

data class Alarm(
    val activityType: String,
    val alarmTime: String,
    val frequency: String,
    val active: Boolean
)

data class AlarmResponse(
    val id: Int,
    val babyId: Int,
    val activityType: String,
    val alarmTime: String,
    val frequency: String,
    val recurring: Boolean,
    val active: Boolean
)

class AlarmActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                AlarmScreen(this)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AlarmScreen(activity: Activity) {
    var backFlag by remember { mutableStateOf(false) }
    var addClick by remember { mutableStateOf(false) }

    val sharedPreferences: SharedPreferences =
        activity.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)
    val babyId: Int = sharedPreferences.getInt("babyId", 0)
    var alarms by remember { mutableStateOf(loadAlarms(sharedPreferences)) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                ),
                title = {
                    Text(
                        "闹钟提醒",
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
                    IconButton(onClick = { addClick = true }) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = "添加"
                        )
                    }
                }
            )
        },
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(paddingValues)
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    items(alarms.reversed()) { babyAlarm ->
                        AlarmCard(babyAlarm) {
                            // 删除打卡记录
                            alarms = alarms.filter { it != babyAlarm }
                            saveAlarms(sharedPreferences, alarms) // 更新存储
                        }
                    }
                }
            }
        }
    )

    if (backFlag) {
        val intent = Intent(activity, MainActivity::class.java)
        activity.startActivity(intent)
        activity.finish()
    }

    if (addClick) {
        AddAlarmDialog(activity,
            onDismiss = { addClick = false },
            onAdd = { time, type, frequency ->
                val newAlarm = Alarm(time, type, frequency, true)
                alarms = alarms + newAlarm
                saveAlarms(sharedPreferences, alarms)
            })
    }

}

@Composable
fun AddAlarmDialog(activity: Activity, onDismiss: () -> Unit, onAdd: (String, String, String) -> Unit) {
    val sharedPreferences: SharedPreferences =
        activity.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)
    val babyId: Int = sharedPreferences.getInt("babyId", 0)
    var alarmTime by remember { mutableStateOf("") }
    var activityType by remember { mutableStateOf("") }
    var frequency by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("添加闹钟") },
        text = {
            Column {
                TextField(
                    value = alarmTime,
                    onValueChange = { alarmTime = it },
                    label = { Text("输入时间") }
                )
                TextField(
                    value = activityType,
                    onValueChange = { activityType = it },
                    label = { Text("输入事件") }
                )
                TextField(
                    value = frequency,
                    onValueChange = { frequency = it },
                    label = { Text("选择频率") }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    CoroutineScope(Dispatchers.Main).launch {
                        if (alarmTime.isNotBlank() && activityType.isNotBlank() && frequency.isNotBlank()  ) {
//                            addAlarm(babyId, alarmTime, activityType, frequency)
                            onAdd(alarmTime, activityType, frequency)
                            onDismiss()
                        }
                    }
                }
            ) {
                Text("添加")
            }
        },
        dismissButton = {
            Button(onDismiss) {
                Text("取消")
            }
        }
    )
}


@Composable
fun AlarmCard(alarm: Alarm, onDelete: () -> Unit) {
    var showDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .background(Color.LightGray.copy(alpha = 0.2f))
            .padding(16.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = alarm.activityType)
            IconButton(onClick = { showDialog = true }) {
                Icon(Icons.Default.Delete, contentDescription = "删除")
            }
        }
        Text(text = "时间： ${alarm.alarmTime} ")
        Text(text = "频率： ${alarm.frequency} ")
        Text(text = "是否开启： ${alarm.active} ")
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("确认删除") },
            text = { Text("您确定要删除这个闹钟吗？") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete()
                        showDialog = false
                    }
                ) {
                    Text("确认")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("取消")
                }
            }
        )
    }
}

fun loadAlarms(sharedPreferences: SharedPreferences): List<Alarm> {
    val json = sharedPreferences.getString("alarms", "[]") ?: "[]"
    return Gson().fromJson(json, Array<Alarm>::class.java).toList()
}

fun saveAlarms(sharedPreferences: SharedPreferences, alarms: List<Alarm>) {
    val editor = sharedPreferences.edit()
    val json = Gson().toJson(alarms)
    editor.putString("alarms", json).apply()
}
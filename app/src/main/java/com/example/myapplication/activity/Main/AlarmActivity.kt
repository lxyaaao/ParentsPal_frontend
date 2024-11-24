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
    val id: Int,
    val activityType: String,
    val alarmTime: String,
    val customIntervalInHours: Int,
    val isRecurring: Boolean,
    val active: Boolean
)

data class AlarmResponse(
    val id: Int,
    val babyId: Int,
    val activityType: String,
    val alarmTime: String,
    val customIntervalInHours: Int,
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
            onAdd = { time, type, hours, recurring ->
                val newAlarm = Alarm(0, type, time, hours, recurring, true)
                alarms = alarms + newAlarm
                saveAlarms(sharedPreferences, alarms)
            })
    }

}

@Composable
fun AddAlarmDialog(activity: Activity, onDismiss: () -> Unit, onAdd: (String, String, Int, Boolean) -> Unit) {
    val sharedPreferences: SharedPreferences =
        activity.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)
    val babyId: Int = sharedPreferences.getInt("babyId", 0)
    var alarmDate by remember { mutableStateOf("") }
    var alarmTime by remember { mutableStateOf("") }
    var activityType by remember { mutableStateOf("") }
    var hour by remember { mutableStateOf("") }
    var isRecurring by remember { mutableStateOf(true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("添加闹钟") },
        text = {
            Column {
                TextField(
                    value = alarmDate,
                    onValueChange = { alarmDate = it },
                    label = { Text("输入日期，格式如0000-00-00") }
                )
                TextField(
                    value = alarmTime,
                    onValueChange = { alarmTime = it },
                    label = { Text("输入时间，格式如00:00:00") }
                )
                TextField(
                    value = activityType,
                    onValueChange = { activityType = it },
                    label = { Text("输入事件") }
                )
                TextField(
                    value = hour,
                    onValueChange = { hour = it },
                    label = { Text("输入循环时间") }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (alarmDate.length!= 10 ||  alarmDate[4] != '-' || alarmDate[7] != '-') {
                        alarmDate = ""
                    }
                    if (alarmTime.length!= 8 ||  alarmTime[2] != ':' || alarmTime[5] != ':') {
                        alarmTime = ""
                    }

                    CoroutineScope(Dispatchers.Main).launch {
                        if (hour == "0") {
                            isRecurring = false
                        } else {
                            isRecurring = true
                        }
                        if ( alarmTime.isNotBlank() && alarmDate.isNotBlank() && activityType.isNotBlank() && hour.isNotBlank() ) {
                            addAlarm(babyId, alarmDate + "T" + alarmTime, activityType, hour)
                            onAdd(alarmDate+ "T" + alarmTime, activityType, hour.toInt(), isRecurring)
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

        val time = alarm.alarmTime.substring(0, 10) + " " + alarm.alarmTime.substring(11)
        Text(text = "时间： $time ")
        if (alarm.isRecurring) {
            Text(text = "频率： ${alarm.customIntervalInHours} h ")
        } else {
            Text(text = "频率： 不重复 ")
        }

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

suspend fun addAlarm(babyId: Int, alarmTime: String, activityType: String, hour: String) {
    var isRecurring: Boolean
    if (hour == "0") {
        isRecurring = false
    } else {
        isRecurring = true
    }

    val apiPath = "api/v1/alarms/set/$babyId"

    val requestBody = JSONObject().apply {
        put("activityType", activityType)
        put("alarmTime", alarmTime)
        put("customIntervalInHours", hour)
        put("isRecurring", isRecurring)
    }

    sendPostRequestWithRequest(apiPath, requestBody.toString())
}

fun fetchAlarms(sharedPreferences: SharedPreferences, babyId: Int) {
    val apiString = "api/v1/alarms/get/$babyId"
    CoroutineScope(Dispatchers.IO).launch {
        val response = sendGetRequest(apiString)
        try {
            println(response)
            val gson = Gson()

            val alarmList: List<AlarmResponse> =
                gson.fromJson(response, Array<AlarmResponse>::class.java).toList()

            val newAlarms =
                alarmList.map { tracking ->
                    Alarm(
                        id = tracking.id,
                        activityType = tracking.activityType,
                        alarmTime = tracking.alarmTime,
                        customIntervalInHours = tracking.customIntervalInHours,
                        isRecurring = tracking.recurring,
                        active = tracking.active
                    )
                }

            saveAlarms(sharedPreferences, newAlarms.toList())

        } catch (e: Exception) {
            println("Json error: $babyId, $response")
        }
    }

}
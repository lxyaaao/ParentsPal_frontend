package com.example.myapplication.activity.Main

import android.app.Activity
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import com.example.myapplication.activity.Me.ButtonWithTwoTexts
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.example.myapplication.utils.NetworkUtils.sendGetRequest
import com.example.myapplication.utils.NetworkUtils.sendPostRequestWithRequest
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

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

class AlarmActivity : AppCompatActivity() {
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

    var showDatePickerDialog by remember { mutableStateOf(false) }
    var showTimePickerDialog by remember { mutableStateOf(false) }
    var activityClick by remember { mutableStateOf(false) }
    var hourClick by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("添加闹钟") },
        text = {
            Column {
                ButtonWithTwoTexts(
                    leftText = "输入日期",
                    rightText = alarmDate,
                    onClick = { showDatePickerDialog = true },
                    color = false
                )

                Divider(color = Color.LightGray, thickness = 1.dp)

                ButtonWithTwoTexts(
                    leftText = "输入时间",
                    rightText = alarmTime,
                    onClick = { showTimePickerDialog = true },
                    color = false
                )

                Divider(color = Color.LightGray, thickness = 1.dp)

                ButtonWithTwoTexts(
                    leftText = "输入事件",
                    rightText = activityType,
                    onClick = { activityClick = true },
                    color = false
                )

                Divider(color = Color.LightGray, thickness = 1.dp)

                ButtonWithTwoTexts(
                    leftText = "输入循环时间",
                    rightText = hour,
                    onClick = { hourClick = true },
                    color = false
                )

                Divider(color = Color.LightGray, thickness = 1.dp)
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

    if (showDatePickerDialog) {
        ShowDatePicker(
            onDateSelected = { selectedDate ->
                alarmDate = selectedDate
            }
        )
        showDatePickerDialog = false
    }

    if (showTimePickerDialog) {
        ShowTimePickerDialog(
            onTimeSelected = { selectedTime ->
                alarmTime = selectedTime
            }
        )
        showTimePickerDialog = false
    }

    if (activityClick) {
        AlertDialog(
            onDismissRequest = { onDismiss() },
            title = { Text(text = "输入事件") },
            text = {
                TextField(
                    value = activityType,
                    onValueChange = { activityType = it },
                    placeholder = { Text(text = "输入事件", color = Color.Gray) }
                )
            },
            confirmButton = {
                Button(onClick = { activityClick = false }) {
                    Text(text = "确定")
                }
            },
            dismissButton = {
                Button(onClick = { activityClick = false }) {
                    Text(text = "取消")
                }
            }
        )
    }

    if (hourClick) {
        AlertDialog(
            onDismissRequest = { onDismiss() },
            title = { Text(text = "输入循环时间") },
            text = {
                TextField(
                    value = hour,
                    onValueChange = { hour = it },
                    placeholder = { Text(text = "输入循环时间", color = Color.Gray) }
                )
            },
            confirmButton = {
                Button(onClick = { hourClick = false }) {
                    Text(text = "确定")
                }
            },
            dismissButton = {
                Button(onClick = { hourClick = false }) {
                    Text(text = "取消")
                }
            }
        )
    }
}

@Composable
fun ShowDatePicker(onDateSelected: (String) -> Unit) {
    val datePicker = MaterialDatePicker.Builder.datePicker()
        .setTitleText("选择日期")
        .build()

    val context = LocalContext.current
    val fragmentActivity = context as? FragmentActivity

    fragmentActivity?.let { activity ->
        val fragmentManager = activity.supportFragmentManager
        val fragmentTag = "DATE_PICKER"

        // Avoid showing multiple DatePickers at the same time
        val existingFragment = fragmentManager.findFragmentByTag(fragmentTag)
        if (existingFragment == null) {
            datePicker.show(fragmentManager, fragmentTag)
        }
    }

    // Set a listener to update date when a date is picked
    datePicker.addOnPositiveButtonClickListener { selection ->
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        onDateSelected(sdf.format(Date(selection)))
    }
}

@Composable
fun ShowTimePickerDialog(onTimeSelected: (String) -> Unit) {
    val context = LocalContext.current
    val currentTime = remember { Calendar.getInstance() }

    val hour = currentTime.get(Calendar.HOUR_OF_DAY)
    val minute = currentTime.get(Calendar.MINUTE)

    val timePickerDialog = TimePickerDialog(
        context,
        { _, selectedHour, selectedMinute ->
            val selectedTime = String.format("%02d:%02d", selectedHour, selectedMinute)
            onTimeSelected("$selectedTime:00")
        },
        hour,
        minute,
        true
    )

    timePickerDialog.show()
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

    val apiPath = "api/alarms/set/$babyId"

    val requestBody = JSONObject().apply {
        put("activityType", activityType)
        put("alarmTime", alarmTime)
        put("customIntervalInHours", hour)
        put("isRecurring", isRecurring)
    }

    sendPostRequestWithRequest(apiPath, requestBody.toString())
}

fun fetchAlarms(sharedPreferences: SharedPreferences, babyId: Int) {
    val apiString = "api/alarms/get/$babyId"
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
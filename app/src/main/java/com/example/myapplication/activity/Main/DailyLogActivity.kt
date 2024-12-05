package com.example.myapplication.activity.Main

import android.app.Activity
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
import com.example.myapplication.api.Baby
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
import java.util.Date
import java.util.Locale

data class GrowthTracking(
    val id: Long,
    val height: Double,
    val weight: Double,
    val measurementDate: String,
    val baby: Baby
)

class DailyLogActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                DailyLogScreen(this)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DailyLogScreen(activity: Activity) {
    var backFlag by remember { mutableStateOf(false) }
    var addClick by remember { mutableStateOf(false) }

    val sharedPreferences: SharedPreferences =
        activity.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)
    val babyId: Int = sharedPreferences.getInt("babyId", 0)
    var checkIns by remember { mutableStateOf(loadCheckIns(sharedPreferences)) }


    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                ),
                title = {
                    Text(
                        "打卡记录",
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
                    items(checkIns.reversed()) { checkIn ->
                        CheckInCard(checkIn) {
                            // 删除打卡记录
                            checkIns = checkIns.filter { it != checkIn }
                            saveCheckIns(sharedPreferences, checkIns) // 更新存储
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
        AddCheckInDialog(activity,
            onDismiss = { addClick = false },
            onAdd = { date, height, weight ->
                val newCheckIn = CheckIn(date, height, weight)
                checkIns = checkIns + newCheckIn // 添加新记录
                saveCheckIns(sharedPreferences, checkIns) // 更新存储
                fetchGrowthTracking(activity, sharedPreferences, babyId)
            })
        }

}

@Composable
fun AddCheckInDialog(activity: Activity, onDismiss: () -> Unit, onAdd: (String, String, String) -> Unit) {
    val sharedPreferences: SharedPreferences =
        activity.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)
    val babyId: Int = sharedPreferences.getInt("babyId", 0)
    var date by remember { mutableStateOf("") }
    var height by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }

    var heightClick by remember { mutableStateOf(false) }
    var weightClick by remember { mutableStateOf(false) }
    var showDatePickerDialog by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("添加打卡记录") },
        text = {
            Column {
                ButtonWithTwoTexts(
                    leftText = "输入日期",
                    rightText = date,
                    onClick = { showDatePickerDialog = true },
                    color = false
                )

                Divider(color = Color.LightGray, thickness = 1.dp)

                ButtonWithTwoTexts(
                    leftText = "输入身高(cm)",
                    rightText = height,
                    onClick = { heightClick = true },
                    color = false
                )

                Divider(color = Color.LightGray, thickness = 1.dp)

                ButtonWithTwoTexts(
                    leftText = "输入体重(kg)",
                    rightText = weight,
                    onClick = { weightClick = true },
                    color = false
                )

                Divider(color = Color.LightGray, thickness = 1.dp)
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (date.length!= 10 ||  date[4] != '-' || date[7] != '-') {
                        date = ""
                    }
                    CoroutineScope(Dispatchers.Main).launch {
                        if (date.isNotBlank() && height.isNotBlank() && weight.isNotBlank()) {
                            addCheckin(babyId, date, height, weight)
                            onAdd(date, height, weight) // 调用添加函数
                            onDismiss() // 关闭对话框
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

    @Composable
    fun showDatePicker() {
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
            date = sdf.format(Date(selection)) // Update date state
        }
    }

    if (showDatePickerDialog) {
        showDatePicker()
        showDatePickerDialog = false // Reset the trigger state after showing the date picker
    }

    if (heightClick) {
        AlertDialog(
            onDismissRequest = { onDismiss() },
            title = { Text(text = "输入身高") },
            text = {
                TextField(
                    value = height,
                    onValueChange = { height = it },
                    placeholder = { Text(text = "输入身高", color = Color.Gray) }
                )
            },
            confirmButton = {
                Button(onClick = { heightClick = false }) {
                    Text(text = "确定")
                }
            },
            dismissButton = {
                Button(onClick = { heightClick = false }) {
                    Text(text = "取消")
                }
            }
        )
    }

    if (weightClick) {
        AlertDialog(
            onDismissRequest = { onDismiss() },
            title = { Text(text = "输入体重") },
            text = {
                TextField(
                    value = weight,
                    onValueChange = { weight = it },
                    placeholder = { Text(text = "输入体重", color = Color.Gray) }
                )
            },
            confirmButton = {
                Button(onClick = { weightClick = false }) {
                    Text(text = "确定")
                }
            },
            dismissButton = {
                Button(onClick = { weightClick = false }) {
                    Text(text = "取消")
                }
            }
        )
    }
}


@Composable
fun CheckInCard(checkIn: CheckIn, onDelete: () -> Unit) {
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
            Text(text = checkIn.date)
            IconButton(onClick = { showDialog = true }) {
                Icon(Icons.Default.Delete, contentDescription = "删除打卡记录")
            }
        }
        Text(text = "身高： ${checkIn.height} cm")
        Text(text = "体重： ${checkIn.weight} kg")
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("确认删除") },
            text = { Text("您确定要删除这个打卡记录吗？") },
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

fun fetchGrowthTracking(activity: Activity, sharedPreferences: SharedPreferences, babyId: Int) {
    val apiString = "api/v1/babies/$babyId/growth"
    CoroutineScope(Dispatchers.IO).launch {
        val response = sendGetRequest(apiString)
        try {
            val gson = Gson()

            val growthTrackingList: List<GrowthTracking> =
                gson.fromJson(response, Array<GrowthTracking>::class.java).toList()

            val newCheckIns =
                growthTrackingList.map { tracking ->
                    CheckIn(
                        date = tracking.measurementDate.toString(),
                        height = tracking.height.toString(),
                        weight = tracking.weight.toString()
                    )
                }

            saveCheckIns(sharedPreferences, newCheckIns.toList())

        } catch (e: Exception) {
            println("Json error: $response")
        }
    }

}

fun loadCheckIns(sharedPreferences: SharedPreferences): List<CheckIn> {
    val json = sharedPreferences.getString("checkins", "[]") ?: "[]"
    return Gson().fromJson(json, Array<CheckIn>::class.java).toList()
}

fun saveCheckIns(sharedPreferences: SharedPreferences, checkIns: List<CheckIn>) {
    val editor = sharedPreferences.edit()
    val json = Gson().toJson(checkIns)
    editor.putString("checkins", json).apply()
}

data class CheckIn(val date: String, val height: String, val weight: String)

suspend fun addCheckin(babyId: Int, date: String, height: String, weight: String) {
    val apiPath = "api/v1/babies/$babyId/growth"

    val requestBody = JSONObject().apply {
        put("weight", weight)
        put("height", height)
        put("measurementDate", date)
    }

    sendPostRequestWithRequest(apiPath, requestBody.toString())
}
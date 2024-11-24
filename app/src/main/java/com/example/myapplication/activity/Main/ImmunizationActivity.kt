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
import com.example.myapplication.utils.sendDeleteRequest
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONException
import org.json.JSONObject
import java.time.LocalDate

data class Immunization(
    val id: Int,
    val vaccineName: String,
    val dateGiven: String,
    val nextDue: String
)

data class ImmunizationResponse(
    val id: Int,
    val babyId: Int,
    val vaccineName: String,
    val dateGiven: String,
    val nextDue: String
)

class ImmunizationActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                ImmunizationScreen(this)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ImmunizationScreen(activity: Activity) {
    var backFlag by remember { mutableStateOf(false) }
    var addClick by remember { mutableStateOf(false) }

    val sharedPreferences: SharedPreferences =
        activity.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)
    val babyId: Int = sharedPreferences.getInt("babyId", 0)
    var immunizations by remember { mutableStateOf(loadImmunizations(sharedPreferences)) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                ),
                title = {
                    Text(
                        "疫苗提醒",
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
                    items(immunizations.reversed()) { immunization ->
                        ImmunizationCard(immunization) {
                            immunizations = immunizations.filter { it != immunization }
                            saveImmunizations(sharedPreferences, immunizations) // 更新存储
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
        AddImmunizationDialog(activity,
            onDismiss = { addClick = false },
            onAdd = { vaccine, date, due ->
                val newImmunization = Immunization(0, vaccine, date, due)
                immunizations = immunizations + newImmunization
                saveImmunizations(sharedPreferences, immunizations)
                fetchImmunizations(sharedPreferences, babyId)
            })
    }

}

@Composable
fun AddImmunizationDialog(activity: Activity, onDismiss: () -> Unit, onAdd: (String, String, String) -> Unit) {
    val sharedPreferences: SharedPreferences =
        activity.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)
    val babyId: Int = sharedPreferences.getInt("babyId", 0)
    var vaccine by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }
    var due by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("添加疫苗") },
        text = {
            Column {
                TextField(
                    value = vaccine,
                    onValueChange = { vaccine = it },
                    label = { Text("输入疫苗名字") }
                )
                TextField(
                    value = date,
                    onValueChange = { date = it },
                    label = { Text("输入疫苗时间，格式如0000-00-00") }
                )
                TextField(
                    value = due,
                    onValueChange = { due = it },
                    label = { Text("输入有效时间，格式如0000-00-00") }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (date.length!= 10 ||  date[4] != '-' || date[7] != '-') {
                        date = ""
                    }
                    if (due.length!= 10 ||  due[4] != '-' || due[7] != '-') {
                        due = ""
                    }
                    CoroutineScope(Dispatchers.Main).launch {
                        if (vaccine.isNotBlank() && date.isNotBlank() && due.isNotBlank()  ) {
                            val apiPath = "api/immunizations/$babyId/add"

                            val requestBody = JSONObject().apply {
                                put("vaccineName", vaccine)
                                put("dateGiven", date)
                                put("nextDue", due)
                            }

                            sendPostRequestWithRequest(apiPath, requestBody.toString())

                            onAdd(vaccine, date, due)
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
fun ImmunizationCard(immunization: Immunization, onDelete: () -> Unit) {
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
            Text(text = immunization.vaccineName)
            IconButton(onClick = { showDialog = true }) {
                Icon(Icons.Default.Delete, contentDescription = "删除")
            }
        }
        Text(text = "注射时间： ${immunization.dateGiven} ")
        Text(text = "起效时间： ${immunization.nextDue} 前")
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("确认删除") },
            text = { Text("您确定要删除这个疫苗提醒吗？") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete()

                        println(immunization.id)
                        val apiString = "api/immunizations/${immunization.id}"
                        CoroutineScope(Dispatchers.IO).launch {
                            sendDeleteRequest(apiString)
                        }

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

fun loadImmunizations(sharedPreferences: SharedPreferences): List<Immunization> {
    val json = sharedPreferences.getString("immunizations", "[]") ?: "[]"
    return Gson().fromJson(json, Array<Immunization>::class.java).toList()
}

fun saveImmunizations(sharedPreferences: SharedPreferences, immunizations: List<Immunization>) {
    val editor = sharedPreferences.edit()
    val json = Gson().toJson(immunizations)
    editor.putString("immunizations", json).apply()
}

fun fetchImmunizations(sharedPreferences: SharedPreferences, babyId: Int) {
    val apiString = "api/immunizations/$babyId"
    CoroutineScope(Dispatchers.IO).launch {
        val response = sendGetRequest(apiString)
        try {
            println(response)
            val gson = Gson()

            val immunizationList: List<ImmunizationResponse> =
                gson.fromJson(response, Array<ImmunizationResponse>::class.java).toList()

            val newImmunizations =
                immunizationList.map { tracking ->
                    Immunization(
                        id = tracking.id,
                        vaccineName = tracking.vaccineName,
                        dateGiven = tracking.dateGiven,
                        nextDue = tracking.nextDue
                    )
                }

            saveImmunizations(sharedPreferences, newImmunizations.toList())

        } catch (e: Exception) {
            println("Json error: $babyId, $response")
        }
    }

}
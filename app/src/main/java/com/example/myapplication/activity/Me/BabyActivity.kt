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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.myapplication.api.Baby
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.example.myapplication.utils.NetworkUtils.sendPostRequestWithRequest
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.util.Date
import java.util.Locale

class BabyActivity : AppCompatActivity() {
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

    var name by remember { mutableStateOf(sharedPreferences.getString("babyName", "") ?: "") }
    var babyGender by remember { mutableStateOf(sharedPreferences.getString("babyGender", "") ?: "") }
    var babyBirth by remember { mutableStateOf(sharedPreferences.getString("babyBirthdate", "") ?: "") }
    var babyId by remember { mutableStateOf(sharedPreferences.getInt("babyId", 0)) }
    var date by remember { mutableStateOf(sharedPreferences.getString("babyBirthdate", "") ?: "") }

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
                    rightText = sharedPreferences.getString("babyName", "") ?: "",
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
                    rightText = date,
                    onClick = { birthClick = true }
                )
            }
        }
    )

    LaunchedEffect(backFlag) {
        if (backFlag) {
            val parentId = sharedPreferences.getInt("parentId", 0)
            babyId = sharedPreferences.getInt("babyId", 0)
            name = sharedPreferences.getString("babyName", "") ?: ""
            babyBirth = sharedPreferences.getString("babyBirthdate", "") ?: ""
            babyGender = sharedPreferences.getString("babyGender", "") ?: ""

            if (babyId == 0 && name != "" && babyBirth != "") {
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
        val datePicker = remember {
            MaterialDatePicker.Builder.datePicker()
                .setTitleText("选择生日")
                .build()
        }

        val fragmentActivity = activity as? androidx.fragment.app.FragmentActivity
        if (fragmentActivity != null) {
            val fragmentManager = fragmentActivity.supportFragmentManager
            val fragmentTag = "DATE_PICKER"

            // 避免重复显示日历
            val existingFragment = fragmentManager.findFragmentByTag(fragmentTag)

            if (existingFragment == null) {
                datePicker.show(fragmentManager, fragmentTag)
            }
        } else {
            println("Activity is not a FragmentActivity.")
        }

        datePicker.addOnPositiveButtonClickListener { selection ->
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val selectedDate = sdf.format(Date(selection))
            date = selectedDate

            val editor = sharedPreferences.edit()
            editor.putString("babyBirthdate", date)
            editor.apply()
        }

        birthClick = false
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
    editor.putInt("babyId", response.id.toInt()).apply()

}

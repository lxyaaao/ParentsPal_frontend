package com.example.myapplication.activity.AIQA

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.myapplication.R
import com.example.myapplication.activity.Me.MeActivity
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.example.myapplication.utils.NetworkUtils.sendGetRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONException
import org.json.JSONObject

class AIHistoryActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApplicationTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    val selectedTabIndex = intent.getIntExtra("selectedTabIndex", 0)
                    AIHistoryScreen(this, selectedTabIndex)
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AIHistoryScreen(activity: Activity, selectedTabIndex: Int) {
    var backFlag by remember { mutableStateOf(false) }
    var selectedTabIndex by remember { mutableStateOf(selectedTabIndex) }
    val sharedPreferences: SharedPreferences =
        activity.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)
    var myName by remember { mutableStateOf(sharedPreferences.getString("name", "宝宝名字") ?: "宝宝名字") }
    var conversations by remember { mutableStateOf<List<Conversation>>(emptyList()) }

    LaunchedEffect(Unit) {
        conversations = FollowingUserList(myName)
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                ),
                title = {
                    Text(
                        "历史对话",
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
        }
    ) { paddingValues->
        Scaffold(
            topBar = {
//                TabRow(
//                    selectedTabIndex = selectedTabIndex,
//                    containerColor = MaterialTheme.colorScheme.primaryContainer
//                ) {
//                    val tabTitles = listOf("关注", "粉丝")
//                    tabTitles.forEachIndexed { index, title ->
//                        Tab(
//                            selected = selectedTabIndex == index,
//                            onClick = {
//                                selectedTabIndex = index
//                            },
//                            text = { Text(title) }
//                        )
//                    }
//                }
            },
            modifier = Modifier.padding(paddingValues)
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(paddingValues)
            ) {
                when (selectedTabIndex) {
                    0 -> {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp)
                        ) {
                            items(conversations) { user ->
                                UserListItem(conversation = user, activity)
                            }
                        }
                    }
                    1 -> {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp)
                        ) {
                            items(conversations) { user ->
                                UserListItem(conversation = user, activity)
                            }
                        }
                    }
                }
            }
        }
    }

    if (backFlag) {
        val intent = Intent(activity, QAActivity::class.java)
        activity.startActivity(intent)
        activity.overridePendingTransition(0, 0)
        activity.finish()
    }
}

@Composable
fun UserListItem(conversation: Conversation, activity: Activity) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .border(BorderStroke(1.dp, Color.LightGray)) // 边框
            .clickable {
                val intent = Intent(activity, QAActivity::class.java)
                intent.putExtra("type", "ai")
                intent.putExtra("username", conversation.conv_id)
                activity.startActivity(intent)
                activity.overridePendingTransition(0, 0)
                activity.finish()
            } // Handle click
    ) {
        Row(
            modifier = Modifier
                .fillMaxHeight()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = conversation.avatarResId),
                contentDescription = "Avatar",
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = conversation.conv_name, color = Color.Black)
        }

        Divider(color = Color.LightGray, thickness = 1.dp)
    }
}

fun parseJsonResponse(jsonResponse: String): List<Pair<String, String>> {
    val jsonObject = JSONObject(jsonResponse)
    val dataArray = jsonObject.getJSONArray("data")
    val result = mutableListOf<Pair<String, String>>()

    for (i in 0 until dataArray.length()) {
        val dataObject = dataArray.getJSONObject(i)
        val convId = dataObject.getString("conv_id")
        val convName = dataObject.getString("conv_name")
        result.add(Pair(convId, convName))
    }

    return result
}



@SuppressLint("NewApi")
suspend fun FollowingUserList(myName: String): List<Conversation> {
    return withContext(Dispatchers.IO) {
        // Simulate network call
        val apiPath = "api/ai_conversations/get-conv?username=${myName}"
        val response = sendGetRequest(apiPath)
        Log.d("response", response)
        var list: List<Pair<String, String>> = emptyList<Pair<String, String>>()
        try {
            list = parseJsonResponse(response)
        } catch (e: JSONException) {
            e.printStackTrace()
            list
        }
        list.map { Conversation(avatarResId = R.drawable.baseline_account_circle_24, conv_id = it.first, conv_name = it.second) }
    }
}

data class Conversation(
    val avatarResId: Int,
    val conv_id: String,
    val conv_name: String
)

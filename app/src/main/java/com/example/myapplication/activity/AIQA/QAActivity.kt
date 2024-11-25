package com.example.myapplication.activity.AIQA

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.activity.Main.MainActivity
import com.example.myapplication.activity.Me.UserListActivity
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.example.myapplication.utils.NetworkUtils
import com.example.myapplication.utils.NetworkUtils.sendAIRequest
import com.example.myapplication.utils.NetworkUtils.sendPostRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


object ConversationInfo {
    var conversationType: String? = null
    var userName: String? = null
}


class QAActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        ConversationInfo.conversationType = intent.getStringExtra("type")
        ConversationInfo.userName = intent.getStringExtra("username")
//        ConversationInfo.userName = "Alice"
        setContent {
            MyApplicationTheme {
                QAScreen(this)
            }
        }

    }
}


fun isUserConversation(): Boolean {
    return ConversationInfo.conversationType == "user" && ConversationInfo.userName != null
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun QAScreen(activity: Activity) {
//    val navController = rememberNavController()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())
    Scaffold(
        modifier = Modifier
            .imePadding()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            CenterAlignedTopAppBar(
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                ),
                title = {
                    Text(
                        if (isUserConversation()) ConversationInfo.userName!! else "AI",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    BackToMainButton(activity)
                },
                actions = {
                    IconButton(onClick = { /* do something */ }) {
                        Icon(
                            imageVector = Icons.Filled.Menu,
                            contentDescription = "Localized description"
                        )
                    }
                },
                scrollBehavior = scrollBehavior,
            )
        },
    ) { innerPadding ->
        ConversationScreen(activity)
        print("innerPadding: $innerPadding")
//        ScrollContent(innerPadding)
//        NavHost(
//            navController = navController,
//            startDestination = NavItem.QuestionAnswer.route,
//            modifier = Modifier.padding(innerPadding)
//        ) {
//            composable(NavItem.Home.route) { HomeScreen(activity) }
//            composable(NavItem.Share.route) { ShareScreen(activity) }
//            composable(NavItem.QuestionAnswer.route) { ConversationScreen(activity) }
//            composable(NavItem.Person.route) { PersonScreen(activity) }
//        }
    }
}


@Composable
fun BackToMainButton(activity: Activity) {
//    val context = LocalContext.current
    IconButton(onClick = {
        val intent = if (isUserConversation()) Intent(activity, UserListActivity::class.java) else Intent(activity, MainActivity::class.java)
        activity.startActivity(intent)
        activity.overridePendingTransition(0, 0)
        activity.finish()
    }) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "Localized description"
        )
    }
}


data class ConversationItem(val userName: String, val lastMessage: String, val timestamp: String)

@Composable
fun ConversationCard(item: ConversationItem, myName: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .background(
                color = if (item.userName == myName) MaterialTheme.colorScheme.primaryContainer else Color.White,
                shape = RoundedCornerShape(8.dp)
            )
            .clickable { /* Handle click */ }
            .padding(16.dp)

    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text = item.userName,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.primary,
                modifier = if (item.userName == myName) Modifier.align(Alignment.End) else Modifier
            )
            Text(
                text = item.lastMessage,
                fontSize = 14.sp,
                color = Color.Gray,
//                modifier = if (item.userName == "User") Modifier.align(Alignment.End) else Modifier
            )
            Text(
                text = item.timestamp,
                fontSize = 12.sp,
                color = Color.LightGray,
                modifier = if (item.userName == myName) Modifier.align(Alignment.End) else Modifier
            )
        }
    }
}

@Composable
fun ConversationList(conversations: List<ConversationItem>, listState: LazyListState , myName: String) {

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        conversations.forEach { conversation ->
            item {
                ConversationCard(item = conversation, myName = myName)
            }
        }

    }
}

fun parseConversationHistory(jsonResponse: String): List<ConversationItem> {
    val conversationItems = mutableListOf<ConversationItem>()
    val jsonObject = JSONObject(jsonResponse)
    val dataArray: JSONArray = jsonObject.getJSONArray("data")

    for (i in 0 until dataArray.length()) {
        val messageObject = dataArray.getJSONObject(i)
        val senderName = messageObject.getString("sender_name")
        val content = messageObject.getString("content")
        val createdAt = messageObject.getString("created_at")
        conversationItems.add(ConversationItem(senderName, content, createdAt))
    }

    return conversationItems.reversed()
}

suspend fun loadConversationHistory(username1: String, username2: String): List<ConversationItem> {
    return withContext(Dispatchers.IO) {
        val apiString =
            "api/conversations/messages-between-users?username1=$username1&username2=$username2"
        val response = NetworkUtils.sendGetRequest(apiString)
        Log.d("response", response)
        try {
            parseConversationHistory(response)
        } catch (e: JSONException) {
            e.printStackTrace()
            emptyList()
        }
    }
}

@Composable
private fun ConversationScreen(activity: Activity) {
    val textState = remember { mutableStateOf("") }
    val currentText = remember { mutableStateOf("") }
    val conversations = remember { mutableStateOf(listOf<ConversationItem>()) }
    val listState = rememberLazyListState()
    val conversationId = remember { mutableStateOf("") }
    val sharedPreferences: SharedPreferences =
        activity.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)
    var myName by remember { mutableStateOf(sharedPreferences.getString("name", "宝宝名字") ?: "宝宝名字") }


    LaunchedEffect(Unit) {
        if (isUserConversation()) {
            while (true){
                val history = loadConversationHistory(myName, ConversationInfo.userName!!)
                conversations.value = history
                delay(1000)
            }
        }
    }

    val conversationName = if (isUserConversation()) ConversationInfo.userName!! else "Bot"

    LaunchedEffect(conversations.value.size) {
        listState.animateScrollToItem(conversations.value.size)
    }

    Box(modifier = Modifier.padding(top = 96.dp)) {
//        Spacer(modifier = Modifier.height(64.dp))
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 64.dp)
        ) {
            ConversationList(conversations = conversations.value, listState = listState, myName)
        }
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            TextField(
                value = textState.value,
                onValueChange = { textState.value = it },
                label = { Text("Enter text") },
                shape = RoundedCornerShape(50), // Fully rounded corners
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
                trailingIcon = {
                    IconButton(onClick = {
                        currentText.value = textState.value
                        textState.value = ""
                        if(isUserConversation()){
                            conversations.value = conversations.value + ConversationItem(
                                myName,
                                currentText.value,
                                "发送中"
                            )
                        }
                        else{
                            conversations.value = conversations.value + ConversationItem(
                                myName,
                                currentText.value,
                                SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
                            )
                        }


                        CoroutineScope(Dispatchers.Main).launch {
//                            val response = sendGetRequest("http://www.baidu.com")
                            if (isUserConversation()) {
                                val apiString =
                                    "api/conversations/message?senderUsername=${myName}&receiverUsername=${conversationName}&content=${currentText.value}"
                                val response =
                                    sendPostRequest(apiString)

                            } else {
                                val response =
                                    sendAIRequest(currentText.value, conversationId.value)
                                Log.d("response", response)
                                val jsonResponse = try {
                                    JSONObject(response)
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                    JSONObject()
                                }
                                conversationId.value = jsonResponse.optString("conversation_id", "")
                                val responseText = jsonResponse.optString("answer", "")
                                // Handle the response here
                                conversations.value = conversations.value + ConversationItem(
                                    conversationName,
                                    responseText,
                                    SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
                                )
                            }
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "Localized description"
                        )
                    }
                }
            )
        }
    }

}


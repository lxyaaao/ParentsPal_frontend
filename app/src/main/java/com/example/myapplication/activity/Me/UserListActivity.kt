package com.example.myapplication.activity.Me

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.myapplication.R
import com.example.myapplication.activity.AIQA.QAActivity
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.example.myapplication.utils.NetworkUtils.sendGetRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.File

class UserListActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val selectedTabIndex = intent.getIntExtra("selectedTabIndex", 0)
                UserListScreen(this, selectedTabIndex)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UserListScreen(activity: Activity, selectedTabIndex: Int) {
    var backFlag by remember { mutableStateOf(false) }
    var selectedTabIndex by remember { mutableStateOf(selectedTabIndex) }
    val sharedPreferences: SharedPreferences =
        activity.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)
    var myName by remember { mutableStateOf(sharedPreferences.getString("name", "宝宝名字") ?: "宝宝名字") }
    var users by remember { mutableStateOf<List<User>>(emptyList()) }

    LaunchedEffect(Unit) {
        users = FollowingUserList(myName)
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
                        "列表",
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
        Column(modifier = Modifier
            .fillMaxWidth()
            .padding(paddingValues)
        ) {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                items(users) { user ->
                    UserListItem(user = user, activity)
                }
            }
        }
    }

    if (backFlag) {
        val intent = Intent(activity, MeActivity::class.java)
        activity.startActivity(intent)
        activity.finish()
    }
}

@Composable
fun UserListItem(user: User, activity: Activity) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .border(BorderStroke(1.dp, Color.LightGray)) // 边框
            .clickable {
                val intent = Intent(activity, QAActivity::class.java)
                intent.putExtra("type", "user")
                intent.putExtra("username", user.username)
                activity.startActivity(intent)
                activity.finish()
            } // Handle click
    ) {
        Row(
            modifier = Modifier
                .fillMaxHeight()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val sharedPreferences: SharedPreferences =
                activity.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)

            val context = LocalContext.current
            //TODO: get userid
            val userId = sharedPreferences.getInt("parentId", 0)

            var localImagePath by remember { mutableStateOf<String?>(null) }
            val file = File(context.cacheDir, "downloaded_image_$userId.jpg")
            localImagePath = file.absolutePath

            if (localImagePath != null) {
                val imageBitmap = remember(localImagePath) {
                    BitmapFactory.decodeFile(localImagePath)?.asImageBitmap()
                }

                if (imageBitmap != null) {
                    Image(
                        bitmap = imageBitmap,
                        contentDescription = "Avatar",
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                    )
                } else {
                    val resourceName = "photo${userId % 12 + 1}" // 动态的资源名称
                    val resId = context.resources.getIdentifier(resourceName, "drawable", context.packageName)

                    Image(
                        painter = painterResource(id = resId),
                        contentDescription = "Avatar",
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                    )
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = user.username, color = Color.Black)
        }

        Divider(color = Color.LightGray, thickness = 1.dp)
    }
}

fun parseConversationList(response: String): List<String> {
    val jsonObject = JSONObject(response)
    val dataArray: JSONArray = jsonObject.getJSONArray("data")
    val namesSet = mutableSetOf<String>()

    for (i in 0 until dataArray.length()) {
        val messageObject = dataArray.getJSONObject(i)
        val senderName = messageObject.getString("sender_name")
        val receiverName = messageObject.getString("receiver_name")
        namesSet.add(senderName)
        namesSet.add(receiverName)
    }

    return namesSet.toList()
}



@SuppressLint("NewApi")
suspend fun FollowingUserList(myName: String): List<User> {
    return withContext(Dispatchers.IO) {
        // Simulate network call
        val apiPath = "api/conversations/latest-messages?username=${myName}"
        val response = sendGetRequest(apiPath)
        Log.d("response", response)
        var list = emptyList<String>()
        try {
            list = parseConversationList(response)
        } catch (e: JSONException) {
            e.printStackTrace()
            list
        }
        list.map { User(avatarResId = R.drawable.photo1, username = it) }
    }
}

data class User(val avatarResId: Int, val username: String)
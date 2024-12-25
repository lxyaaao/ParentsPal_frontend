package com.example.myapplication.activity.Blog

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.myapplication.activity.Main.RegisterResponse
import com.example.myapplication.activity.Main.saveUser
import com.example.myapplication.activity.Me.getNameFromSharedPreferences
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.example.myapplication.utils.NetworkUtils.sendGetRequest
import com.example.myapplication.utils.NetworkUtils.sendPostRequestWithRequest
import com.example.myapplication.utils.sendPutRequest
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class BlogAddActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                BlogAddScreen(this)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BlogAddScreen(activity: Activity) {
    val sharedPreferences: SharedPreferences =
        activity.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)
    var selectedTabIndex by remember { mutableIntStateOf(sharedPreferences.getInt("selectedTabIndex", 0)) }
    var checkClick by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {

            CenterAlignedTopAppBar(
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                ),
                title = {
                    Text(
                        "",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        val articleId: Int = sharedPreferences.getInt("articleId", 0)

                        if (articleId == 0) {
                            val editor = sharedPreferences.edit()
                            editor.putBoolean("refreshArticleState", true)
                            editor.apply()

                            val intent = Intent(activity, BlogMineActivity::class.java)
                            activity.startActivity(intent)
                            activity.finish()
                        } else {
                            val intent = Intent(activity, MyBlogCheckActivity::class.java)
                            activity.startActivity(intent)
                            activity.finish()
                        }
                    }
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { checkClick = true }) {
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = "添加"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Scaffold(
            topBar = {
                TabRow(
                    selectedTabIndex = selectedTabIndex,
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ) {
                    val tabTitles = listOf("分享", "问答")
                    tabTitles.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTabIndex == index,
                            onClick = {
                                selectedTabIndex = index
                            },
                            text = { Text(title) }
                        )
                    }
                }
            },
            modifier = Modifier.padding(paddingValues)
        ) {
                paddingValuesIn ->
            Column(modifier = Modifier.padding(paddingValuesIn)) {
                when (selectedTabIndex) {
                    0 -> addArticle(activity, checkClick, 0)
                    1 -> addArticle(activity, checkClick, 1)
                }
            }
        }


    }
}

@Composable
fun addArticle(activity: Activity, checkClick: Boolean, number: Int): Boolean {
    val sharedPreferences: SharedPreferences =
        activity.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)

    val parentId = sharedPreferences.getInt("parentId", 0)
    var content by remember { mutableStateOf("") }
    var title by remember { mutableStateOf("") }

    val articleId: Int = sharedPreferences.getInt("articleId", 0)

    val urlPart = if (number == 0) {
        "article"
    } else {
        "qna"
    }

    if (articleId != 0 ) {
        LaunchedEffect(articleId) {
            val apiString = "api/$urlPart/$articleId"
            val response = sendGetRequest(apiString)
            try {
                println(response)
                val gson = Gson()
                val apiResponse = gson.fromJson(response, CheckArticleResponse::class.java)
                content = apiResponse.data.content
                title = apiResponse.data.title
            } catch (e: Exception) {
                println("Json error: $response")
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        TextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("输入标题") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color(0xFFF1E3E6),
                unfocusedContainerColor = Color(0xFFF1E3E6),
            ),
        )

        Spacer(modifier = Modifier.height(16.dp))

        TextField(
            value = content,
            onValueChange = { content = it },
            label = { Text("输入内容") },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 120.dp)
                .padding(bottom = 8.dp) ,
            colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFFF1E3E6),
                    unfocusedContainerColor = Color(0xFFF1E3E6),
                ),
        )

        Spacer(modifier = Modifier.height(16.dp))
    }

    if(checkClick) {
        LaunchedEffect(Unit) {
            if (articleId == 0) {
                try {
                    val apiPath = "api/$urlPart"

                    val requestBody = JSONObject().apply {
                        put("userId", parentId)
                        put("title", title)
                        put("content", content)
                    }

                    val responseString = sendPostRequestWithRequest(apiPath, requestBody.toString())

                    val gson = Gson()
                    val response = gson.fromJson(responseString, RegisterResponse::class.java)
                    println("Parsed response: $response")

                } catch (e: Exception) {
                    println("Json error")
                }

                val intent = Intent(activity, BlogMineActivity::class.java)
                activity.startActivity(intent)
                activity.finish()
            } else {
                try {
                    val apiPath = "api/$urlPart/update/${articleId}"

                    val requestBody = JSONObject().apply {
                        put("title", title)
                        put("content", content)
                    }

                    val responseString = sendPostRequestWithRequest(apiPath, requestBody.toString())

                    val gson = Gson()
                    val response = gson.fromJson(responseString, RegisterResponse::class.java)
                    println("Parsed response: $response")

                } catch (e: Exception) {
                    println("Json error")
                }

                val intent = Intent(activity, MyBlogCheckActivity::class.java)
                activity.startActivity(intent)
                activity.finish()
            }
        }
    }

    return false
}
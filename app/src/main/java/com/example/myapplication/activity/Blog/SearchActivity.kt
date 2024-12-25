package com.example.myapplication.activity.Blog

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.ui.theme.MyApplicationTheme

class SearchActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                SearchScreen(this)
            }
        }
    }
}

@Composable
fun SearchScreen(activity: Activity) {
    var searchQuery by remember { mutableStateOf("") }
    val sharedPreferences: SharedPreferences =
        activity.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)
    val editor = sharedPreferences.edit()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Spacer(modifier = Modifier.height(16.dp))

        IconButton(onClick = {
            val intent = Intent(activity, BlogActivity::class.java)
            activity.startActivity(intent)
            activity.finish()
        }) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
        }

        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {

            TextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text(
                    "输入搜索内容",
                    style = TextStyle(fontSize = 14.sp)
                ) },
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .height(48.dp)
                    .fillMaxWidth(0.85f)
                    .padding(start = 8.dp),
                textStyle = TextStyle(fontSize = 14.sp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFFF1E3E6),
                    unfocusedContainerColor = Color(0xFFF1E3E6),
                ),
            )

            IconButton(
                onClick = {
                    editor.putString("searchResult", searchQuery)
                    editor.apply()
                    if (searchQuery != "") {
                        val intent = Intent(activity, SearchResultActivity::class.java)
                        activity.startActivity(intent)
                        activity.finish()
                    }
                },
                modifier = Modifier.padding(start = 8.dp) // 左侧添加间距
            ) {
                Icon(Icons.Filled.Search,
                    contentDescription = "Search")
            }
        }
    }
}
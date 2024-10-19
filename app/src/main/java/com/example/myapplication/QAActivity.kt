package com.example.myapplication

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.ui.theme.MyApplicationTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class QAActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                QAScreen(this)
            }
        }

    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun QAScreen(activity: Activity) {
//    val navController = rememberNavController()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())
    Scaffold(
        modifier = Modifier.imePadding().nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            CenterAlignedTopAppBar(
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                ),
                title = {
                    Text(
                        "Q&A",
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
        val intent = Intent(activity, MainActivity::class.java)
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
fun ConversationCard(item: ConversationItem) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .background(
                color = if (item.userName == "User") MaterialTheme.colorScheme.primaryContainer else Color.White,
                shape = RoundedCornerShape(8.dp))
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
                modifier = if (item.userName == "User") Modifier.align(Alignment.End) else Modifier
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
                modifier = if (item.userName == "User") Modifier.align(Alignment.End) else Modifier
            )
        }
    }
}

@Composable
fun ConversationList(conversations: List<ConversationItem>, listState: LazyListState) {

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        conversations.forEach { conversation ->
            item {
                ConversationCard(item = conversation)
            }
        }

    }
}

@Composable
private fun ConversationScreen(activity: Activity) {
    val textState = remember { mutableStateOf("") }
    val currentText = remember { mutableStateOf("") }
    val conversations = remember { mutableStateOf(listOf<ConversationItem>()) }
    val listState = rememberLazyListState()

    LaunchedEffect(conversations.value.size) {
        listState.animateScrollToItem(conversations.value.size)
    }

    Box(modifier = Modifier.padding(top = 96.dp)) {
//        Spacer(modifier = Modifier.height(64.dp))
        Box(
            modifier = Modifier.fillMaxSize().padding(bottom = 64.dp)
        ) {
            ConversationList(conversations = conversations.value, listState = listState)
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
                        conversations.value = conversations.value + ConversationItem("User", currentText.value, SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date()))

                        Handler(Looper.getMainLooper()).postDelayed({
                            conversations.value = conversations.value + ConversationItem("Bot", currentText.value, SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date()))
                        }, (0..3000).random().toLong())
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


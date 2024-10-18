package com.example.myapplication

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.style.TextOverflow
import com.example.myapplication.ui.theme.MyApplicationTheme

class BlogActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                BlogScreen(this)
            }
        }
    }
}

@Composable
private fun BlogScreen(activity: Activity) {
//    val navController = rememberNavController()
//    Scaffold(
//        bottomBar = { BottomNavigationBar(navController) }
//    ) { innerPadding ->
//        CenterAlignedTopAppBarExample()
//        NavHost(
//            navController = navController,
//            startDestination = NavItem.Share.route,
//            modifier = Modifier.padding(innerPadding)
//        ) {
//            composable(NavItem.Home.route) { HomeScreen(activity) }
//            composable(NavItem.Share.route) { BlogMainScreen(activity) }
//            composable(NavItem.QuestionAnswer.route) { QuestionAnswerScreen(activity) }
//            composable(NavItem.Person.route) { PersonScreen(activity) }
//        }
//    }
}

private fun BlogMainScreen(activity: Activity) {

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CenterAlignedTopAppBarExample() {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),

        topBar = {
            CenterAlignedTopAppBar(
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                ),
                title = {
                    Text(
                        "Blog",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
//                navigationIcon = {
//                    FinishAffinityButton()
//                },
//                actions = {
//                    IconButton(onClick = { /* do something */ }) {
//                        Icon(
//                            imageVector = Icons.Filled.Menu,
//                            contentDescription = "Localized description"
//                        )
//                    }
//                },
                scrollBehavior = scrollBehavior,
            )
        },
    ) { innerPadding ->
        ScrollContent(innerPadding)
    }
}


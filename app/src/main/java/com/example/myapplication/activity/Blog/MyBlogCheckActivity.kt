package com.example.myapplication.activity.Blog

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.activity.AIQA.QAActivity
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.example.myapplication.utils.NetworkUtils.sendGetRequest
import com.example.myapplication.utils.NetworkUtils.sendPostRequestWithRequest
import com.example.myapplication.utils.sendDeleteRequest
import com.example.myapplication.utils.sendPutRequest
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.File

class MyBlogCheckActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MyBlogCheckScreen(this)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MyBlogCheckScreen(activity: Activity) {
    var backFlag by remember { mutableStateOf(false) }
    var editClick by remember { mutableStateOf(false) }
    var deleteClick by remember { mutableStateOf(false) }

    val sharedPreferences: SharedPreferences =
        activity.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)
    val articleId: Int = sharedPreferences.getInt("articleId", 0)
    val parentId = sharedPreferences.getInt("parentId", 0)

    var article: Article? by remember { mutableStateOf(null) }
    var likes by remember { mutableIntStateOf(0) }
    var saves by remember { mutableIntStateOf(0) }
    var articleUserId  by remember { mutableIntStateOf(0) }
    var articleUserName by remember { mutableStateOf("") }
    val selectedTabIndex = sharedPreferences.getInt("selectedTabIndex", 0)

    val likedArticleIds = sharedPreferences.getStringSet("likedArticleIds", emptySet())
        ?.map { it.toInt() }?.toHashSet() ?: hashSetOf()
    val savedArticleIds = sharedPreferences.getStringSet("savedArticleIds", emptySet())
        ?.map { it.toInt() }?.toHashSet() ?: hashSetOf()

    val urlPart = if (selectedTabIndex == 0) {
        "article"
    } else {
        "qna"
    }

    LaunchedEffect(articleId) {
        val apiString = "api/$urlPart/$articleId"
        val response = sendGetRequest(apiString)
        try {
            println(response)
            val gson = Gson()
            val apiResponse = gson.fromJson(response, CheckArticleResponse::class.java)
            article = apiResponse.data
            likes = apiResponse.data.likes
            saves = apiResponse.data.saves
            articleUserId = apiResponse.data.userId
            articleUserName = apiResponse.data.username
        } catch (e: Exception) {
            println("Json error: $response")
        }
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
                        "",
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
                    if (articleUserId == parentId) {
                        IconButton(onClick = { editClick = true }) {
                            Icon(
                                imageVector = Icons.Filled.Edit,
                                contentDescription = "修改"
                            )
                        }
                    }
                }
            )
        },
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(paddingValues)
                    .padding(16.dp)
            ) {
                article?.let {
                    Text(
                        text = it.title,
                        style = TextStyle(fontSize = 24.sp),
                        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                    )
                }

                val context = LocalContext.current
                var localImagePath by remember { mutableStateOf<String?>(null) }
                val file = File(context.cacheDir, "downloaded_image_$articleUserId.jpg")
                localImagePath = file.absolutePath

                Row(verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .padding(vertical = 8.dp)) {
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
                                    .clickable {
                                        val intent = Intent(activity, QAActivity::class.java)
                                        intent.putExtra("type", "user")
                                        intent.putExtra("username", articleUserName)
                                        activity.startActivity(intent)
                                        activity.finish()
                                    },
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            val resourceName = "photo${articleUserId % 12 + 1}" // 动态的资源名称
                            val resId = context.resources.getIdentifier(resourceName, "drawable", context.packageName)

                            Image(
                                painter = painterResource(id = resId),
                                contentDescription = "Avatar",
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .clickable {
                                        val intent = Intent(activity, QAActivity::class.java)
                                        intent.putExtra("type", "user")
                                        intent.putExtra("username", articleUserName)
                                        activity.startActivity(intent)
                                        activity.finish()
                                    },
                                contentScale = ContentScale.Crop
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    article?.let {
                        Text(
                            text = it.username,
                            style = TextStyle(fontSize = 16.sp),
                            modifier = Modifier.clickable {
                                val intent = Intent(activity, QAActivity::class.java)
                                intent.putExtra("type", "user")
                                intent.putExtra("username", articleUserName)
                                activity.startActivity(intent)
                                activity.finish()
                            }
                        ) }

                    Spacer(modifier = Modifier.weight(1f))

                    if (articleUserId == parentId) {
                        IconButton(onClick = { deleteClick = true }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete"
                            )
                        }
                    }

                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.DateRange,
                        contentDescription = "Publish Date",
                        modifier = Modifier.size(20.dp),
                        tint = Color.Gray
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${article?.time?.substring(0, 10)} ${article?.time?.substring(11)}",
                        style = TextStyle(fontSize = 14.sp, color = Color.Gray)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                article?.let {
                    Text(
                        text = it.content,
                        style = TextStyle(fontSize = 16.sp)
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                Box {
                    var isLiked by remember { mutableStateOf(likedArticleIds.contains(articleId)) }
                    var isSaved by remember { mutableStateOf(savedArticleIds.contains(articleId)) }
                    var commentText by remember { mutableStateOf(TextFieldValue(text = "")) }
                    var opLikes by remember { mutableStateOf("") }
                    var opSaves by remember { mutableStateOf("") }

                    val focusRequester = remember { FocusRequester() }
                    val focusManager = LocalFocusManager.current
                    val keyboardController = LocalSoftwareKeyboardController.current

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(bottom = 16.dp)
                            .clickable(
                                indication = null,
                                interactionSource = MutableInteractionSource()
                            ) {
                                focusManager.clearFocus() // 清除焦点
                                keyboardController?.hide() // 隐藏键盘
                                commentText = TextFieldValue(
                                    text = "",
                                )
                            }
                    ) {
                        CommentSection(articleId, activity, selectedTabIndex, onClick = {username ->
                            commentText = TextFieldValue(
                                text = "回复 $username: ",
                                selection = TextRange("回复 $username: ".length) // 光标定位到末尾
                            )
                            focusRequester.requestFocus()
                        })
                    }

                    Box(modifier = Modifier.fillMaxSize()) {
                        Row(
                            modifier = Modifier
                                .align(Alignment.BottomStart),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(modifier = Modifier.height(48.dp)) {
                                IconButton(
                                    onClick = {
                                        isLiked = !isLiked

                                        opLikes = if (isLiked) {
                                            "incr"
                                        } else {
                                            "decr"
                                        }

                                        CoroutineScope(Dispatchers.IO).launch {
                                            val apiString = "api/$urlPart/$parentId/likes/$articleId&$opLikes"
                                            sendPutRequest(apiString, "")
                                        }

                                        likes = if (isLiked) (likes + 1) else (likes - 1)
                                    },
                                    modifier = Modifier.size(32.dp)
                                        .align(Alignment.TopCenter)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Favorite,
                                        contentDescription = "Like",
                                        tint = if (isLiked) Color.Red else Color.Gray
                                    )
                                }

                                Text(
                                    text = likes.toString(),
                                    color = Color.Gray,
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier
                                        .align(Alignment.BottomCenter)
                                        .padding(4.dp)
                                )
                            }

                            Box(modifier = Modifier.height(48.dp)) {
                                IconButton(
                                    onClick = {
                                        isSaved = !isSaved

                                        opSaves = if (isSaved) {
                                            "incr"
                                        } else {
                                            "decr"
                                        }

                                        CoroutineScope(Dispatchers.IO).launch {
                                            val apiString = "api/$urlPart/$parentId/saves/$articleId&$opSaves"
                                            sendPutRequest(apiString, "")
                                        }

                                        saves = if (isSaved) (saves + 1) else (saves - 1)

                                    },
                                    modifier = Modifier.size(32.dp)
                                        .align(Alignment.TopCenter)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Bookmark,
                                        contentDescription = "Bookmark",
                                        tint = if (isSaved) Color(0xFFFFC107) else Color.Gray
                                    )
                                }

                                Text(
                                    text = saves.toString(),
                                    color = Color.Gray,
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier
                                        .align(Alignment.BottomCenter)
                                        .padding(4.dp)
                                )
                            }

                            var isFocused by remember { mutableStateOf(false) }

                            TextField(
                                value = commentText,
                                onValueChange = { commentText = it },
                                label = { Text("Add a comment...") },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(56.dp)
                                    .padding(start = 4.dp, end = 4.dp)
                                    .focusRequester(focusRequester)
                                    .onFocusChanged { focusState ->
                                        isFocused = focusState.isFocused
                                    },
                                singleLine = true,
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color(0xFFF1E3E6),
                                    unfocusedContainerColor = Color(0xFFF1E3E6),
                                ),
                            )

                            IconButton(
                                onClick = {
                                    if (commentText.text.isNotBlank()) {
                                        var apiPath = ""
                                        var requestBody = JSONObject().apply {
                                            put("articleId", "")
                                            put("userId", "")
                                            put("content", "")
                                        }

                                        if (selectedTabIndex == 0) {
                                            apiPath = "api/comment"

                                            requestBody = JSONObject().apply {
                                                put("articleId", articleId)
                                                put("userId", parentId)
                                                put("content", commentText.text)
                                            }
                                        } else {
                                            apiPath = "api/qnaComment"

                                            requestBody = JSONObject().apply {
                                                put("qnaId", articleId)
                                                put("userId", parentId)
                                                put("content", commentText.text)
                                            }
                                        }

                                        println(requestBody)
                                        CoroutineScope(Dispatchers.Main).launch {
                                            val responseString = sendPostRequestWithRequest(
                                                apiPath,
                                                requestBody.toString()
                                            )
                                            println(responseString)
                                        }

                                        commentText = TextFieldValue(
                                            text = "",
                                        )

                                        val editor = sharedPreferences.edit()
                                        editor.putBoolean("refreshCommentState", true)
                                        editor.apply()

                                        focusManager.clearFocus()

                                        val intent = Intent(activity, MyBlogCheckActivity::class.java)
                                        activity.startActivity(intent)
                                        activity.finish()
                                    }
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Send,
                                    contentDescription = "Submit Comment",
                                    tint = Color.Gray
                                )
                            }
                        }
                    }
                }
            }
        }
    )

    if (backFlag) {
        val page = sharedPreferences.getString("blogPage", "")

        if (page == "mine") {
            val intent = Intent(activity, BlogMineActivity::class.java)
            activity.startActivity(intent)
            activity.finish()
        }

        if (page == "search") {
            val intent = Intent(activity, SearchResultActivity::class.java)
            activity.startActivity(intent)
            activity.finish()
        }

        if (page == "blog") {
            val intent = Intent(activity, BlogActivity::class.java)
            activity.startActivity(intent)
            activity.finish()
        }

    }

    if (editClick) {
        val intent = Intent(activity, BlogAddActivity::class.java)
        activity.startActivity(intent)
        activity.finish()
    }

    if (deleteClick) {
        AlertDialog(
            onDismissRequest = { deleteClick = false },
            title = { Text("是否删除博文？") },
            text = { Text("删除后无法恢复，您确定要删除此博文吗？") },
            confirmButton = {
                Button(
                    onClick = {
                        val apiString = "api/$urlPart/$articleId"
                        CoroutineScope(Dispatchers.IO).launch {
                            sendDeleteRequest(apiString)
                        }

                        val intent = Intent(activity, BlogMineActivity::class.java)
                        activity.startActivity(intent)
                        activity.finish()
                    }
                ) {
                    Text("确定")
                }
            },
            dismissButton = {
                Button(onClick = {
                    deleteClick = false
                }) {
                    Text("取消")
                }
            }
        )
    }
}

@Composable
fun CommentSection(articleId: Int, activity: Activity, number: Int, onClick: (String) -> Unit) {
    val sharedPreferences: SharedPreferences =
        activity.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)
    val refreshState = sharedPreferences.getBoolean("refreshCommentState", false)

    var apiString = ""
    apiString = if (number == 0) {
        "api/article-comment?articleId=$articleId"
    } else {
        "api/qna-qnaComment?qnaId=$articleId"
    }
    var comments: List<Comment> by remember { mutableStateOf(emptyList()) }

    LaunchedEffect(refreshState) {
        val response = sendGetRequest(apiString)
        try {
            val gson = Gson()
            val apiResponse = gson.fromJson(response, CommentResponse::class.java)
            println(apiResponse)
            comments = if (!apiResponse.success) {
                emptyList()
            } else {
                apiResponse.data
            }
        } catch (e: Exception) {
            println("Json error: $response")
        }

        val editor = sharedPreferences.edit()
        editor.putBoolean("refreshCommentState", false)
        editor.apply()
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "评论区 (${comments.size})",
            style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold)
        )

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(modifier = Modifier.fillMaxWidth()
            .padding(bottom = 60.dp) ) {
            items(comments) { comment ->
                CommentItem(comment = comment, activity, number, onClick = { username ->
                    onClick(username) })
            }
        }

    }

}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CommentItem(comment: Comment, activity: Activity, number: Int, onClick: (String) -> Unit) {
    val sharedPreferences: SharedPreferences =
        activity.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)
    val parentId = sharedPreferences.getInt("parentId", 0)

    var showDeleteDialog by remember { mutableStateOf(false) }

    val likedCommentIds = sharedPreferences.getStringSet("likedCommentIds", emptySet())
        ?.map { it.toInt() }?.toHashSet() ?: hashSetOf()

    Row(
        modifier = Modifier
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.padding(8.dp)
                .weight(1f)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = {
                            onClick(comment.username)
                        },
                        onLongPress = {
                            showDeleteDialog = true
                        }
                    )
                }
        ) {
            Text(
                text = comment.username,
                style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 14.sp)
            )

            Text(
                text = "${comment.time.substring(0, 10)} ${comment.time.substring(11)}",
                style = TextStyle(fontSize = 12.sp, color = Color.Gray)
            )

            Text(
                text = comment.content,
                style = TextStyle(fontSize = 14.sp)
            )

            Spacer(modifier = Modifier.height(8.dp))
        }

        println("list:$likedCommentIds, id:${comment.commentId}")

        var isLiked by remember { mutableStateOf(likedCommentIds.contains(comment.commentId)) }
        var opLikes by remember { mutableStateOf("") }
        var likes by remember { mutableIntStateOf(comment.likes) }

        Box(modifier = Modifier.height(48.dp)) {
            IconButton(
                onClick = {
                    isLiked = !isLiked

                    opLikes = if (isLiked) {
                        "incr"
                    } else {
                        "decr"
                    }

                    CoroutineScope(Dispatchers.IO).launch {
                        var apiString = ""
                        apiString = if (number == 0) {
                            "api/comment/$parentId/likes/${comment.commentId}&$opLikes"
                        } else {
                            "api/qnaComment/$parentId/likes/${comment.qnaCommentId}&$opLikes"
                        }
                        val response = sendPutRequest(apiString, "")
                        println(response)
                    }

                    likes = if (isLiked) (likes + 1) else (likes - 1)
                },
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Favorite,
                    contentDescription = "Like",
                    tint = if (isLiked) Color.Red else Color.Gray,
                    modifier = Modifier.size(20.dp)
                )
            }

            Text(
                text = likes.toString(),
                color = Color.Gray,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(4.dp)
            )
        }

    }

    if (showDeleteDialog && comment.userId == parentId) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("是否删除该评论？") },
            text = { Text("您确定要删除此评论吗？") },
            confirmButton = {
                Button(onClick = {
                    var apiString = ""
                    apiString = if (number == 0) {
                        "api/comment/${comment.commentId}"
                    } else {
                        "api/qnaComment/${comment.qnaCommentId}"
                    }
                    CoroutineScope(Dispatchers.IO).launch {
                        sendDeleteRequest(apiString)
                    }

                    val editor = sharedPreferences.edit()
                    editor.putBoolean("refreshCommentState", true)
                    editor.apply()

                    showDeleteDialog = false

                    val intent = Intent(activity, MyBlogCheckActivity::class.java)
                    activity.startActivity(intent)
                    activity.finish()
                }) {
                    Text("确定")
                }
            },
            dismissButton = {
                Button(onClick = { showDeleteDialog = false }) {
                    Text("取消")
                }
            }
        )
    }
}

data class CheckArticleResponse(
      val data: Article,
      val success: Boolean,
      val errorMsg: String?
)

data class CommentResponse(
    val data: List<Comment>,
    val success: Boolean,
    val errorMsg: String? = null
)

data class Comment(
    val commentId: Int,
    val qnaCommentId: Int,
    val articleId: Int,
    val userId: Int,
    val username: String,
    val content: String,
    val likes: Int,
    val time: String,
)
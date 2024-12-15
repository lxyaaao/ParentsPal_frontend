package com.example.myapplication

import com.example.myapplication.activity.AIQA.ConversationInfo
import com.example.myapplication.activity.AIQA.isUserConversation
import com.example.myapplication.activity.AIQA.parseConversationHistory
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CoroutineTestRule : TestRule {
    private val testDispatcher = StandardTestDispatcher()
    val testScope = TestScope(testDispatcher)

    override fun apply(base: Statement?, description: Description?): Statement {
        return object : Statement() {
            @Throws(Throwable::class)
            override fun evaluate() {
                Dispatchers.setMain(testDispatcher)
                try {
                    base?.evaluate()
                } finally {
                    Dispatchers.resetMain()
                }
            }
        }
    }
}

class QAActivityTest {

    @Test
    fun testIsUserConversation_UserConversation() {
        // 模拟为用户对话
        ConversationInfo.conversationType = "user"
        ConversationInfo.userName = "Alice"

        assertTrue(isUserConversation())
    }

    @Test
    fun testIsUserConversation_BotConversation() {
        // 模拟为机器人对话
        ConversationInfo.conversationType = "bot"
        ConversationInfo.userName = null

        assertFalse(isUserConversation())
    }

    @get:Rule
    val coroutineTestRule = CoroutineTestRule()

//    @Test
//    fun testLoadConversationHistory_Coroutine() = runTest {
//        // 模拟返回的 JSON 数据
//        val mockResponse = """{
//            "data": [{"sender_name": "Alice", "content": "Hi", "created_at": "2024-12-15T10:00:00Z"}]
//        }"""
//
//        // 使用 mockkStatic 来模拟静态方法
//        mockkStatic(NetworkUtils::class)
//
//        // 使用 coEvery 来模拟挂起函数
//        coEvery { NetworkUtils.sendGetRequest(any<String>()) } returns mockResponse
//
//        // 在协程中调用挂起函数
//        val result = loadConversationHistory("Bob", "Alice")
//
//        // 断言返回的结果
//        assertEquals(1, result.size)
//        assertEquals("Alice", result[0].userName)
//    }
}


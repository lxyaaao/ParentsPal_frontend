package com.example.myapplication.utils

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URL

object NetworkUtils {
    suspend fun sendPostRequest(apiString: String): String {
        return withContext(Dispatchers.IO) {
            val urlString = "http://parentspal.natapp1.cc/"
            val url = URL(urlString + apiString)

            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            print(connection)
            try {
                connection.inputStream.use { it.reader().use { reader -> reader.readText() } }
            } catch (e: Exception) {
                "Error: ${e.message}"
            } finally {
                connection.disconnect()
            }
        }
    }

    suspend fun sendPostRequestWithRequest(apiString: String, requestBody: String): String {
        return withContext(Dispatchers.IO) {
            val urlString = "http://parentspal.natapp1.cc/"
            val url = URL(urlString + apiString)

            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            print(connection)
            connection.doOutput = true
            connection.setRequestProperty("Content-Type", "application/json")
            try {
                connection.outputStream.use { outputStream ->
                    outputStream.write(requestBody.toByteArray(Charsets.UTF_8))
                    outputStream.flush()
                }

                val responseCode = connection.responseCode
                println("HTTPCode: $responseCode")

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    connection.inputStream.use { it.reader().use { reader -> reader.readText() } }
                } else if (responseCode == HttpURLConnection.HTTP_CONFLICT) {
                    connection.errorStream.use { it.reader().use { reader -> reader.readText() } }
                } else {
                    "Error: HTTP $responseCode"
                }
            } catch (e: Exception) {
                "Error: ${e.message}"
            } finally {
                connection.disconnect()
            }
        }
    }





    suspend fun sendAIRequest(message: String, conversation: String = ""): String {
        return withContext(Dispatchers.IO) {
            val url = URL("http://parentspal.natapp1.cc/v1/chat-messages")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.setRequestProperty("Authorization", "Bearer app-2Z15tg459MeUA12SSjKuoyYt")
            connection.setRequestProperty("Content-Type", "application/json; utf-8")
            connection.setRequestProperty("Accept", "application/json")
            connection.doOutput = true

            val jsonInputString = """
                {
                    "inputs": {},
                    "query": "$message",
                    "response_mode": "blocking",
                    "conversation_id": "$conversation",
                    "user": "随便写一个"
                }
            """.trimIndent()

            try {
                connection.outputStream.use { os: OutputStream ->
                    val input = jsonInputString.toByteArray()
                    os.write(input, 0, input.size)
                }

                val responseCode = connection.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    connection.inputStream.use { it.reader().use { reader -> reader.readText() } }
                } else {
                    connection.errorStream.use { it.reader().use { reader -> reader.readText() } }
                }
            } catch (e: Exception) {
                "Error: ${e.message}"
            } finally {
                connection.disconnect()
            }
        }
    }

    suspend fun sendGetRequest(apiString: String): String {
        return withContext(Dispatchers.IO) {
            val urlString = "http://parentspal.natapp1.cc/"
            val url = URL(urlString + apiString)

            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"

            try {
                connection.inputStream.use { it.reader().use { reader -> reader.readText() } }
            } catch (e: Exception) {
                "Error: ${connection.responseCode} - ${e.message}"
            } finally {
                connection.disconnect()
            }
        }
    }
}

suspend fun sendDeleteRequest(apiString: String): String {
    return withContext(Dispatchers.IO) {
        val urlString = "http://parentspal.natapp1.cc/"
        val url = URL(urlString + apiString)

        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "DELETE"
        println("HTTP: ${connection.responseCode}")

        try {
            connection.inputStream.use { it.reader().use { reader -> reader.readText() } }
        } catch (e: Exception) {
            "Error: ${connection.responseCode} - ${e.message}"
        } finally {
            connection.disconnect()
        }
    }
}

suspend fun sendPutRequest(apiString: String, requestBody: String): String {
    return withContext(Dispatchers.IO) {
        val urlString = "http://parentspal.natapp1.cc/"
        val url = URL(urlString + apiString)

        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "PUT"
        connection.doOutput = true

        try {
            connection.outputStream.use { outputStream ->
                outputStream.write(requestBody.toByteArray(Charsets.UTF_8))
            }
            connection.inputStream.use { it.reader().use { reader -> reader.readText() } }
        } catch (e: Exception) {
            "Error: ${connection.responseCode} - ${e.message}"
        } finally {
            connection.disconnect()
        }
    }
}
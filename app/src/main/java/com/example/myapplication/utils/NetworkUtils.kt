package com.example.myapplication.utils

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedWriter
import java.io.File
import java.io.FileInputStream
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLConnection

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
                "Error: ${connection.responseCode} - ${e.message}"
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
                "Error: ${connection.responseCode} - ${e.message}"
            } finally {
                connection.disconnect()
            }
        }
    }


    suspend fun sendPostRequestWithFile(apiString: String, file: File): String {
        return withContext(Dispatchers.IO) {
            val urlString = "http://parentspal.natapp1.cc/"
            val url = URL(urlString + apiString)

            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.doOutput = true
            connection.doInput = true

            val boundary = "Boundary-${System.currentTimeMillis()}"
            connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=$boundary")

            // 设置超时
            connection.connectTimeout = 15000 // 15秒
            connection.readTimeout = 20000 // 20秒

            var writer: BufferedWriter? = null
            var outputStream: OutputStream? = null

            try {
                outputStream = connection.outputStream
                writer = outputStream.bufferedWriter(Charsets.UTF_8)

                // 写入请求头部
                writer.apply {
                    append("--$boundary\r\n")
                    append("Content-Disposition: form-data; name=\"file\"; filename=\"${file.name}\"\r\n")
                    append("Content-Type: ${URLConnection.guessContentTypeFromName(file.name)}\r\n")
                    append("\r\n")
                    flush()
                }

                // 写入文件流
                val buffer = ByteArray(4096)
                var bytesRead: Int
                FileInputStream(file).use { fileInputStream ->
                    while (fileInputStream.read(buffer).also { bytesRead = it } != -1) {
                        outputStream.write(buffer, 0, bytesRead)
                    }
                }
                outputStream.flush()

                // 写入请求结束标志
                writer.apply {
                    append("\r\n")
                    append("--$boundary--\r\n")
                    flush()
                }

                // 获取服务器响应
                val responseCode = connection.responseCode
                println("HTTPCode: $responseCode")

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    connection.inputStream.use { it.reader().use { reader -> reader.readText() } }
                } else {
                    connection.errorStream.use { it.reader().use { reader -> reader.readText() } }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                return@withContext "Error: ${e.message}"
            } finally {
                writer?.close()
                outputStream?.close()
                connection.disconnect()
            }
        }
    }



    suspend fun sendAIRequest(myName: String, message: String, conversation: String = ""): String {
        return withContext(Dispatchers.IO) {
            val url = URL("http://parentspal.natapp1.cc/api/ai_conversations/message")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.setRequestProperty("Authorization", "Bearer app-2Z15tg459MeUA12SSjKuoyYt")
            connection.setRequestProperty("Content-Type", "application/json; utf-8")
            connection.setRequestProperty("Accept", "application/json")
            connection.doOutput = true
            val jsonInputString = """
                {
                    "username":"$myName",
                    "conversationId":"",
                    "query":"$message",
                    "mode":"blocking"
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
                "Error: ${connection.responseCode} - ${e.message}"
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

suspend fun downloadImage(apiString: String, saveFilePath: String): String {
    return withContext(Dispatchers.IO) {
        val urlString = "http://parentspal.natapp1.cc/"
        val url = URL(urlString + apiString)

        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "GET"

        try {
            // 检查响应码
            val responseCode = connection.responseCode
            if (responseCode != HttpURLConnection.HTTP_OK) {
                return@withContext "Error: HTTP $responseCode"
            }

            // 保存图片到本地
            val file = File(saveFilePath)
            file.outputStream().use { fileOut ->
                connection.inputStream.copyTo(fileOut)
            }

            println("File downloaded successfully to $saveFilePath")

            return@withContext "File downloaded successfully to $saveFilePath"
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext "Error: ${e.message}"
        } finally {
            connection.disconnect()
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

suspend fun sendPatchRequest(apiString: String, requestBody: String): String {
    return withContext(Dispatchers.IO) {
        val urlString = "http://parentspal.natapp1.cc/"
        val url = URL(urlString + apiString)

        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "PATCH"
        connection.doOutput = true
        connection.setRequestProperty("Content-Type", "application/json")

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

suspend fun getUserExpertStatus(parentId: Int): Boolean {
    return withContext(Dispatchers.IO) {
        val urlString = "http://parentspal.natapp1.cc/api/appuser/$parentId/expert-status"
        val url = URL(urlString)

        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "GET"

        try {
            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                connection.inputStream.use { it.reader().use { reader -> reader.readText().toBoolean() } }
            } else {
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        } finally {
            connection.disconnect()
        }
    }
}

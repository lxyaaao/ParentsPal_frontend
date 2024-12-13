package com.example.myapplication.api

import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

data class Baby(
    val id: Long,
    val name: String,
    val gender: String,
    val birthdate: String,
    val photoUrl: String
)

// 请求数据类
data class LoginRequest(val phoneNumber: String, val password: String)

// 响应数据类
data class LoginResponse(val message: String, val status: Boolean, val parentId: Int, val parentName: String, val babies: List<Baby>)

// 定义 API 接口
interface ApiService {
    @POST("api/appuser/login")
    fun login(@Body request: LoginRequest): Call<LoginResponse>

}
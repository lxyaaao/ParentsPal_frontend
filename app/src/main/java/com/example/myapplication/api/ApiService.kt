package com.example.myapplication.api

import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

data class Baby(
    val id: Int,
    val name: String,
    val gender: String,
    val birthdate: String,
    val photoUrl: String
)

// 请求数据类
data class LoginRequest(val phoneNumber: String, val password: String)
data class RegisterRequest(val name: String, val phoneNumber: String, val password: String)
data class ChangePasswordRequest(val oldPassword: String,  val newPassword: String)

// 响应数据类
data class LoginResponse(val message: String, val status: Boolean, val parentId: Int, val babies: List<Baby>)
data class ChangePasswordResponse(val message: String, val status: Boolean)
data class BabiesResponse(val name: String, val gender: String, val birthdate: String, val photoUrl: String)

// 定义 API 接口
interface ApiService {
    @POST("api/v1/appuser/login")
    fun login(@Body request: LoginRequest): Call<LoginResponse>

    @POST("api/v1/appuser/register")
    fun register(@Body request: RegisterRequest): Response<Void>

    @PUT("api/v1/appuser/changePassword")
    fun changePassword(
        @Body changePasswordRequest: ChangePasswordRequest
    ): Response<ChangePasswordResponse>

    @GET("api/babies/{parentId}/get")
    fun getBabies(
        @Path("parentId") parentId: String
    ): Response<BabiesResponse>
}
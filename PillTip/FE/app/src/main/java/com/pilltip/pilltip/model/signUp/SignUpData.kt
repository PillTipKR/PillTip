package com.pilltip.pilltip.model.signUp

data class SignUpData(
    val loginType: LoginType = LoginType.NONE,
    val userId: String = "",
    val password: String = "",
    val term: Boolean = false,
    val nickname: String = "",
    val gender: String = "",
    val birthDate: String = "", // YYYY-MM-DD
    val age: Int = 0,
    val height: Int = 0,
    val weight: Int = 0,
    val interest: String = "",
    val phone: String = "",
    val token: String = "" // 소셜 로그인 시 access token
)

data class SignUpRequest(
    val loginType: String,
    val userId: String?, // token일 시 null
    val password: String?,  // token일 시 null
    val term: Boolean,
    val nickname: String,
    val gender: String,
    val birthDate: String,
    val age: Int,
    val height: Int,
    val weight: Int,
    val interest: String?,
    val phone: String,
    val token: String?  // id/pw일 시 null
)

data class SignUpResponse(
    val success: Boolean,
    val userId: String,
    val token: String,
    val nickname: String
)

package com.pilltip.pilltip.model.main

import com.pilltip.pilltip.model.signUp.LoginType

data class MainData(
    val loginType: LoginType = LoginType.IDPW,
    val loginId: String = "",
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

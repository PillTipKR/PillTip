package com.pilltip.pilltip.model.signUp

data class SignUpData(
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

data class SignUpRequest(
    val loginType: String,
    val loginId: String?, // token일 시 null
    val password: String?,  // token일 시 null
//    val term: Boolean,
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
    val status: String,
    val message: String,
    val data: SignUpTokenData
)

data class SignUpTokenData(
    val accessToken: String,
    val refreshToken: String
)

data class TermsRequest(
    val termsOfService: Boolean,
    val privacyPolicy: Boolean,
    val marketingConsent: Boolean
)

package com.pilltip.pilltip.model.signUp

import javax.inject.Inject

class AuthRepository @Inject constructor(
    private val authApi: ServerAuthAPI
) {
    suspend fun sendSignUp(data: SignUpData): Boolean {
        val request = SignUpRequest(
            loginType = data.loginType.name,
            userId = data.userId,
            password = if (data.loginType == LoginType.IDPW) data.password else null,
            term = data.term,
            nickname = data.nickname,
            gender = data.gender,
            birthDate = data.birthDate,
            age = data.age,
            height = data.height,
            weight = data.weight,
            interest = data.interest,
            phone = data.phone,
            token = if (data.loginType == LoginType.KAKAO) data.token else null
        )

        return try {
            val response = authApi.signUp(request)
            response.isSuccessful && response.body()?.success == true
        } catch (e: Exception) {
            false
        }
    }
}
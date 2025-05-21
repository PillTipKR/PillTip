package com.pilltip.pilltip.model.signUp

import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

class AuthRepository @Inject constructor(
    private val authApi: ServerAuthAPI
) {
    suspend fun sendSignUp(data: SignUpData): Boolean {
        val request = SignUpRequest(
            loginType = data.loginType.name,
            userId = if (data.loginType == LoginType.idpw) data.userId else null,
            password = if (data.loginType == LoginType.idpw) data.password else null,
            term = data.term,
            nickname = data.nickname,
            gender = data.gender,
            birthDate = data.birthDate,
            age = data.age,
            height = data.height,
            weight = data.weight,
            interest = data.interest,
            phone = data.phone,
            token = if (data.loginType == LoginType.social) data.token else null
        )

        return try {
            val response = authApi.signUp(request)
            Log.d("SignUp", "sendSignUp called")
            Log.d("SignUp", "Code: ${response.code()}, Success: ${response.body()?.success}, Error: ${response.errorBody()?.string()}")
            response.isSuccessful && response.body()?.success == true
        } catch (e: Exception) {
            Log.e("SignUp", "Network error", e)
            false
        }
    }
}
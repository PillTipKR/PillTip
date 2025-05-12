package com.pilltip.pilltip.model.social

import android.content.Context
import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.kakao.sdk.user.model.User
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val kakaoLoginManager: KakaoLoginManager
) : ViewModel() {

    private val _user = mutableStateOf<User?>(null)
    val user: State<User?> = _user
    private var currentToken: String? = null

    fun kakaoLogin(context: Context) {
        kakaoLoginManager.login(context) { token, error ->
            if (error != null) {
                Log.e("LoginVM", "로그인 실패", error)
            } else {
                if (token != null) {
                    currentToken = token.accessToken
                }
                kakaoLoginManager.getUserInfo { userInfo ->
                    _user.value = userInfo
                    //Log.d("LoginVM", "사용자 정보: $userInfo")
                }
            }
        }
    }

    fun getAccessToken(): String? = currentToken

    fun logout() {
        kakaoLoginManager.logout {
            _user.value = null
        }
    }

    fun unlink(context : Context){
        kakaoLoginManager.unlink(context)
        logout()
    }
}
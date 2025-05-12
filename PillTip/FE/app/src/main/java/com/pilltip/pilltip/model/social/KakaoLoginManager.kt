package com.pilltip.pilltip.model.social

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.user.UserApiClient
import com.kakao.sdk.user.model.User
import javax.inject.Inject

class KakaoLoginManager @Inject constructor() {
    fun login(context: Context, callback: (OAuthToken?, Throwable?) -> Unit) {
        if (UserApiClient.instance.isKakaoTalkLoginAvailable(context)) {
            UserApiClient.instance.loginWithKakaoTalk(context, callback = callback)
        } else {
            UserApiClient.instance.loginWithKakaoAccount(context, callback = callback)
        }
    }

    fun getUserInfo(onResult: (User?) -> Unit) {
        UserApiClient.instance.me { user, error ->
            if (error != null) {
                Log.e("Kakao", "사용자 정보 요청 실패", error)
                onResult(null)
            } else {
                onResult(user)
                if (user != null) {
                    Log.d("Kakao", "닉네임: ${user.kakaoAccount?.profile?.nickname}")
                }

                UserApiClient.instance.signup { signupError ->
                    if (signupError != null) {
                        Log.e("Kakao", "signup 실패", signupError)
                    } else {
                        Log.d("Kakao", "signup 성공")
                    }
                }
            }
        }
    }

    fun logout(callback: (Throwable?) -> Unit) {
        UserApiClient.instance.logout(callback)
    }

    fun unlink(context: Context) {
        UserApiClient.instance.unlink { error ->
            if (error != null) {
                Toast.makeText(context, "탈퇴 실패", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "정상적으로 탈퇴되었습니다", Toast.LENGTH_SHORT).show()
                // 유저 상태 초기화 or 로그인 페이지 이동
            }
        }
    }
}
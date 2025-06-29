package com.pilltip.pilltip.view.auth

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.pilltip.pilltip.R
import com.pilltip.pilltip.composable.ButtonWithLogo
import com.pilltip.pilltip.composable.HeightSpacer
import com.pilltip.pilltip.model.signUp.KaKaoLoginViewModel
import com.pilltip.pilltip.model.signUp.LoginType
import com.pilltip.pilltip.model.signUp.SignUpViewModel
import com.pilltip.pilltip.view.auth.logic.TermBottomSheet

@Composable
fun KakaoAuthPage(
    navController: NavController,
    signUpViewModel: SignUpViewModel,
    kakaoViewModel: KaKaoLoginViewModel = hiltViewModel(),
) {
    val user by kakaoViewModel.user
    val context = LocalContext.current
    val token = kakaoViewModel.getAccessToken()
    var termsOfService by remember { mutableStateOf(false) }

    LaunchedEffect(user) {
        if (user != null && token != null) {
            signUpViewModel.updateLoginType(LoginType.SOCIAL)
            signUpViewModel.updateToken(token)
            signUpViewModel.updateProvider("kakao")
            termsOfService = true
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.Bottom,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ButtonWithLogo(
            backgroundColor = Color(0xFFFDE500),
            textColor = Color(0xFF230E00),
            textSize = 16,
            textWeight = 500,
            buttonText = "카카오 로그인",
            logoResourceId = R.drawable.ic_kakao_login,
            onClick = {
                kakaoViewModel.kakaoLogin(context)
            }
        )
        HeightSpacer(16.dp)
        ButtonWithLogo(
            backgroundColor = Color(0xFFFDE500),
            textColor = Color(0xFF230E00),
            textSize = 16,
            textWeight = 500,
            buttonText = "카카오 로그아웃",
            logoResourceId = R.drawable.ic_kakao_login,
            onClick = {
                kakaoViewModel.logout()
            }
        )
        HeightSpacer(16.dp)
        ButtonWithLogo(
            backgroundColor = Color(0xFFFDE500),
            textColor = Color(0xFF230E00),
            textSize = 16,
            textWeight = 500,
            buttonText = "카카오 탈퇴",
            logoResourceId = R.drawable.ic_kakao_login,
            onClick = {
                kakaoViewModel.unlink(context)
            }
        )
        HeightSpacer(80.dp)
    }

    if (termsOfService) {
        TermBottomSheet(
            signUpViewModel,
            navController,
            onDismiss = { termsOfService = false }
        )
    }
}
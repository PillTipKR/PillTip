package com.pilltip.pilltip.view

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.pilltip.pilltip.R
import com.pilltip.pilltip.composable.ButtonWithLogo
import com.pilltip.pilltip.composable.HeightSpacer
import com.pilltip.pilltip.model.social.LoginViewModel

@Composable
fun LoginPage(viewModel: LoginViewModel = hiltViewModel()) {
    val user by viewModel.user
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.Bottom,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (user == null) {
            ButtonWithLogo(
                backgroundColor = Color(0xFFFDE500),
                textColor = Color(0xFF230E00),
                textSize = 16,
                textWeight = 500,
                buttonText = "카카오 로그인",
                logoResourceId = R.drawable.ic_kakao_login,
                onClick = {
                    viewModel.kakaoLogin(context)
                }
            )
        } else {
            Text("안녕하세요, ${user?.kakaoAccount?.profile?.nickname}님!")
            viewModel.getAccessToken()?.let { Log.d("Token : ", it) }
            Spacer(modifier = Modifier.height(16.dp))
            ButtonWithLogo(
                backgroundColor = colorResource(id = R.color.kakao_background_color),
                textColor = colorResource(id = R.color.kakao_font_color),
                textSize = 16,
                textWeight = 500,
                buttonText = "달력 테스트",
                logoResourceId = null,
                onClick = { viewModel.logout() }
            )
            Spacer(modifier = Modifier.height(16.dp))
            ButtonWithLogo(
                backgroundColor = colorResource(id = R.color.kakao_background_color),
                textColor = colorResource(id = R.color.kakao_font_color),
                textSize = 16,
                textWeight = 500,
                buttonText = "로그아웃",
                logoResourceId = R.drawable.ic_kakao_login,
                onClick = { viewModel.logout() }
            )
            HeightSpacer(24.dp)
            ButtonWithLogo(
                backgroundColor = colorResource(id = R.color.kakao_background_color),
                textColor = colorResource(id = R.color.kakao_font_color),
                textSize = 16,
                textWeight = 500,
                buttonText = "카카오 로그인 연동 철회",
                logoResourceId = R.drawable.ic_kakao_login,
                onClick = { viewModel.unlink(context) }
            )
        }
        HeightSpacer(80.dp)
    }
}

@Preview
@Composable
fun LoginPagePreview() {
    LoginPage()
}
package com.pilltip.pilltip.composable

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pilltip.pilltip.R
import com.pilltip.pilltip.model.social.LoginViewModel
import com.pilltip.pilltip.ui.theme.pretendard

/**
 * 주로 화면 아랫 부분에 있는 버튼입니다. 앱 전반에서 주로 사용합니다.
 * @param mModifier 버튼을 만들 때 사용하는 Modifier입니다. 따로 정의 돤 buttonModifier를 기본으로 사용합니다.
 * @param text 버튼에 들어갈 텍스트입니다.
 * @param buttonColor 버튼의 색을 설정합니다. 기본 색은 Color(0xFF397CDB)입니다.
 * @param textColor 버튼의 텍스트 색을 설정합니다. 기본 색은 Color.White입니다.
 * @param onClick 버튼을 누르면 실행되는 함수입니다. 주로 네비게이션 관련 함수가 작성됩니다.
 */
@Composable
fun NextButton(
    mModifier: Modifier = buttonModifier,
    text: String = "다음",
    buttonColor: Color = Color(0xFF397CDB),
    textColor: Color = Color.White,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = mModifier,
        shape = RoundedCornerShape(size = 16.dp),
        colors = ButtonDefaults.buttonColors(buttonColor),
    ) {
        Text(
            text = text,
            fontFamily = pretendard,
            color = Color.White,
            fontWeight = FontWeight.SemiBold,
            fontSize = 18.sp,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            style = TextStyle(
                platformStyle = PlatformTextStyle(
                    includeFontPadding = false
                )
            )
        )
    }
}

@Composable
fun ButtonWithLogo(
    backgroundColor: Color,
    textColor: Color,
    textSize: Int,
    textWeight: Int,
    buttonText: String,
    logoResourceId: Int? = null,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp)
            .background(color = backgroundColor, shape = RoundedCornerShape(size = 16.dp))
            .clickable { onClick() }
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (logoResourceId != null) {
            Image(
                imageVector = ImageVector.vectorResource(id = logoResourceId),
                contentDescription = "Button Logo"
            )
        } else Box(modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = buttonText,
            color = textColor,
            fontSize = textSize.sp,
            fontFamily = pretendard,
            fontWeight = FontWeight(textWeight)
        )
        Spacer(modifier = Modifier.weight(1f))
        Box(modifier = Modifier.size(20.dp))
    }
}
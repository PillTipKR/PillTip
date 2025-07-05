package com.pilltip.pilltip.composable.MainComposable

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pilltip.pilltip.composable.HeightSpacer
import com.pilltip.pilltip.ui.theme.gray800
import com.pilltip.pilltip.ui.theme.pretendard
import com.pilltip.pilltip.ui.theme.primaryColor

@Composable
fun DURText(
    title : String = "A약품 성분 검사지",
    isOk : Boolean = true,
    description : String = "A약품, 또는 B 약품 탭의 해당 약품 성분 분석 결과지에는 A 또는 B 약품의 어떤 성분과, 내가 복약 중인 약품의 어떤 성분 간 상충이 발생하기에, 또는 과복용의 우려가 있기에 함께 복약해선 안 된다는 상세 설명이 나옴."
){
    Text(
        text = title,
        style = TextStyle(
            fontSize = 18.sp,
            fontFamily = pretendard,
            fontWeight = FontWeight(600),
            color = if(isOk) primaryColor else Color(0xFFEB2C28),
        )
    )
    HeightSpacer(16.dp)
    Text(
        text = description,
        style = TextStyle(
            fontSize = 12.sp,
            lineHeight = 18.sp,
            fontFamily = pretendard,
            fontWeight = FontWeight(400),
            color = gray800,
            textAlign = TextAlign.Center,
        )
    )
}
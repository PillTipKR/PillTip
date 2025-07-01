package com.pilltip.pilltip.composable.QuestionnaireComposable

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pilltip.pilltip.R
import com.pilltip.pilltip.composable.HeightSpacer
import com.pilltip.pilltip.ui.theme.gray100
import com.pilltip.pilltip.ui.theme.gray500
import com.pilltip.pilltip.ui.theme.pretendard

@Composable
fun InformationBox(
    header: String,
    headerColor: Color = Color.Black,
    headerSize: Int = 24,
    desc: String,
    image: Int = R.drawable.logo_pilltip_typo
){
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = header,
            fontSize = headerSize.sp,
            fontFamily = pretendard,
            fontWeight = FontWeight(700),
            color = headerColor,
        )
        HeightSpacer(12.dp)
        Text(
            text = desc,
            fontSize = 14.sp,
            lineHeight = 19.6.sp,
            fontFamily = pretendard,
            fontWeight = FontWeight(600),
            color = gray500
        )
        HeightSpacer(42.dp)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
                .background(color = gray100, shape = RoundedCornerShape(size = 12.dp)),
            contentAlignment = Alignment.Center
        ){
            Image(
                imageVector = ImageVector.vectorResource(image),
                contentDescription = "설명 이미지"
            )
        }
    }
}
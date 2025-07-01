package com.pilltip.pilltip.composable.MainComposable

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pilltip.pilltip.composable.HeightSpacer
import com.pilltip.pilltip.composable.WidthSpacer
import com.pilltip.pilltip.composable.noRippleClickable
import com.pilltip.pilltip.model.search.TakingPillSummary
import com.pilltip.pilltip.ui.theme.gray200
import com.pilltip.pilltip.ui.theme.pretendard
import com.pilltip.pilltip.ui.theme.primaryColor
import com.pilltip.pilltip.ui.theme.primaryColor050

@Composable
fun ProfileTagButton(
    text: String,
    image: Int = 0,
    selected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (selected) primaryColor else primaryColor050
    val textColor = if (selected) Color.White else primaryColor
    val borderColor = if (selected) primaryColor else primaryColor

    Row(
        modifier = Modifier
            .border(
                width = 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(size = 100.dp)
            )
            .height(30.dp)
            .background(color = backgroundColor, shape = RoundedCornerShape(size = 100.dp))
            .padding(horizontal = if (image == 0) 14.dp else 12.dp, vertical = 8.dp)
            .noRippleClickable { onClick() }
    ) {
        Text(
            text = text,
            style = TextStyle(
                fontSize = 12.sp,
                fontFamily = pretendard,
                fontWeight = FontWeight(500),
                color = textColor,
            )
        )
        if (image != 0) {
            WidthSpacer(6.dp)
            Image(
                imageVector = ImageVector.vectorResource(image),
                contentDescription = "태그 버튼"
            )
        }
    }
}

@Composable
fun DrugSummaryCard(pill: TakingPillSummary) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .border(1.dp, gray200, RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        Column {
            Text(
                text = pill.medicationName,
                style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold)
            )
            HeightSpacer(6.dp)
            Text(
                text = "복약 기간: ${pill.startDate} ~ ${pill.endDate}",
                style = TextStyle(fontSize = 14.sp, color = Color.Gray)
            )
        }
    }
}
package com.pilltip.pilltip.composable.QuestionnaireComposable

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pilltip.pilltip.R
import com.pilltip.pilltip.composable.HeightSpacer
import com.pilltip.pilltip.composable.IosButton
import com.pilltip.pilltip.composable.WidthSpacer
import com.pilltip.pilltip.composable.noRippleClickable
import com.pilltip.pilltip.ui.theme.gray100
import com.pilltip.pilltip.ui.theme.gray200
import com.pilltip.pilltip.ui.theme.gray500
import com.pilltip.pilltip.ui.theme.gray600
import com.pilltip.pilltip.ui.theme.gray800
import com.pilltip.pilltip.ui.theme.pretendard

@Composable
fun InformationBox(
    header: String,
    headerColor: Color = Color.Black,
    headerSize: Int = 24,
    desc: String,
    image: Int = R.drawable.logo_pilltip_typo
) {
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
        ) {
            Image(
                imageVector = ImageVector.vectorResource(image),
                contentDescription = "설명 이미지"
            )
        }
    }
}

@Composable
fun DottedDivider(
    color: Color = Color(0xFFE2E4EC),
    thickness: Dp = 1.dp,
    dashLength: Dp = 4.dp,
    gapLength: Dp = 4.dp,
    modifier: Modifier = Modifier
        .fillMaxWidth()
        .height(thickness)
) {
    Canvas(modifier = modifier) {
        val lineY = size.height / 2
        val dashPx = dashLength.toPx()
        val gapPx = gapLength.toPx()
        var startX = 0f

        while (startX < size.width) {
            drawLine(
                color = color,
                start = Offset(x = startX, y = lineY),
                end = Offset(x = (startX + dashPx).coerceAtMost(size.width), y = lineY),
                strokeWidth = thickness.toPx()
            )
            startX += dashPx + gapPx
        }
    }
}

@Composable
fun InfoRow(
    title : String,
    desc: String
){
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = TextStyle(
                fontSize = 14.sp,
                fontFamily = pretendard,
                fontWeight = FontWeight(500),
                color = gray600,
                textAlign = TextAlign.Justify
            ),
            modifier = Modifier.width(49.dp)
        )
        WidthSpacer(20.dp)
        Text(
            text = desc,
            style = TextStyle(
                fontSize = 14.sp,
                fontFamily = pretendard,
                fontWeight = FontWeight(500),
                color = gray800,
                textAlign = TextAlign.Justify,
            )
        )
    }
}

@Composable
fun <T> QuestionnaireToggleSection(
    title: String,
    items: List<T>,
    getName: (T) -> String,
    getSubmitted: (T) -> Boolean
) {
    var expanded by remember { mutableStateOf(true) }
    val rotationDegree by animateFloatAsState(
        targetValue = if (expanded) 0f else 90f,
        animationSpec = tween(durationMillis = 300),
        label = "toggle_arrow_rotation"
    )
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = TextStyle(
                    fontSize = 14.sp,
                    fontFamily = pretendard,
                    fontWeight = FontWeight(500),
                    color = gray600
                )
            )
            Image(
                imageVector = ImageVector.vectorResource(R.drawable.btn_questionnaire_downward_arrow),
                contentDescription = "접기/펼치기",
                modifier = Modifier.rotate(rotationDegree).noRippleClickable { expanded = !expanded }
            )
        }

        if (expanded) {
            HeightSpacer(8.dp)
            items.forEach { item ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = getName(item),
                        style = TextStyle(
                            fontSize = 14.sp,
                            fontFamily = pretendard,
                            fontWeight = FontWeight(500),
                            color = gray800
                        )
                    )
                    IosButton(
                        checked = getSubmitted(item),
                        onCheckedChange = {},
                    )
                }
                HeightSpacer(12.dp)
            }
        }
    }
}
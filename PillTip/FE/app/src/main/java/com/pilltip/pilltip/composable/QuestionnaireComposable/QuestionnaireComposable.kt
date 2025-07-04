package com.pilltip.pilltip.composable.QuestionnaireComposable

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
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pilltip.pilltip.R
import com.pilltip.pilltip.composable.HeightSpacer
import com.pilltip.pilltip.composable.WidthSpacer
import com.pilltip.pilltip.composable.noRippleClickable
import com.pilltip.pilltip.ui.theme.gray100
import com.pilltip.pilltip.ui.theme.gray300
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
fun EditableProfileField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    isEditable: Boolean,
    onEditToggle: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            fontFamily = pretendard,
            fontWeight = FontWeight(500),
            color = gray600,
            modifier = Modifier.width(49.dp)
        )
        WidthSpacer(20.dp)
        if (isEditable) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(modifier = Modifier
                    .weight(1f)
                    .border(
                        width = 0.dp,
                        color = Color.Transparent
                    )) {
                    BasicTextField(
                        value = value,
                        onValueChange = onValueChange,
                        textStyle = TextStyle(
                            fontSize = 14.sp,
                            fontFamily = pretendard,
                            fontWeight = FontWeight(500),
                            color = gray800
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.CenterStart)
                    )
                }

                Text(
                    text = "수정 완료",
                    fontSize = 14.sp,
                    fontFamily = pretendard,
                    fontWeight = FontWeight(500),
                    color = gray800,
                    modifier = Modifier.noRippleClickable { onEditToggle() }
                )
            }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .noRippleClickable { onEditToggle() },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = value,
                    fontSize = 14.sp,
                    fontFamily = pretendard,
                    fontWeight = FontWeight(500),
                    color = gray800
                )
            }
        }
    }
}

@Composable
fun FixedProfiledField(
    label: String,
    value: String,
){
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            fontFamily = pretendard,
            fontWeight = FontWeight(500),
            color = gray600,
            modifier = Modifier.width(49.dp)
        )
        WidthSpacer(20.dp)
        Text(
            text = value,
            fontSize = 14.sp,
            fontFamily = pretendard,
            fontWeight = FontWeight(500),
            color = gray800
        )
    }
}
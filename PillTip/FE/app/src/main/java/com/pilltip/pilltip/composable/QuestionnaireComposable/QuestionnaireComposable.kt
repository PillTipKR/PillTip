package com.pilltip.pilltip.composable.QuestionnaireComposable

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.draw.shadow
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
import com.pilltip.pilltip.model.search.QuestionnaireSummary
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


@Composable
fun QuestionnaireCard(
    questionnaire: QuestionnaireSummary,
    onEdit: (QuestionnaireSummary) -> Unit,
    onDelete: (QuestionnaireSummary) -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .border(width = 0.5.dp, color = gray200, shape = RoundedCornerShape(size = 12.dp))
            .padding(0.25.dp)
            .fillMaxWidth()
            .height(93.dp)
            .background(color = Color(0xFFFFFFFF), shape = RoundedCornerShape(size = 12.dp))
            .padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 16.dp)
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = questionnaire.questionnaireName,
                    style = TextStyle(
                        fontSize = 14.sp,
                        fontFamily = pretendard,
                        fontWeight = FontWeight.W700,
                        color = Color.Black
                    )
                )
                Spacer(modifier = Modifier.weight(1f))
                Box {
                    Image(
                        imageVector = ImageVector.vectorResource(R.drawable.btn_vertical_dots),
                        contentDescription = "드롭다운 메뉴",
                        modifier = Modifier
                            .noRippleClickable {
                                menuExpanded = true
                            }
                    )
                    MaterialTheme(
                        shapes = MaterialTheme.shapes.copy(extraSmall = RoundedCornerShape(12.dp))
                    ) {
                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false },
                            modifier = Modifier
                                .shadow(0.dp)
                                .background(
                                    Color.White,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .border(0.5.dp, gray200, RoundedCornerShape(12.dp)),
                        ) {
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = "수정",
                                        style = TextStyle(
                                            fontSize = 14.sp,
                                            fontFamily = pretendard,
                                            fontWeight = FontWeight(400),
                                            color = Color.Black,
                                        )
                                    )
                                },
                                onClick = {
                                    menuExpanded = false
                                    //onEdit()
                                }
                            )
                            HorizontalDivider(thickness = 0.5.dp, color = gray200)
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = "삭제",
                                        style = TextStyle(
                                            fontSize = 14.sp,
                                            fontFamily = pretendard,
                                            fontWeight = FontWeight(400),
                                            color = Color.Black,
                                        )
                                    )
                                },
                                onClick = {
                                    menuExpanded = false
                                    //onDelete(pill)
                                }
                            )
                        }

                    }

                }
            }

            HeightSpacer(12.dp)
            Text(
                text = "최초 작성일 | ${questionnaire.issueDate}",
                style = TextStyle(
                    fontSize = 12.sp,
                    fontFamily = pretendard,
                    fontWeight = FontWeight.W400,
                    color = gray500
                )
            )
            HeightSpacer(4.dp)
            Text(
                text = "최종 수정일 | ${questionnaire.lastModifiedDate}",
                style = TextStyle(
                    fontSize = 12.sp,
                    fontFamily = pretendard,
                    fontWeight = FontWeight.W400,
                    color = gray500
                )
            )
        }
    }
}


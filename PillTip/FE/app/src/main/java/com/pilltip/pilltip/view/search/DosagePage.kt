package com.pilltip.pilltip.view.search

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.pilltip.pilltip.composable.AuthComposable.AgeField
import com.pilltip.pilltip.composable.AuthComposable.RoundTextField
import com.pilltip.pilltip.composable.BackButton
import com.pilltip.pilltip.composable.DoubleLineTitleText
import com.pilltip.pilltip.composable.HeightSpacer
import com.pilltip.pilltip.composable.WhiteScreenModifier
import com.pilltip.pilltip.ui.theme.gray800
import com.pilltip.pilltip.ui.theme.pretendard
import com.pilltip.pilltip.ui.theme.primaryColor

@Composable
fun DosagePage(
    navController: NavController
) {
    var startYear by remember { mutableStateOf(0) }
    var startMonth by remember { mutableStateOf(0) }
    var startDay by remember { mutableStateOf(0) }
    var endYear by remember { mutableStateOf(0) }
    var endMonth by remember { mutableStateOf(0) }
    var endDay by remember { mutableStateOf(0) }
    Column(
        modifier = WhiteScreenModifier
    ) {
        BackButton { navController.navigate("DetailPage") }
        HeightSpacer(62.dp)
        Text(
            text = "Q.",
            style = TextStyle(
                fontSize = 26.sp,
                lineHeight = 33.8.sp,
                fontFamily = pretendard,
                fontWeight = FontWeight(600),
                color = primaryColor,
            )
        )
        DoubleLineTitleText("복약 일정이", "어떻게 되시나요?", textHeight = 33.8.dp, fontSize = 26)
        HeightSpacer(52.dp)
        Text(
            text = "복약 일수",
            style = TextStyle(
                fontSize = 16.sp,
                fontFamily = pretendard,
                fontWeight = FontWeight(600),
                color = gray800,
            )
        )
        AgeField("복약 시작일") { selectedYear, selectedMonth, selectedDay ->
            startYear = selectedYear
            startMonth = selectedMonth
            startDay = selectedDay
        }
        AgeField("복약 종료일") { selectedYear, selectedMonth, selectedDay ->
            endYear = selectedYear
            endMonth = selectedMonth
            endDay = selectedDay
        }
    }
}
package com.pilltip.pilltip.view.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pilltip.pilltip.calender.CalendarView
import com.pilltip.pilltip.composable.HeightSpacer
import com.pilltip.pilltip.ui.theme.gray050
import com.pilltip.pilltip.ui.theme.gray800
import com.pilltip.pilltip.ui.theme.pretendard
import com.pilltip.pilltip.ui.theme.primaryColor
import java.time.LocalDate

@Composable
fun CalenderPage(

) {
    var selectedDate by remember { mutableStateOf<LocalDate?>(LocalDate.now()) }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(gray050)
            .statusBarsPadding()
    ) {
        Text(
            text = "복약일정",
            style = TextStyle(
                fontSize = 16.sp,
                fontFamily = pretendard,
                fontWeight = FontWeight(500),
                color = gray800,
            ),
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 18.dp)
        )
        CalendarView(
            selectedDate = selectedDate,
            onDateSelected = { selectedDate = it }
        )
        HeightSpacer(10.dp)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Color.White,
                    shape = RoundedCornerShape(
                        topStart = 30.dp,
                        topEnd = 30.dp,
                        bottomStart = 0.dp,
                        bottomEnd = 0.dp
                    )
                )
                .padding(horizontal = 22.dp)
        ) {
            HeightSpacer(36.dp)
            Text(
                text = "${selectedDate?.month}월 ${selectedDate?.dayOfMonth}일",
                style = TextStyle(
                    fontSize = 14.sp,
                    lineHeight = 21.sp,
                    fontFamily = pretendard,
                    fontWeight = FontWeight(700),
                    color = primaryColor,
                )
            )
            HeightSpacer(10.dp)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "복약 완료율",
                    style = TextStyle(
                        fontSize = 20.sp,
                        lineHeight = 30.sp,
                        fontFamily = pretendard,
                        fontWeight = FontWeight(700),
                        color = gray800,
                    )
                )
                Text(
                    text = "71%",
                    style = TextStyle(
                        fontSize = 28.sp,
                        lineHeight = 42.sp,
                        fontFamily = pretendard,
                        fontWeight = FontWeight(700),
                        color = gray800,
                    )
                )
            }
            HeightSpacer(24.dp)
            LinearProgressIndicator(
                progress = { 70 / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp),
                color = primaryColor,
                trackColor = Color(0x29787880)
            )
            HeightSpacer(36.dp)
        }
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 22.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

//            items(medicationList) { medication ->
//                MedicationCard(medication = medication)
//            }
        }
    }
}
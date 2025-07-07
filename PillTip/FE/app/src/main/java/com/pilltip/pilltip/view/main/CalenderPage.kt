package com.pilltip.pilltip.view.main

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.pilltip.pilltip.calender.CalendarView
import com.pilltip.pilltip.composable.DrugLogCard
import com.pilltip.pilltip.composable.DrugLogDetailSection
import com.pilltip.pilltip.composable.HeightSpacer
import com.pilltip.pilltip.model.search.SearchHiltViewModel
import com.pilltip.pilltip.ui.theme.gray050
import com.pilltip.pilltip.ui.theme.gray800
import com.pilltip.pilltip.ui.theme.pretendard
import com.pilltip.pilltip.ui.theme.primaryColor
import java.time.LocalDate

@Composable
fun CalenderPage(
    navController: NavController,
    hiltViewModel: SearchHiltViewModel
) {
    var selectedDate by remember { mutableStateOf<LocalDate?>(LocalDate.now()) }
    val logData by hiltViewModel.dailyDosageLog.collectAsState()
    val selectedDrugLog = hiltViewModel.selectedDrugLog

    LaunchedEffect(selectedDate) {
        selectedDate?.let {
            hiltViewModel.fetchDailyDosageLog(it)
        }
    }

    BackHandler(enabled = hiltViewModel.selectedDrugLog != null) {
        hiltViewModel.selectedDrugLog = null
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(gray050)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
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
            onDateSelected = {
                selectedDate = it
                hiltViewModel.selectedDrugLog = null
            }
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
                text = if (hiltViewModel.selectedDrugLog == null) "${selectedDate?.monthValue}월 ${selectedDate?.dayOfMonth}일"
                else hiltViewModel.selectedDrugLog!!.medicationName,
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
                val animatedPercent by animateIntAsState(
                    targetValue = if (hiltViewModel.selectedDrugLog == null)  logData?.percent ?: 0
                    else hiltViewModel.selectedDrugLog!!.percent,
                    animationSpec = tween(
                        durationMillis = 500,
                        easing = FastOutSlowInEasing
                    )
                )
                Text(
                    text = "${animatedPercent}%",
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
            val animatedProgress by animateFloatAsState(
                targetValue = if (hiltViewModel.selectedDrugLog == null) (logData?.percent ?: 0) / 100f
                else (hiltViewModel.selectedDrugLog!!.percent) / 100f,
                animationSpec = tween(
                    durationMillis = 700,
                    easing = FastOutSlowInEasing
                )
            )
            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(50)),
                color = primaryColor,
                trackColor = Color(0x29787880),
            )
            HeightSpacer(36.dp)
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 22.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (selectedDrugLog == null) {
                logData?.perDrugLogs?.forEach { drug ->
                    selectedDate?.let { DrugLogCard(drug, hiltViewModel, it) }
                }
            } else {
                selectedDate?.let { DrugLogDetailSection(selectedDrugLog, hiltViewModel, it) }
            }
        }
    }
}
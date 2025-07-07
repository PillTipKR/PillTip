package com.pilltip.pilltip.composable

import android.widget.Toast
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pilltip.pilltip.R
import com.pilltip.pilltip.model.search.DosageLogPerDrug
import com.pilltip.pilltip.model.search.DosageLogSchedule
import com.pilltip.pilltip.model.search.SearchHiltViewModel
import com.pilltip.pilltip.ui.theme.gray100
import com.pilltip.pilltip.ui.theme.gray200
import com.pilltip.pilltip.ui.theme.gray500
import com.pilltip.pilltip.ui.theme.gray900
import com.pilltip.pilltip.ui.theme.pretendard
import com.pilltip.pilltip.ui.theme.primaryColor
import com.pilltip.pilltip.ui.theme.primaryColor050
import java.time.LocalDate
import java.time.LocalTime

@Composable
fun DrugLogCard(
    drugLog: DosageLogPerDrug,
    searchHiltViewModel: SearchHiltViewModel,
    selectedDate: LocalDate
) {
    Column(
        modifier = Modifier
            .border(width = 0.5.dp, color = gray200, shape = RoundedCornerShape(size = 12.dp))
            .padding(0.25.dp)
            .fillMaxWidth()
            .background(color = Color(0xFFFFFFFF), shape = RoundedCornerShape(size = 12.dp))
            .padding(start = 20.dp, top = 24.dp, end = 20.dp, bottom = 24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = drugLog.medicationName,
                style = TextStyle(
                    fontSize = 14.sp,
                    fontFamily = pretendard,
                    fontWeight = FontWeight(600),
                    color = Color(0xFF000000),
                )
            )
            Image(
                imageVector = ImageVector.vectorResource(R.drawable.btn_right_gray_arrow),
                colorFilter = ColorFilter.tint(gray500),
                contentDescription = "해당 약품 세부 일정",
                modifier = Modifier.noRippleClickable {
                    searchHiltViewModel.selectedDrugLog = drugLog
                }
            )
        }
        HeightSpacer(28.dp)
        drugLog.dosageSchedule.forEach { schedule ->
            DosageTimeItem(schedule, searchHiltViewModel, selectedDate)
            HeightSpacer(16.dp)
        }
    }

}

@Composable
fun DosageTimeItem(
    schedule: DosageLogSchedule,
    viewModel: SearchHiltViewModel,
    selectedDate: LocalDate
) {
    val time = schedule.scheduledTime.substring(11, 16)
    val context = LocalContext.current
    val displayTime = try {
        val parsed = LocalTime.parse(time)
        when {
            parsed.hour in 0..10 -> "오전 ${parsed.hour}:${"%02d".format(parsed.minute)}"
            parsed.hour == 12 -> "오후 12:${"%02d".format(parsed.minute)}"
            else -> "오후 ${parsed.hour - 12}:${"%02d".format(parsed.minute)}"
        }
    } catch (e: Exception) {
        schedule.scheduledTime
    }

    val statusText = when {
        schedule.isTaken -> "먹었어요"
        else -> "미뤘어요"
    }

    val statusColor = when {
        schedule.isTaken -> primaryColor
        else -> gray500
    }

    val statusBackgroundColor = when {
        schedule.isTaken -> primaryColor050
        else -> gray100
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .noRippleClickable {
                viewModel.toggleDosageTaken(
                    logId = schedule.logId,
                    onSuccess = {
                        Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                        viewModel.fetchDailyDosageLog(selectedDate)
                    },
                    onError = {
                        Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                    }
                )
            },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = displayTime,
            style = TextStyle(
                fontSize = 16.sp,
                fontFamily = pretendard,
                fontWeight = FontWeight(500),
                color = gray900,
            )
        )
        Box(
            modifier = Modifier
                .background(
                    color = statusBackgroundColor,
                    shape = RoundedCornerShape(size = 1000.dp)
                )
                .padding(start = 10.dp, top = 6.dp, end = 10.dp, bottom = 6.dp)
        ) {
            Text(
                text = statusText,
                style = TextStyle(
                    fontSize = 10.sp,
                    fontFamily = pretendard,
                    fontWeight = FontWeight(500),
                    color = statusColor,
                )
            )
        }
    }
}

@Composable
fun DrugLogDetailSection(
    drug: DosageLogPerDrug,
    viewModel: SearchHiltViewModel,
    selectedDate: LocalDate
) {
    Column(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
        Text(
            text = drug.medicationName,
            style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold)
        )
        HeightSpacer(16.dp)

        drug.dosageSchedule.forEach { schedule ->
            DosageTimeItem(schedule, viewModel, selectedDate)
            HeightSpacer(16.dp)
        }
    }
}

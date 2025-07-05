package com.pilltip.pilltip.calender

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pilltip.pilltip.R
import com.pilltip.pilltip.composable.HeightSpacer
import com.pilltip.pilltip.composable.noRippleClickable
import com.pilltip.pilltip.ui.theme.gray200
import com.pilltip.pilltip.ui.theme.gray400
import com.pilltip.pilltip.ui.theme.gray800
import com.pilltip.pilltip.ui.theme.pretendard
import com.pilltip.pilltip.ui.theme.primaryColor
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun CalendarView(
    selectedDate: LocalDate? = null,
    onDateSelected: (LocalDate) -> Unit
) {
    var displayedMonth by remember { mutableStateOf(YearMonth.now()) }

    val daysInMonth = remember(displayedMonth) {
        val firstDayOfMonth = displayedMonth.atDay(1)
        val lastDay = displayedMonth.lengthOfMonth()
        val dayOfWeek = firstDayOfMonth.dayOfWeek.value % 7

        val prevPadding = List(dayOfWeek) { "" }
        val days = (1..lastDay).map { it.toString() }
        (prevPadding + days).chunked(7)
    }

    val calendarDays = remember(displayedMonth) {
        val firstDayOfMonth = displayedMonth.atDay(1)
        val lastDayOfMonth = displayedMonth.atEndOfMonth()
        val startDayOfWeek = firstDayOfMonth.dayOfWeek.value % 7
        val days = mutableListOf<CalendarDay>()
        val prevMonth = displayedMonth.minusMonths(1)
        val prevMonthEnd = prevMonth.atEndOfMonth().dayOfMonth
        val totalCells = 42
        for (i in startDayOfWeek downTo 1) {
            days.add(
                CalendarDay(
                    date = prevMonth.atDay(prevMonthEnd - i + 1),
                    isCurrentMonth = false
                )
            )
        }
        for (i in 1..lastDayOfMonth.dayOfMonth) {
            days.add(
                CalendarDay(
                    date = displayedMonth.atDay(i),
                    isCurrentMonth = true
                )
            )
        }
        val nextMonth = displayedMonth.plusMonths(1)
        for (i in 1..(totalCells - days.size)) {
            days.add(
                CalendarDay(
                    date = nextMonth.atDay(i),
                    isCurrentMonth = false
                )
            )
        }
        days.chunked(7)
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .pointerInput(Unit) {
                var triggered = false
                detectHorizontalDragGestures(
                    onDragEnd = { triggered = false }
                ) { _, dragAmount ->
                    if (!triggered) {
                        when {
                            dragAmount > 30 -> {
                                displayedMonth = displayedMonth.minusMonths(1)
                                triggered = true
                            }

                            dragAmount < -30 -> {
                                displayedMonth = displayedMonth.plusMonths(1)
                                triggered = true
                            }
                        }
                    }
                }
            }
            .padding(horizontal = 22.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            Image(
                imageVector = ImageVector.vectorResource(R.drawable.btn_calender_previous),
                contentDescription = "전월 이동",
                modifier = Modifier.noRippleClickable {
                    displayedMonth = displayedMonth.minusMonths(1)
                }
            )
            Text(
                text = displayedMonth.format(DateTimeFormatter.ofPattern("yyyy년 MMMM")),
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
            Image(
                imageVector = ImageVector.vectorResource(R.drawable.btn_calender_next),
                contentDescription = "익월 이동",
                modifier = Modifier.noRippleClickable {
                    displayedMonth = displayedMonth.plusMonths(1)
                }
            )
        }
        HeightSpacer(12.dp)
        Row(
            Modifier
                .height(48.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            listOf("일", "월", "화", "수", "목", "금", "토").forEach {
                Text(
                    text = it,
                    style = androidx.compose.ui.text.TextStyle(
                        fontSize = 14.sp,
                        fontFamily = pretendard,
                        fontWeight = FontWeight(500),
                        color = gray400,
                        textAlign = TextAlign.Center,
                    ),
                    modifier = Modifier.width(40.dp)
                )
            }
        }
        calendarDays.forEach { week ->
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 3.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                week.forEach { day ->
                    val isSelected = selectedDate == day.date
                    val backgroundColor = when {
                        isSelected -> primaryColor
                        selectedDate == null && day.date == LocalDate.now() -> primaryColor
                        else -> Color.Transparent
                    }
                    val textColor = when {
                        isSelected -> Color.White
                        selectedDate == null && day.date == LocalDate.now() -> Color.White
                        !day.isCurrentMonth -> gray200
                        else -> gray800
                    }
                    Box(
                        modifier = Modifier
                            .width(40.dp)
                            .height(40.dp)
                            .background(
                                color = backgroundColor,
                                shape = RoundedCornerShape(16.dp)
                            )
                            .noRippleClickable {
                                onDateSelected(day.date)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = day.date.dayOfMonth.toString(),
                            style = androidx.compose.ui.text.TextStyle(
                                fontSize = 16.sp,
                                fontFamily = pretendard,
                                fontWeight = FontWeight(500),
                                color = textColor
                            )
                        )
                    }
                }
            }
        }
    }
}
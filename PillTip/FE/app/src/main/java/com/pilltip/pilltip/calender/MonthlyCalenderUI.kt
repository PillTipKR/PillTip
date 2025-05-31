package com.pilltip.pilltip.calender

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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.pilltip.pilltip.composable.HeightSpacer
import com.pilltip.pilltip.composable.noRippleClickable
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun MonthlyCalendar(
    yearMonth: YearMonth,
    selectedDate: LocalDate?,
    events: Set<LocalDate> = emptySet(),
    onDateSelected: (LocalDate) -> Unit
) {
    val today = LocalDate.now()
    val days = remember(yearMonth) { generateCalendarDays(yearMonth) }

    Column {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            DayOfWeek.values().forEach {
                Text(
                    text = it.getDisplayName(TextStyle.SHORT, Locale.getDefault()),
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
            }
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            userScrollEnabled = false
        ) {
            items(items = days) { date ->
                Box(
                    modifier = Modifier
                        .aspectRatio(1f)
                        .padding(2.dp)
                        .border(
                            1.dp,
                            when {
                                date == null -> Color.Transparent
                                date == selectedDate -> Color.Blue
                                date == today -> Color.LightGray
                                else -> Color.Transparent
                            },
                            RoundedCornerShape(8.dp)
                        )
                        .clickable(enabled = date != null) {
                            date?.let(onDateSelected)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = date?.dayOfMonth?.toString() ?: "",
                            color = when {
                                date == selectedDate -> Color.Gray
                                else -> Color.Black
                            },
                            style = MaterialTheme.typography.bodyMedium
                        )
                        if (date != null && events.contains(date)) {
                            Spacer(Modifier.height(2.dp))
                            Box(
                                Modifier
                                    .size(6.dp)
                                    .background(Color.Red, CircleShape)
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen() {
    val sheetState = rememberModalBottomSheetState()
    val coroutineScope = rememberCoroutineScope()

    var currentMonth by remember { mutableStateOf(YearMonth.now()) }
    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }

    var showBottomSheet by remember { mutableStateOf(false) }

    val eventDates = remember {
        setOf(
            LocalDate.of(2025, 5, 10),
            LocalDate.of(2025, 5, 14)
        )
    }

    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showBottomSheet = false },
            sheetState = sheetState
        ) {
            MonthYearPicker(
                initialMonth = currentMonth,
                onSelect = {
                    currentMonth = it
                    coroutineScope.launch { sheetState.hide() }
                    showBottomSheet = false
                }
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                var triggered = false
                detectHorizontalDragGestures(
                    onDragEnd = { triggered = false }
                ) { _, dragAmount ->
                    if (!triggered) {
                        when {
                            dragAmount > 30 -> {
                                currentMonth = currentMonth.minusMonths(1)
                                triggered = true
                            }

                            dragAmount < -30 -> {
                                currentMonth = currentMonth.plusMonths(1)
                                triggered = true
                            }
                        }
                    }
                }
            }
    ) {
        HeightSpacer(50.dp)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = { currentMonth = currentMonth.minusMonths(1) }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Previous")
            }
            IconButton(onClick = { currentMonth = currentMonth.plusMonths(1) }) {
                Icon(Icons.Default.ArrowForward, contentDescription = "Next")
            }
        }

        // 👇 텍스트 클릭 시 BottomSheet 열기
        Text(
            text = "${currentMonth.year}년 ${currentMonth.monthValue}월",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier
                .fillMaxWidth()
                .noRippleClickable { showBottomSheet = true }
                .padding(vertical = 16.dp),
            textAlign = TextAlign.Center
        )

        MonthlyCalendar(
            yearMonth = currentMonth,
            selectedDate = selectedDate,
            events = eventDates,
            onDateSelected = { selectedDate = it }
        )
    }
}

@Preview
@Composable
fun CalenderScreenPreview() {
    CalendarScreen()
}
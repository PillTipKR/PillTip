package com.pilltip.pilltip.calender

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import java.time.YearMonth

fun generateCalendarDays(yearMonth: YearMonth): List<LocalDate?> {
    val firstDay = yearMonth.atDay(1)
    val firstDayOfWeek = (firstDay.dayOfWeek.value % 7) // Sunday = 0
    val daysInMonth = yearMonth.lengthOfMonth()
    val totalCells = firstDayOfWeek + daysInMonth

    return List(totalCells) { index ->
        val offset = index - firstDayOfWeek
        if (offset >= 0) yearMonth.atDay(offset + 1) else null
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MonthYearPicker(
    initialMonth: YearMonth,
    onSelect: (YearMonth) -> Unit
) {
    val yearList = listOf(null, null) + (2000..2030).toList() + listOf(null, null)
    val monthList = listOf(null, null) + (1..12).toList() + listOf(null, null)

    val yearState = rememberLazyListState(
        initialFirstVisibleItemIndex = yearList.indexOf(initialMonth.year)
    )
    val monthState = rememberLazyListState(
        initialFirstVisibleItemIndex = initialMonth.monthValue - 1
    )

    val centeredYear = remember {
        derivedStateOf {
            val visibleItems = yearState.layoutInfo.visibleItemsInfo
            if (visibleItems.isNotEmpty()) {
                val centerItem = visibleItems[visibleItems.size / 2]
                yearList.getOrNull(centerItem.index) ?: initialMonth.year
            } else {
                initialMonth.year
            }
        }
    }

    val centeredMonth = remember {
        derivedStateOf {
            val visibleItems = monthState.layoutInfo.visibleItemsInfo
            if (visibleItems.isNotEmpty()) {
                val centerItem = visibleItems[visibleItems.size / 2]
                monthList.getOrNull(centerItem.index) ?: initialMonth.monthValue
            } else {
                initialMonth.monthValue
            }
        }
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(250.dp)
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        LazyColumn(
            state = yearState,
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(vertical = 80.dp),
            flingBehavior = rememberSnapFlingBehavior(lazyListState = yearState)
        ) {
            items(yearList.size) { index ->
                val year = yearList[index]
                Text(
                    text = year?.toString() ?: "",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    textAlign = TextAlign.Center,
                    color = if (year != null && year == centeredYear.value) Color.Blue else Color.Gray,
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }

        LazyColumn(
            state = monthState,
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(vertical = 80.dp),
            flingBehavior = rememberSnapFlingBehavior(lazyListState = monthState)
        ) {
            items(monthList.size) { index ->
                val month = monthList[index]
                Text(
                    text = month?.toString() ?: "",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    textAlign = TextAlign.Center,
                    color = if (month != null && month == centeredMonth.value) Color.Blue else Color.Gray,
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    Button(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        onClick = {
            onSelect(YearMonth.of(centeredYear.value, centeredMonth.value))
        }
    ) {
        Text("선택 완료")
    }
}
package com.pilltip.pilltip.view.search

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.pilltip.pilltip.composable.HeightSpacer
import com.pilltip.pilltip.composable.search.AutoCompleteList
import com.pilltip.pilltip.composable.search.PillSearchField
import com.pilltip.pilltip.composable.search.SearchTag
import com.pilltip.pilltip.model.search.AutoCompleteHiltViewModel
import com.pilltip.pilltip.model.search.LogViewModel
import com.pilltip.pilltip.ui.theme.gray500
import com.pilltip.pilltip.ui.theme.gray700
import com.pilltip.pilltip.ui.theme.pretendard
import com.pilltip.pilltip.ui.theme.primaryColor
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter

@OptIn(ExperimentalLayoutApi::class, FlowPreview::class)
@Composable
fun SearchPage(
    navController: NavController,
    LogViewModel: LogViewModel,
    AutoCompleteViewModel : AutoCompleteHiltViewModel = hiltViewModel()
) {
    var inputText by remember { mutableStateOf("") }
    val recentSearches by LogViewModel.recentSearches.collectAsState()
    val autoCompleted by AutoCompleteViewModel.autoCompleted.collectAsState()

    val isLoading by AutoCompleteViewModel.isLoading.collectAsState()

    LaunchedEffect(Unit) {
        snapshotFlow { inputText }
            .debounce(700)
            .distinctUntilChanged()
            .filter { it.isNotEmpty() }
            .collect { debouncedText ->
                if (debouncedText.isNotBlank()) {
                    AutoCompleteViewModel.fetchAutoComplete(debouncedText, reset = true)
                }
            }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFDFDFD))
            .padding(horizontal = 22.dp, vertical = 18.dp)
            .padding(WindowInsets.statusBars.asPaddingValues())
    ) {
        PillSearchField(
            navController,
            LogViewModel,
            nowTyping = { inputText = it },
            searching = { inputText = it }
        )
        HeightSpacer(28.dp)
        if(inputText.isEmpty()){
            Text(
                text = "인기 검색어",
                style = TextStyle(
                    fontSize = 14.sp,
                    fontFamily = pretendard,
                    fontWeight = FontWeight(600),
                    color = gray700,
                )
            )
            HeightSpacer(26.dp)
            Text(
                text = "최근 검색어",
                style = TextStyle(
                    fontSize = 14.sp,
                    fontFamily = pretendard,
                    fontWeight = FontWeight(600),
                    color = gray700,
                )
            )
            HeightSpacer(18.dp)
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                recentSearches.forEach { keyword ->
                    SearchTag(keyword, onClick = { LogViewModel.deleteSearchQuery(keyword) })
                }
            }
        } else {
            Box(modifier = Modifier.fillMaxSize()) {
                AutoCompleteList(
                    query = inputText,
                    searched = autoCompleted,
                    onClick = { selected ->
                        inputText = selected.value
                    },
                    onLoadMore = {
                        AutoCompleteViewModel.fetchAutoComplete(inputText)
                    }
                )

                if (isLoading) {
                    HeightSpacer(40.dp)
                    CircularProgressIndicator(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .size(32.dp),
                        color = primaryColor,
                        strokeWidth = 3.dp
                    )
                }

                if (!isLoading && autoCompleted.isEmpty()) {
                    HeightSpacer(40.dp)
                    Text(
                        text = "검색 결과가 없어요",
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 20.dp),
                        style = TextStyle(
                            fontSize = 14.sp,
                            fontFamily = pretendard,
                            fontWeight = FontWeight.Medium,
                            color = gray500
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun SearchResultsPage(
    navController: NavController
){
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFDFDFD))
            .padding(horizontal = 22.dp, vertical = 18.dp)
            .padding(WindowInsets.statusBars.asPaddingValues())
    ) {
    }
}











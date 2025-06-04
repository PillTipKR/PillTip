package com.pilltip.pilltip.view.search

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.pilltip.pilltip.composable.HeightSpacer
import com.pilltip.pilltip.composable.SearchComposable.AutoCompleteList
import com.pilltip.pilltip.composable.SearchComposable.DrugSearchResultList
import com.pilltip.pilltip.composable.SearchComposable.PillSearchField
import com.pilltip.pilltip.composable.SearchComposable.SearchTag
import com.pilltip.pilltip.model.search.LogViewModel
import com.pilltip.pilltip.model.search.SearchHiltViewModel
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
    logViewModel: LogViewModel,
    searchViewModel: SearchHiltViewModel
) {
    var inputText by remember { mutableStateOf("") }
    val recentSearches by logViewModel.recentSearches.collectAsState()
    val autoCompleted by searchViewModel.autoCompleted.collectAsState()
    val isLoading by searchViewModel.isAutoCompleteLoading.collectAsState()

    LaunchedEffect(Unit) {
        snapshotFlow { inputText }
            .debounce(700)
            .distinctUntilChanged()
            .filter { it.isNotEmpty() }
            .collect { debouncedText ->
                if (debouncedText.isNotBlank()) {
                    searchViewModel.fetchAutoComplete(debouncedText, reset = true)
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
            "",
            navController,
            nowTyping = { inputText = it },
            searching = { inputText = it },
            onNavigateToResult = { query ->
                logViewModel.addSearchQuery(inputText)
                searchViewModel.fetchDrugSearch(query)
                navController.navigate("SearchResultsPage/${query}")
                Log.d("Query: ", query)
            }
        )
        HeightSpacer(28.dp)
        if (inputText.isEmpty()) {
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
                    SearchTag(
                        keyword,
                        onNavigateToResult = {
                            logViewModel.addSearchQuery(keyword)
                            searchViewModel.fetchDrugSearch(keyword)
                            navController.navigate("SearchResultsPage/${keyword}")
                        },
                        onDelete = { logViewModel.deleteSearchQuery(keyword) }
                    )
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
                        searchViewModel.fetchAutoComplete(inputText)
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
    navController: NavController,
    logViewModel: LogViewModel,
    searchViewModel: SearchHiltViewModel,
    initialQuery: String
) {
    Log.d("initialQuery: ", initialQuery)
    var inputText by remember { mutableStateOf(initialQuery) }
    var isDropdownExpanded by remember { mutableStateOf(false) }

    val autoCompleted by searchViewModel.autoCompleted.collectAsState()
    val isLoading by searchViewModel.isLoading.collectAsState()
    val isAutoCompleteLoading by searchViewModel.isAutoCompleteLoading.collectAsState()
    val searchResults by searchViewModel.drugSearchResults.collectAsState()
    var hasUserTyped by remember { mutableStateOf(false) }

    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val dropdownWidth = screenWidth - 44.dp

    LaunchedEffect(inputText, hasUserTyped) {
        if (!hasUserTyped) return@LaunchedEffect

        snapshotFlow { inputText }
            .debounce(600)
            .distinctUntilChanged()
            .filter { it.isNotBlank() }
            .collect {
                searchViewModel.fetchAutoComplete(it, reset = true)
                isDropdownExpanded = true
            }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFDFDFD))
            .padding(horizontal = 22.dp, vertical = 18.dp)
            .padding(WindowInsets.statusBars.asPaddingValues())
    ) {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            PillSearchField(
                initialQuery,
                navController = navController,
                nowTyping = {
                    inputText = it
                    if (!hasUserTyped) hasUserTyped = true
                },
                searching = {
                    inputText = it
                    if (!hasUserTyped) hasUserTyped = true
                },
                onNavigateToResult = { query ->
                    logViewModel.addSearchQuery(query)
                    searchViewModel.fetchDrugSearch(query)
                    isDropdownExpanded = false
                }
            )

            DropdownMenu(
                expanded = isDropdownExpanded,
                onDismissRequest = { isDropdownExpanded = false },
                modifier = Modifier
                    .width(dropdownWidth)
                    .background(Color.White)
            ) {
                when {
                    isAutoCompleteLoading -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp,
                                color = primaryColor
                            )
                        }
                    }

                    autoCompleted.isEmpty() -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "검색 결과가 없어요",
                                style = TextStyle(
                                    fontSize = 14.sp,
                                    fontFamily = pretendard,
                                    fontWeight = FontWeight.Medium,
                                    color = gray500
                                )
                            )
                        }
                    }

                    else -> {
                        autoCompleted.forEach { result ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = result.value,
                                        fontSize = 14.sp,
                                        fontFamily = pretendard
                                    )
                                },
                                onClick = {
                                    inputText = result.value
                                    searchViewModel.fetchDrugSearch(result.value)
                                    isDropdownExpanded = false
                                }
                            )
                        }
                    }
                }
            }
        }

        HeightSpacer(16.dp)

        when {
            isLoading -> {
                Box(modifier = Modifier.fillMaxSize()) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
            }

            searchResults.isEmpty() -> {
                Box(modifier = Modifier.fillMaxSize()) {
                    Text(
                        text = "검색 결과가 없어요",
                        modifier = Modifier.align(Alignment.Center),
                        style = TextStyle(
                            fontSize = 14.sp,
                            fontFamily = pretendard,
                            fontWeight = FontWeight.Medium,
                            color = gray500
                        )
                    )
                }
            }

            else -> {
                DrugSearchResultList(drugs = searchResults)
            }
        }
    }
}











package com.pilltip.pilltip.view.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.pilltip.pilltip.composable.MainComposable.BottomBar
import com.pilltip.pilltip.composable.MainComposable.BottomTab
import com.pilltip.pilltip.composable.MainComposable.LogoField
import com.pilltip.pilltip.composable.MainComposable.MainSearchField
import com.pilltip.pilltip.ui.theme.backgroundColor

@Composable
fun PillMainPage() {
    var selectedTab by remember { mutableStateOf(BottomTab.Home) }
    val systemUiController = rememberSystemUiController()
    SideEffect {
        systemUiController.setStatusBarColor(
            color = Color.Transparent,
            darkIcons = true
        )
    }
    Scaffold(
        bottomBar = {
            BottomBar(
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it }
            )
        },
        containerColor = backgroundColor
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(backgroundColor)
        ) {
            when (selectedTab) {
                BottomTab.Home -> HomePage()
                BottomTab.Interaction -> InteractionPage()
                BottomTab.Chart -> ChartPage()
                BottomTab.Calendar -> CalendarPage()
                BottomTab.MyPage -> MyPage()
            }
        }
    }
}

@Composable fun HomePage() {
    LogoField()
    MainSearchField()

}

@Composable fun InteractionPage() {
    Text("상충 비교 화면")
}

@Composable fun ChartPage() {
    Text("차트 화면")
}

@Composable fun CalendarPage() {
    Text("캘린더 화면")
}

@Composable fun MyPage() {
    Text("마이페이지")
}
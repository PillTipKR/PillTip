package com.pilltip.pilltip.view.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.pilltip.pilltip.composable.HeightSpacer
import com.pilltip.pilltip.composable.LogoField
import com.pilltip.pilltip.composable.WhiteScreenModifier
import com.pilltip.pilltip.ui.theme.primaryColor

@Composable
fun PillMainPage(

){
    val systemUiController = rememberSystemUiController()

    // 상태바 배경색 및 아이콘 밝기 설정
    systemUiController.setStatusBarColor(
        color = primaryColor,       // 상태바 배경색
        darkIcons = true           // 아이콘을 어둡게 (글자가 검정색처럼 보임)
    )
    Column(
        modifier = WhiteScreenModifier
            .padding(WindowInsets.statusBars.asPaddingValues())
    ){
        LogoField()
        HeightSpacer(16.dp)
    }
}
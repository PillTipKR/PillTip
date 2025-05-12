package com.pilltip.pilltip.composable

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp

/**
 * 세로 간격을 띄우기 위한 Spacer입니다.
 * @param height (Dp)세로 간격을 지정합니다.
 * @author 김기윤
 */
@Composable
fun HeightSpacer(height: Dp) {
    Spacer(modifier = Modifier.height(height))
}

/**
 * 가로 간격을 띄우기 위한 Spacer입니다.
 * @param width (Dp)가로 간격을 지정합니다.
 * @author 김기윤
 */
@Composable
fun WidthSpacer(width: Dp) {
    Spacer(modifier = Modifier.width(width))
}
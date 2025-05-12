package com.pilltip.pilltip.composable

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * 흰 화면을 구성하기 위한 Custom Modifier입니다.
 * @author 김기윤
 */
val WhiteScreenModifier = Modifier
    .background(color = Color.White)
    .fillMaxSize()

/**
 * 앱 전반적으로 활용하는 버튼 관련 Custom Modifier입니다.
 * @author 김기윤
 */
val buttonModifier = Modifier
    .fillMaxWidth()
    .padding(horizontal = 24.dp, vertical = 16.dp)
    .padding(bottom = 46.dp)
    .height(58.dp)

/**
 * Clickable한 Composable의 기본 effect를 제거할 때 활용하는 간편한 method입니다.
 * @author 김기윤
 */
fun Modifier.noRippleClickable(onClick: () -> Unit): Modifier = composed {
    this.clickable(
        onClick = onClick,
        indication = null,
        interactionSource = remember { MutableInteractionSource() }
    )
}
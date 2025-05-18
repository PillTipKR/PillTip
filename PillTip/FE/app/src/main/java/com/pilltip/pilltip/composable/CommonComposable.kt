package com.pilltip.pilltip.composable

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pilltip.pilltip.R
import com.pilltip.pilltip.ui.theme.pretendard
import com.pilltip.pilltip.view.logic.InputType

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

/**
 * 하이라이트 라인을 표시하는 Composable입니다.
 * @param text : String | 텍스트 입력 여부를 받습니다.
 * @param isFocused : Boolean | 포커스 여부를 받습니다.
 * @param isAllConditionsValid : Boolean | 모든 조건이 만족되었는지 여부를 받습니다.
 */
@Composable
fun HighlightingLine(text: String, isFocused: Boolean, isAllConditionsValid: Boolean = true) {
    val fillPercentage = if (isFocused) 1f else 0f
    val animatedFillPercentage by animateFloatAsState(targetValue = fillPercentage, label = "")

    if (!isFocused) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .padding(horizontal = 24.dp)
                .background(Color(0xFFBFBFBF))
        )
    } else {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(2.dp)
                .padding(horizontal = 24.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction = animatedFillPercentage)
                    .height(2.dp)
                    .background(
                        if (text.isEmpty()) Color(0xFF397CDB)
                        else {
                            if (isAllConditionsValid)
                                Color(0xFF397CDB)
                            else
                                Color(0xFFE43D45)
                        }
                    )
            )
        }
    }
}

/**
 * 회원가입 페이지에서 조건 만족 여부를 시각화할 때 사용하는 Composable입니다.
 * @param description : String | 조건을 작성합니다.
 * @param isValid : Boolean | 조건을 만족하는지 여부를 판별하는 값입니다.
 */
@Composable
fun Guideline(description: String, isValid: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 28.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Box(
            modifier = Modifier
                .padding(horizontal = 5.dp)
                .height(3.dp)
                .width(3.dp)
                .background(color = Color(0xFFBFBFBF), shape = CircleShape),
        )
        Text(
            text = description,
            fontFamily = pretendard,
            fontSize = 14.sp,
            fontWeight = FontWeight.W400,
            color = Color(0xFFBFBFBF),
            style = TextStyle(
                platformStyle = PlatformTextStyle(
                    includeFontPadding = false
                )
            ),
            letterSpacing = (-0.3).sp,
            modifier = Modifier
                .weight(1f)
        )
        Image(
            imageVector = if (isValid) {
                ImageVector.vectorResource(id = R.drawable.ic_green_checkmark)
            } else {
                ImageVector.vectorResource(id = R.drawable.ic_red_checkmark)
            },
            contentDescription = "status_icon",
            modifier = Modifier.size(18.dp)
        )
    }
}

/**
 * 두 줄에 걸쳐 작성되는 Title Text Composable입니다.
 * @param upperTextLine 윗 줄에 작성되는 Title Text입니다.
 * @param lowerTextLine 아랫 줄에 작성되는 Title Text입니다.
 * @param padding : Dp | Title Text의 horizontal 패딩을 설정합니다.
 */
@Composable
fun DoubleLineTitleText(
    upperTextLine: String = "Upper TextLine", lowerTextLine: String = "Lower TextLine", padding: Dp = 24.dp
) {
    Text(
        text = upperTextLine,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = padding)
            .height(40.dp)
            .wrapContentHeight(Alignment.CenterVertically),
        style = TextStyle(
            fontFamily = pretendard,
            fontWeight = FontWeight.Bold,
            fontSize = 28.sp,
            letterSpacing = (-0.3).sp,
            platformStyle = PlatformTextStyle(
                includeFontPadding = false
            )
        )
    )
    Text(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 24.dp, end = 24.dp)
            .height(40.dp)
            .wrapContentHeight(Alignment.CenterVertically),
        text = lowerTextLine,
        fontSize = 28.sp,
        fontFamily = pretendard,
        fontWeight = FontWeight.Bold,
        color = Color(0xFF000000),
        letterSpacing = (-0.3).sp,
    )
}

/**
 * Title 아래 세부 설명사항을 작성하는 Composable입니다.
 * @param description : String | 세부 설명사항을 작성합니다.
 */
@Composable
fun TitleDescription(description: String = "TitleDescription") {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 24.dp, end = 24.dp)
    ) {
        Text(
            text = description,
            fontSize = 16.sp,
            fontFamily = pretendard,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFFAFB8C1)
        )
    }
}

/**
 * 텍스트를 작성하면 나타나는 라벨 텍스트를 설정하는 Composable입니다.
 * @param labelText : String | 라벨 텍스트를 설정합니다.
 * @param padding : Dp | 라벨 텍스트의 start 패딩을 설정합니다.
 * @param bottomPadding : Dp | 라벨 텍스트의 bottom 패딩을 설정합니다.
 */
@Composable
fun LabelText(labelText: String = "", padding: Dp = 24.dp, bottomPadding: Dp = 8.dp) {
    Text(
        text = labelText,
        fontSize = 12.sp,
        fontFamily = pretendard,
        fontWeight = FontWeight.Medium,
        color = Color(0xFFAFB8C1),
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = padding)
            .padding(bottom = bottomPadding)
    )
}

/**
 * place holder를 지원하는 TextField Composable입니다.
 * @param placeHolder : String | 텍스트 필드의 place holder를 설정합니다.
 * @param inputText : String | 텍스트 필드에 입력된 텍스트를 설정합니다.
 * @param padding : Dp | 텍스트 필드의 horizontal 패딩을 설정합니다.
 * @param inputType : InputType | 텍스트 필드의 입력 타입을 설정합니다. TEXT, EMAIL, PASSWORD, NUMBER 중 하나를 선택하여 입력합니다.
 * @param onTextChanged : (String) -> Unit | 텍스트 필드의 텍스트가 변경될 때 호출되는 콜백 함수입니다.
 * @param onFocusChanged : (Boolean) -> Unit | 텍스트 필드의 포커스 여부가 변경될 때 호출되는 콜백 함수입니다.
 */
@Composable
fun PlaceholderTextField(
    placeHolder: String = "",
    inputText: String,
    padding: Dp = 24.dp,
    inputType: InputType = InputType.TEXT,
    onTextChanged: (String) -> Unit,
    onFocusChanged: (Boolean) -> Unit
) {
    var isFocused by remember { mutableStateOf(false) }

    BasicTextField(
        value = inputText,
        onValueChange = {
            onTextChanged(it)
        },
        cursorBrush = SolidColor(if (isFocused) Color.Blue else Color.Gray),
        keyboardOptions = if (inputType == InputType.TEXT)
            KeyboardOptions(
                imeAction = ImeAction.Next
            )
        else if (inputType == InputType.EMAIL)
            KeyboardOptions(
                imeAction = ImeAction.Next,
                keyboardType = KeyboardType.Email
            )
        else if(inputType == InputType.PASSWORD) KeyboardOptions(
            keyboardType = KeyboardType.Password,
            imeAction = ImeAction.Next
        ) else {
            KeyboardOptions(
                imeAction = ImeAction.Next,
                keyboardType = KeyboardType.Number
            )
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = padding)
            .focusable()
            .onFocusChanged { focusState ->
                isFocused = focusState.isFocused
                onFocusChanged(isFocused)
            },
        textStyle = TextStyle(
            fontSize = 20.sp,
            fontFamily = pretendard,
            fontWeight = FontWeight(if (inputType == InputType.TEXT) 600 else 500),
            color = Color(0xFF121212),
            letterSpacing = 0.5.sp,
        ),
        decorationBox = { innerTextField ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                if (inputText.isEmpty()) {
                    Text(
                        modifier = Modifier
                            .fillMaxWidth(),
                        text = placeHolder,
                        style = TextStyle(
                            fontFamily = pretendard,
                            fontWeight = FontWeight.Normal,
                            fontSize = 18.sp,
                            color = Color(0xFFAFB8C1)
                        )
                    )
                }
                innerTextField()
                if (inputText.isNotEmpty()) {
                    Image(
                        imageVector = ImageVector.vectorResource(id = R.drawable.btn_textfield_eraseall),
                        contentDescription = "x_marker",
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .padding(end = 16.dp)
                            .clickable {
                                onTextChanged("")
                            }
                    )
                }
            }
        }
    )
}
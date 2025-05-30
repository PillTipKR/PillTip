package com.pilltip.pilltip.view.auth.composable

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pilltip.pilltip.R
import com.pilltip.pilltip.composable.WidthSpacer
import com.pilltip.pilltip.ui.theme.gray200
import com.pilltip.pilltip.ui.theme.gray500
import com.pilltip.pilltip.ui.theme.gray800
import com.pilltip.pilltip.ui.theme.pretendard
import com.pilltip.pilltip.ui.theme.primaryColor

@Composable
fun ProfileStepDescription(
    Title: String
) {
    Text(
        text = Title,
        fontSize = 16.sp,
        fontFamily = pretendard,
        fontWeight = FontWeight(600),
        color = gray800
    )
}

@Composable
fun NicknameField(
    nickname: String,
    nicknameChange: (String) -> Unit
){
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusRequester = remember { FocusRequester() }
    var isFocused by remember { mutableStateOf(false) }
    BasicTextField(
        value = nickname,
        onValueChange = {
            if(it.length<=15) nicknameChange(it)
        },
        modifier = Modifier
            .border(
                width = 1.dp,
                color = gray200,
                shape = RoundedCornerShape(size = 12.dp)
            )
            .fillMaxWidth()
            .height(51.dp)
            .padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 16.dp)
            .focusRequester(focusRequester)
            .onFocusChanged { focusState ->
                isFocused = focusState.isFocused
            },
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(
            onDone = { keyboardController?.hide() }
        ),
        textStyle = TextStyle(
            fontSize = 17.sp,
            color = Color.Black,
            fontFamily = pretendard,
            fontWeight = FontWeight.W500
        ),
        decorationBox = { innerTextField ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(22.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                if (nickname.isEmpty()) {
                    Text(
                        text = "닉네임을 입력해주세요",
                        style = TextStyle(
                            fontSize = 16.sp,
                            fontFamily = pretendard,
                            fontWeight = FontWeight.W400,
                            color = gray500
                        )
                    )
                }
                innerTextField()
            }
        },
    )
}

@Composable
fun ProfileGenderPick(
    select: (String) -> Unit
) {
    var gender by remember{ mutableStateOf("")}
    Row(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .border(width = 1.dp, color = if (gender == "FEMALE") primaryColor else gray500,  shape = RoundedCornerShape(size = 12.dp))
                .weight(1f)
                .height(43.dp)
                .background(color = if (gender == "FEMALE") Color(0xFFF1F6FE) else Color(0xFFE2E4EC), shape = RoundedCornerShape(size = 12.dp))
                .padding(start = 0.dp, top = 12.dp, end = 0.dp, bottom = 12.dp)
                .clickable { gender = "FEMALE"
                    select(gender)
                }
        ) {
            Text(
                text = "여성",
                fontSize = 16.sp,
                fontFamily = pretendard,
                fontWeight = FontWeight(500),
                color = gray800
            )
        }
        WidthSpacer(8.dp)
        Box(
            modifier = Modifier
                .border(width = 1.dp, color = if (gender == "MALE") primaryColor else gray500, shape = RoundedCornerShape(size = 12.dp))
                .weight(1f)
                .height(43.dp)
                .background(color = if (gender == "MALE") Color(0xFFF1F6FE) else Color(0xFFE2E4EC), shape = RoundedCornerShape(size = 12.dp))
                .padding(start = 0.dp, top = 12.dp, end = 0.dp, bottom = 12.dp)
                .clickable {
                    gender = "MALE"
                    select(gender)
                }
        ) {
            Text(
                text = "남성",
                fontSize = 16.sp,
                fontFamily = pretendard,
                fontWeight = FontWeight(500),
                color = gray800
            )
        }
    }
}
//개발 중
//@Composable
//fun AgeField(
//    nickname: String,
//    nicknameChange: (String) -> Unit
//){
//    val keyboardController = LocalSoftwareKeyboardController.current
//    val focusRequester = remember { FocusRequester() }
//    var isFocused by remember { mutableStateOf(false) }
//    BasicTextField(
//        value = nickname,
//        onValueChange = {
//            if(it.length<=15) nicknameChange(it)
//        },
//        modifier = Modifier
//            .border(
//                width = 1.dp,
//                color = gray200,
//                shape = RoundedCornerShape(size = 12.dp)
//            )
//            .fillMaxWidth()
//            .height(51.dp)
//            .padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 16.dp)
//            .focusRequester(focusRequester)
//            .onFocusChanged { focusState ->
//                isFocused = focusState.isFocused
//            },
//        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
//        keyboardActions = KeyboardActions(
//            onDone = { keyboardController?.hide() }
//        ),
//        textStyle = TextStyle(
//            fontSize = 17.sp,
//            color = Color.Black,
//            fontFamily = pretendard,
//            fontWeight = FontWeight.W500
//        ),
//        decorationBox = { innerTextField ->
//            Row(
//                modifier = Modifier.fillMaxWidth(),
//                verticalAlignment = Alignment.CenterVertically,
//                horizontalArrangement = Arrangement.SpaceBetween
//            ) {
//                Box(Modifier.weight(1f)) {
//                    if (text.isEmpty()) {
//                        Text(
//                            "생년월일을 입력해주세요",
//                            color = Color.Gray,
//                            fontSize = 16.sp
//                        )
//                    }
//                    innerTextField()
//                }
//
//                // SVG 아이콘 표시 (예: btn_right_gray_arrow.svg)
//                Icon(
//                    painter = painterResource(id = R.drawable.btn_right_gray_arrow),
//                    contentDescription = "다음",
//                    tint = Color.Gray,
//                    modifier = Modifier.size(20.dp)
//                )
//            }
//        },
//    )
//}
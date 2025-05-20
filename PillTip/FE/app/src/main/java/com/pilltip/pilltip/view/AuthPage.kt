package com.pilltip.pilltip.view


import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.pilltip.pilltip.R
import com.pilltip.pilltip.composable.BackButton
import com.pilltip.pilltip.composable.DoubleLineTitleText
import com.pilltip.pilltip.composable.Guideline
import com.pilltip.pilltip.composable.HeightSpacer
import com.pilltip.pilltip.composable.HighlightingLine
import com.pilltip.pilltip.composable.LabelText
import com.pilltip.pilltip.composable.NextButton
import com.pilltip.pilltip.composable.PillTipDatePicker
import com.pilltip.pilltip.composable.PlaceholderTextField
import com.pilltip.pilltip.composable.SelectButton
import com.pilltip.pilltip.composable.TitleDescription
import com.pilltip.pilltip.composable.WhiteScreenModifier
import com.pilltip.pilltip.composable.WidthSpacer
import com.pilltip.pilltip.composable.buttonModifier
import com.pilltip.pilltip.model.signUp.SignUpViewModel
import com.pilltip.pilltip.ui.theme.pretendard
import com.pilltip.pilltip.view.logic.InputType
import com.pilltip.pilltip.view.logic.TermBottomSheet
import com.pilltip.pilltip.view.logic.containsSequentialNumbers

/**
 * 아이디 입력 페이지입니다.
 * @param SignUpViewModel로, SignIn Data를 관리합니다.
 * @param navController Navigation Controller입니다.
 * @author 김기윤
 */
@Composable
fun IdPage(
    viewModel: SignUpViewModel = hiltViewModel(),
    navController: NavController
) {
    var ID by remember { mutableStateOf("") }
    var isChecked by remember { mutableStateOf(false) }

    var containsEngNum by remember { mutableStateOf(false) }
    var containsKorean by remember { mutableStateOf(false) }
    var containsSpeical by remember { mutableStateOf(false) }
    val engNumRegex = Regex("[a-zA-Z0-9]")
    val koreanRegex = Regex("[\uAC00-\uD7AF\u1100-\u11FF\u3130-\u318F]+")
    val specialCharRegex = Regex("[!@#$%^&*(),.?\":{}|<>]")
    containsEngNum = engNumRegex.containsMatchIn(ID)
    containsKorean = koreanRegex.containsMatchIn(ID)
    containsSpeical = specialCharRegex.containsMatchIn(ID)

    val isEnglishAndNumberValid =
        ID.matches(Regex(".*[a-zA-Z].*")) && ID.matches(Regex(".*[0-9].*")) && !containsKorean
    val isLengthValid = ID.length >= 8 && ID.length <= 20
    val isSpecialCharValid = !containsSpeical && ID.isNotEmpty()
    val isAllConditionsValid = isEnglishAndNumberValid && isLengthValid && isSpecialCharValid

    val focusRequester = FocusRequester()
    var isFocused by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    Column(
        modifier = WhiteScreenModifier
            .pointerInput(Unit) {
                detectTapGestures {
                    focusManager.clearFocus()
                }
            },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        BackButton(navigationTo = ({ navController.navigate("SplashPage") }))
        HeightSpacer(56.dp)
        DoubleLineTitleText("아이디를", "입력해주세요")
        HeightSpacer(42.dp)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 28.dp)
        ) {
            BasicTextField(
                value = ID,
                cursorBrush = SolidColor(if (isFocused) Color(0xFF397CDB) else Color(0xFFBFBFBF)),
                onValueChange = {
                    ID = it
                    viewModel.updateUserId(ID)
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(
                    onDone = { keyboardController?.hide() }
                ),
                modifier = Modifier
                    .height(22.dp)
                    .weight(1f)
                    .focusRequester(focusRequester)
                    .onFocusChanged { focusState ->
                        isFocused = focusState.isFocused
                    },
                textStyle = TextStyle(
                    fontSize = 17.sp,
                    color = Color.Black
                ),
                decorationBox = { innerTextField ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(22.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        if (ID.isEmpty()) {
                            Text(
                                text = "아이디",
                                style = TextStyle(
                                    fontSize = 17.sp,
                                    fontFamily = pretendard,
                                    fontWeight = FontWeight.Normal,
                                    color = Color(0xFFBFBFBF)
                                )
                            )
                        }
                        innerTextField()
                    }
                },
            )
            if (ID.isNotEmpty()) {
                Image(
                    imageVector = ImageVector.vectorResource(id = R.drawable.btn_textfield_eraseall),
                    contentDescription = "x_marker",
                    modifier = Modifier
                        .padding(start = 21.43.dp, end = 9.dp, top = 2.5.dp, bottom = 1.3.dp)
                        .clickable {
                            ID = ""
                        }
                )
            }
        }
        HeightSpacer(14.dp)
        HighlightingLine(
            text = ID,
            isFocused = isFocused,
            isAllConditionsValid = isAllConditionsValid
        )
        HeightSpacer(32.dp)
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 24.dp, end = 24.dp, bottom = 16.dp),
            text = "가이드",
            style = TextStyle(
                fontSize = 16.sp,
                fontFamily = pretendard,
                fontWeight = FontWeight.W700,
                color = Color(0xFFAFB8C1)
            )
        )
        Guideline(
            description = "영문, 숫자 조합을 사용해주세요",
            isValid = isEnglishAndNumberValid
        )
        HeightSpacer(12.dp)
        Guideline(
            description = "최소 8자리 이상, 20자 미만으로 구성해주세요",
            isValid = isLengthValid
        )
        HeightSpacer(12.dp)
        Guideline(
            description = "특수문자는 사용할 수 없어요",
            isValid = isSpecialCharValid
        )
        Spacer(modifier = Modifier.weight(1f))
        NextButton(
            mModifier = buttonModifier,
            text = if (isChecked) "다음" else "아이디 중복 확인",
            buttonColor = if (isAllConditionsValid) Color(0xFF348ADF) else Color(0xFFCADCF5),
            onClick = {
                if (isChecked && isAllConditionsValid) navController.navigate("PasswordPage")
                isChecked = true
            }
        )
    }
}

/**
 * 비밀번호 입력 페이지입니다.
 * @param SignUpViewModel로, SignIn Data를 관리합니다.
 * @param navController Navigation Controller입니다.
 * @author 김기윤
 */
@Composable
fun PasswordPage(
    viewModel: SignUpViewModel = hiltViewModel(),
    navController: NavController
) {
    var password by remember { mutableStateOf("") }
    var reenteredPassword by remember { mutableStateOf("") }

    val focusRequester = FocusRequester()
    var isFocused by remember { mutableStateOf(false) }
    var isFocusedReentered by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    val containsKorean by remember { mutableStateOf(false) }
    val isEnglishAndNumberValid =
        password.matches(Regex(".*[a-zA-Z].*")) && password.matches(Regex(".*[0-9].*")) && !containsKorean
    val isLengthValid = password.length >= 8
    val isSequentialNumbersValid = !containsSequentialNumbers(password)
    val keyboardController = LocalSoftwareKeyboardController.current

    val isAllConditionsValid = isEnglishAndNumberValid && isLengthValid && isSequentialNumbersValid

    var termsOfService by remember { mutableStateOf(false) }

    Column(
        modifier = WhiteScreenModifier
            .pointerInput(Unit) {
                detectTapGestures {
                    focusManager.clearFocus()
                }
            },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        BackButton(navigationTo = ({ navController.navigate("IdPage") }))
        HeightSpacer(56.dp)
        DoubleLineTitleText("비밀번호를", "입력해주세요")
        HeightSpacer(40.dp)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 28.dp)
        ) {
            BasicTextField(
                value = password,
                onValueChange = {
                    password = it
                    viewModel.updatePassword(password)
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(
                    onDone = { keyboardController?.hide() }
                ),
                modifier = Modifier
                    .height(22.dp)
                    .weight(1f)
                    .focusRequester(focusRequester)
                    .onFocusChanged { focusState ->
                        isFocused = focusState.isFocused
                    },
                textStyle = TextStyle(
                    fontSize = 17.sp,
                    color = Color.Black
                ),
                decorationBox = { innerTextField ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(21.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        if (password.isEmpty()) {
                            Text(
                                text = "비밀번호",
                                style = TextStyle(
                                    fontFamily = pretendard,
                                    fontSize = 18.sp,
                                    color = Color(0x99818181)
                                )
                            )
                        }
                        innerTextField()
                    }
                }

            )
            if (password.isNotEmpty()) {
                Image(
                    imageVector = ImageVector.vectorResource(id = R.drawable.btn_textfield_eraseall),
                    contentDescription = "x_marker",
                    modifier = Modifier
                        .padding(start = 21.43.dp, end = 9.dp, top = 2.5.dp, bottom = 2.5.dp)
                        .clickable {
                            password = ""
                            viewModel.updatePassword("")
                        }
                )
            }
        }
        HeightSpacer(14.dp)
        HighlightingLine(
            text = password,
            isFocused = isFocused,
            isAllConditionsValid = isAllConditionsValid
        )
        HeightSpacer(32.dp)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 28.dp)
        ) {
            BasicTextField(
                value = reenteredPassword,
                onValueChange = {
                    reenteredPassword = it
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(
                    onDone = { keyboardController?.hide() }
                ),
                modifier = Modifier
                    .height(22.dp)
                    .weight(1f)
                    .focusRequester(focusRequester)
                    .onFocusChanged { focusState ->
                        isFocusedReentered = focusState.isFocused
                    },
                textStyle = TextStyle(
                    fontSize = 17.sp,
                    color = Color.Black
                ),
                decorationBox = { innerTextField ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(21.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        if (reenteredPassword.isEmpty()) {
                            Text(
                                text = "비밀번호 재확인",
                                style = TextStyle(
                                    fontFamily = pretendard,
                                    fontSize = 18.sp,
                                    color = Color(0x99818181)
                                )
                            )
                        }
                        innerTextField()
                    }
                }
            )
            if (reenteredPassword.isNotEmpty()) {
                Image(
                    imageVector = ImageVector.vectorResource(id = R.drawable.btn_textfield_eraseall),
                    contentDescription = "x_marker",
                    modifier = Modifier
                        .padding(start = 21.43.dp, end = 9.dp, top = 2.5.dp, bottom = 2.5.dp)
                        .clickable {
                            reenteredPassword = ""
                        }
                )
            }
        }
        HeightSpacer(14.dp)
        HighlightingLine(
            text = reenteredPassword,
            isFocused = isFocusedReentered,
            isAllConditionsValid = password == reenteredPassword
        )
        HeightSpacer(32.dp)
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 24.dp, end = 24.dp, bottom = 16.dp),
            text = "가이드",
            style = TextStyle(
                fontSize = 16.sp,
                fontFamily = pretendard,
                fontWeight = FontWeight.W700,
                color = Color(0xFFAFB8C1),
            )
        )
        Guideline(
            description = "영문, 숫자 조합을 사용해주세요",
            isValid = isEnglishAndNumberValid
        )
        HeightSpacer(12.dp)
        Guideline(
            description = "최소 8자리 이상으로 구성해주세요",
            isValid = isLengthValid
        )
        HeightSpacer(12.dp)
        Guideline(
            description = "연속된 숫자는 사용할 수 없어요",
            isValid = isSequentialNumbersValid
        )
        Spacer(modifier = Modifier.weight(1f))
        NextButton(
            mModifier = buttonModifier,
            text = "다음",
            buttonColor =
            if (isAllConditionsValid && password == reenteredPassword) Color(0xFF348ADF)
            else Color(0xFFCADCF5),
            onClick = {
                if (isAllConditionsValid) {
                    if (!termsOfService)
                        termsOfService = true
                }
            }
        )
    }
    if (termsOfService) {
        TermBottomSheet(
            viewModel,
            navController,
            onDismiss = { termsOfService = false }
        )
    }
}

/**
 * 전화번호 입력 페이지입니다.
 * @param SignUpViewModel로, SignIn Data를 관리합니다.
 * @param navController Navigation Controller입니다.
 * @author 김기윤
 */
@Composable
fun PhoneAuthPage(
    viewModel: SignUpViewModel = hiltViewModel(),
    navController: NavController,
) {
    var phoneNumber by remember { mutableStateOf("") }
    var isFocused by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    Column(
        modifier = WhiteScreenModifier
            .pointerInput(Unit) {
                detectTapGestures {
                    focusManager.clearFocus()
                }
            },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        BackButton(navigationTo = ({ navController.navigate("PasswordPage")}))
        HeightSpacer(56.dp)
        DoubleLineTitleText(upperTextLine = "전화번호를", lowerTextLine = "입력해주세요")
        HeightSpacer(12.dp)
        TitleDescription(description = "연락 가능한 번호를 입력해주세요")
        HeightSpacer(29.dp)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Column(modifier = Modifier.weight(1f)) {
                LabelText(labelText = if (phoneNumber.isNotEmpty()) "전화번호" else "")
                PlaceholderTextField(
                    placeHolder = "010-0000-0000",
                    inputText = phoneNumber,
                    inputType = InputType.NUMBER,
                    onTextChanged = { phoneNumber = it },
                    onFocusChanged = {
                        isFocused = it
                    }
                )
            }
        }
        HeightSpacer(14.dp)
        HighlightingLine(text = phoneNumber, isFocused = isFocused)
        Spacer(modifier = Modifier.weight(1f))
        NextButton(
            mModifier = buttonModifier,
            buttonColor = if (phoneNumber.length >= 11) Color(0xFF397CDB) else Color(0xFFCADCF5),
            onClick = {
                if (phoneNumber.length >= 11) {
                    viewModel.updatePhone(phoneNumber)
                    navController.navigate("GenderPage")
                }
            }
        )
    }
}

@Composable
fun GenderPage(
    navController: NavController,
    viewModel: SignUpViewModel = hiltViewModel()
){
    val lc = LocalConfiguration.current.screenHeightDp
    val localWitdh = LocalConfiguration.current.screenWidthDp
    Column(
        modifier = WhiteScreenModifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        BackButton(navigationTo = ({ navController.navigate("PhoneAuthPage")}))
        HeightSpacer(56.dp)
        Column(
            modifier = Modifier.height((lc - 50 + 46).dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            HeightSpacer(60.dp)
            Box(
                Modifier
                    .width(84.dp)
                    .height(33.dp)
                    .background(color = Color(0xFFEEF4FC), shape = RoundedCornerShape(size = 12.dp))
                    .padding(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 8.dp)
            ) {
                Text(
                    text = "성별 선택",
                    style = TextStyle(
                        fontSize = 14.sp,
                        fontFamily = pretendard,
                        fontWeight = FontWeight(700),
                        color = Color(0xFF397CDB),
                        textAlign = TextAlign.Center,
                    )
                )
            }
            HeightSpacer(22.dp)
            Text(
                text = "맞춤 서비스 제공을 위해 필요해요",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
                    .wrapContentHeight(Alignment.CenterVertically),
                style = TextStyle(
                    fontSize = 28.sp,
                    fontFamily = pretendard,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF121212),
                    letterSpacing = (-0.3).sp,
                    textAlign = TextAlign.Center,
                    platformStyle = PlatformTextStyle(
                        includeFontPadding = false
                    )
                )
            )
            Row(
                modifier = Modifier
                    .padding(horizontal = 24.dp, vertical = 40.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.Start),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                SelectButton(
                    text = "남성",
                    widthValue = (localWitdh - 48 - 16)/2,
                    imageSource = R.drawable.btn_blue_checkmark,
                    onClick = {
                        viewModel.updateGender("M")
                        navController.navigate("AgePage")
                    }
                )
                SelectButton(
                    text = "여성",
                    widthValue = (localWitdh - 48 - 16)/2,
                    imageSource = R.drawable.btn_gray_checkmark,
                    onClick = {
                        viewModel.updateGender("F")
                        navController.navigate("AgePage")
                    }
                )
            }
        }
    }
}

@Composable
fun AgePage(
    navController: NavController,
    viewModel: SignUpViewModel = hiltViewModel()
){
    Column(
        modifier = WhiteScreenModifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ){
        BackButton(navigationTo = ({ navController.navigate("GenderPage")}))
        HeightSpacer(56.dp)
        DoubleLineTitleText(upperTextLine = "나이를", lowerTextLine = "입력해주세요")
        HeightSpacer(40.dp)
        PillTipDatePicker(
            onDateSelected = { localDate ->
                viewModel.updateBirthDate(
                    localDate.year,
                    localDate.monthValue,
                    localDate.dayOfMonth
                )
            }
        )
        Spacer(modifier = Modifier.weight(1f))
        NextButton(
            mModifier = buttonModifier,
            buttonColor =  Color(0xFF397CDB),
            onClick = {
                navController.navigate("BodyStatPage")
            }
        )
    }
}

@Composable
fun BodyStatPage(
    navController: NavController,
    viewModel: SignUpViewModel = hiltViewModel()
){
    var height by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }
    var isFocusedHeight by remember { mutableStateOf(false) }
    var isFocusedWeight by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    Column(
        modifier = WhiteScreenModifier
            .pointerInput(Unit) {
                detectTapGestures {
                    focusManager.clearFocus()
                }
            },
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        BackButton(navigationTo = ({ navController.navigate("AgePage")}))
        HeightSpacer(56.dp)
        DoubleLineTitleText(upperTextLine = "키와 몸무게를", lowerTextLine = "입력해주세요")
        HeightSpacer(12.dp)
        TitleDescription(description = "복약 안전을 위해 필수적이에요")
        HeightSpacer(29.dp)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Column(modifier = Modifier.weight(1f)) {
                LabelText(labelText = if (height.isNotEmpty()) "키" else "")
                PlaceholderTextField(
                    placeHolder = "174",
                    inputText = height,
                    inputType = InputType.NUMBER,
                    onTextChanged = { height = it },
                    onFocusChanged = {
                        isFocusedHeight = it
                    }
                )
                HeightSpacer(14.dp)
                HighlightingLine(text = height, isFocused = isFocusedHeight)
            }
            WidthSpacer(16.dp)
            Column(modifier = Modifier.weight(1f)) {
                LabelText(labelText = if (weight.isNotEmpty()) "몸무게" else "")
                PlaceholderTextField(
                    placeHolder = "50",
                    inputText = weight,
                    inputType = InputType.NUMBER,
                    onTextChanged = { weight = it },
                    onFocusChanged = {
                        isFocusedWeight = it
                    }
                )
                HeightSpacer(14.dp)
                HighlightingLine(text = weight, isFocused = isFocusedWeight)
            }
        }

        Spacer(modifier = Modifier.weight(1f))
        NextButton(
            mModifier = buttonModifier,
            buttonColor = if (height.length >= 2 && weight.length >= 2) Color(0xFF397CDB) else Color(0xFFCADCF5),
            onClick = {
                if (height.length >= 2 && weight.length >= 2) {
                    viewModel.updateHeight(height.toInt())
                    viewModel.updateWeight(weight.toInt())

                    navController.navigate("InterestPage")
                }
            }
        )
    }
}

@Composable
fun InterestPage(
    navController: NavController,
    viewModel: SignUpViewModel = hiltViewModel()
){
    var interest by remember { mutableStateOf("") }
    var isFocused by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    val context = LocalContext.current
    Column(
        modifier = WhiteScreenModifier
            .pointerInput(Unit) {
                detectTapGestures {
                    focusManager.clearFocus()
                }
            },
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        BackButton(navigationTo = ({ navController.navigate("BodyStatPage")}))
        HeightSpacer(56.dp)
        DoubleLineTitleText(upperTextLine = "관심사를", lowerTextLine = "입력해주세요")
        HeightSpacer(12.dp)
        TitleDescription(description = "무엇에 관심이 있으신가요?")
        HeightSpacer(29.dp)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Column(modifier = Modifier.weight(1f)) {
                LabelText(labelText = if (interest.isNotEmpty()) "관심사" else "")
                PlaceholderTextField(
                    placeHolder = "174",
                    inputText = interest,
                    inputType = InputType.NUMBER,
                    onTextChanged = { interest = it },
                    onFocusChanged = {
                        isFocused = it
                    }
                )
            }
        }
        HeightSpacer(14.dp)
        HighlightingLine(text = interest, isFocused = isFocused)
        Spacer(modifier = Modifier.weight(1f))
        NextButton(
            mModifier = buttonModifier,
            buttonColor = if (interest.isNotEmpty()) Color(0xFF397CDB) else Color(0xFFCADCF5),
            onClick = {
                if (interest.isNotEmpty()) {
                    viewModel.updateInterest(interest)

                    viewModel.completeSignUp(
                        onSuccess = {
                            val sharedPreferences = context.getSharedPreferences("user", Context.MODE_PRIVATE)
                            with(sharedPreferences.edit()) {
                                putString("userId", viewModel.getUserId())
                                putString("token", viewModel.getToken())
                                putString("nickname", viewModel.getNickname())
                                apply()
                            }

                            navController.navigate("SplashPage") {
                                popUpTo("InterestPage") { inclusive = true }
                            }
                        },
                        onFailure = { error ->
                            Toast.makeText(
                                context,
                                error?.message ?: "회원가입에 실패했습니다.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    )
                }
            }
        )
    }
}




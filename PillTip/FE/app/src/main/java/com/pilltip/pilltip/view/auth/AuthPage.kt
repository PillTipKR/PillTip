package com.pilltip.pilltip.view.auth


import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
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
import androidx.compose.ui.res.painterResource
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
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.pilltip.pilltip.R
import com.pilltip.pilltip.composable.AppBar
import com.pilltip.pilltip.composable.AuthComposable.AgeField
import com.pilltip.pilltip.composable.AuthComposable.BodyProfile
import com.pilltip.pilltip.composable.AuthComposable.LoginButton
import com.pilltip.pilltip.composable.AuthComposable.NicknameField
import com.pilltip.pilltip.composable.AuthComposable.ProfileGenderPick
import com.pilltip.pilltip.composable.AuthComposable.ProfileStepDescription
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
import com.pilltip.pilltip.composable.SingleLineTitleText
import com.pilltip.pilltip.composable.TagButton
import com.pilltip.pilltip.composable.TitleDescription
import com.pilltip.pilltip.composable.WhiteScreenModifier
import com.pilltip.pilltip.composable.WidthSpacer
import com.pilltip.pilltip.composable.buttonModifier
import com.pilltip.pilltip.model.signUp.KaKaoLoginViewModel
import com.pilltip.pilltip.model.signUp.LoginType
import com.pilltip.pilltip.model.signUp.PhoneAuthViewModel
import com.pilltip.pilltip.model.signUp.SignUpViewModel
import com.pilltip.pilltip.model.signUp.TokenManager
import com.pilltip.pilltip.ui.theme.gray500
import com.pilltip.pilltip.ui.theme.gray600
import com.pilltip.pilltip.ui.theme.gray700
import com.pilltip.pilltip.ui.theme.pretendard
import com.pilltip.pilltip.ui.theme.primaryColor
import com.pilltip.pilltip.view.auth.logic.InputType
import com.pilltip.pilltip.view.auth.logic.OtpInputField
import com.pilltip.pilltip.view.auth.logic.TermBottomSheet
import com.pilltip.pilltip.view.auth.logic.containsSequentialNumbers
import kotlinx.coroutines.delay

@Composable
fun SplashPage(navController: NavController) {
    var visible by remember { mutableStateOf(false) }

    val systemUiController = rememberSystemUiController()
    SideEffect {
        systemUiController.isNavigationBarVisible = false
    }

    LaunchedEffect(Unit) {
        visible = true
        delay(3000)
        navController.navigate("SelectPage") {
            popUpTo("SplashPage") { inclusive = true }
        }
    }

    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = 1000) // 1초간 페이드 인
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = primaryColor),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            imageVector = ImageVector.vectorResource(R.drawable.logo_splash),
            contentDescription = "PillTip_Logo",
            modifier = Modifier.alpha(alpha)
        )
    }
}

@Composable
fun SelectPage(
    navController: NavController,
    signUpViewModel: SignUpViewModel,
    kakaoViewModel: KaKaoLoginViewModel = hiltViewModel(),
) {
    val systemUiController = rememberSystemUiController()
    val user by kakaoViewModel.user
    val context = LocalContext.current
    val token = kakaoViewModel.getAccessToken()
    var termsOfService by remember { mutableStateOf(false) }

    LaunchedEffect(user) {
        if (user != null && token != null) {
            signUpViewModel.updateLoginType(LoginType.SOCIAL)
            signUpViewModel.updateToken(token)
            Log.d("accessToken: ", signUpViewModel.getToken())
            termsOfService = true
        }
    }

    SideEffect {
        systemUiController.isNavigationBarVisible = true
    }
    Column(
        modifier = WhiteScreenModifier.padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        HeightSpacer(214.dp)
        Image(
            imageVector = ImageVector.vectorResource(id = R.drawable.logo_pilltip_blue_pill),
            contentDescription = "필팁 알약 로고"
        )
        HeightSpacer(13.dp)
        Image(
            imageVector = ImageVector.vectorResource(id = R.drawable.logo_pilltip_typo),
            contentDescription = "필팁 로고 typography"
        )
        HeightSpacer(12.dp)
        Text(
            text = "당신만의 AI 의약 관리",
            fontSize = 16.sp,
            fontFamily = pretendard,
            fontWeight = FontWeight(500),
            color = gray600
        )
        HeightSpacer(117.dp)
        Image(
            painter = painterResource(id = R.drawable.ic_select_page_fast_signin),
            contentDescription = "빠른 회원가입"
        )
        Spacer(modifier = Modifier.height(10.dp))
        LoginButton(
            text = "카카오로 시작하기",
            sourceImage = R.drawable.ic_kakao_login,
            borderColor = Color(0xFFFEE500),
            backgroundColor = Color(0xFFFEE500),
            fontColor = Color.Black,
            onClick = {
                kakaoViewModel.kakaoLogin(context)
            }
        )
//        HeightSpacer(16.dp)
//        LoginButton(
//            text = "구글로 시작하기 - 개발 중",
//            sourceImage = R.drawable.ic_google_login,
//            borderColor = gray500,
//            backgroundColor = Color.White,
//            fontColor = Color.Black,
//            onClick = {
//
//            }
//        )
        HeightSpacer(16.dp)
        LoginButton(
            text = "아이디로 시작하기",
            sourceImage = R.drawable.ic_id_login,
            borderColor = Color(0xFF408AF1),
            backgroundColor = Color.White,
            fontColor = primaryColor,
            onClick = { navController.navigate("IDPage") }
        )
    }

    if (termsOfService) {
        TermBottomSheet(
            signUpViewModel,
            navController,
            onDismiss = { termsOfService = false }
        )
    }
}

/**
 * 아이디 입력 페이지입니다.
 * @param SignUpViewModel로, SignIn Data를 관리합니다.
 * @param navController Navigation Controller입니다.
 * @author 김기윤
 */
@Composable
fun IdPage(
    navController: NavController,
    viewModel: SignUpViewModel
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
        BackButton(navigationTo = ({ navController.navigate("SelectPage") }))
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
                viewModel.updateloginId(ID)
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
    navController: NavController,
    viewModel: SignUpViewModel,
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
                if (isAllConditionsValid && password == reenteredPassword) {
                    viewModel.updatePassword(password)
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
@SuppressLint("DefaultLocale")
@Composable
fun PhoneAuthPage(
    navController: NavController,
    viewModel: SignUpViewModel,
    phoneViewModel: PhoneAuthViewModel = hiltViewModel(),
) {
    var phoneNumber by remember { mutableStateOf("") }
    val verificationId by phoneViewModel.verificationId.collectAsState()
    val code by phoneViewModel.code.collectAsState()
    val timeRemaining by phoneViewModel.timeRemaining.collectAsState()
    val timerText = remember(timeRemaining) {
        val min = timeRemaining / 60
        val sec = timeRemaining % 60
        String.format("%02d:%02d", min, sec)
    }

    var isFocused by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    val context = LocalContext.current
    val activity = context as? Activity
    val localWitdh = LocalConfiguration.current.screenWidthDp

    val isAutoVerified by phoneViewModel.isAutoVerified.collectAsState()


    LaunchedEffect(isAutoVerified) {
        if (isAutoVerified) {
            viewModel.updatePhone(phoneNumber)
            navController.navigate("ProfilePage")
        }
    }

    Column(
        modifier = WhiteScreenModifier
            .pointerInput(Unit) {
                detectTapGestures {
                    focusManager.clearFocus()
                }
            },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (verificationId != null) {
            HeightSpacer(130.dp)
            SingleLineTitleText("문자로 받은")
            SingleLineTitleText("인증번호를 입력해주세요")
            HeightSpacer(24.dp)
            Text(
                text = if (timeRemaining > 0) "남은 시간 $timerText" else "만료",
                fontSize = 16.sp,
                fontFamily = pretendard,
                fontWeight = FontWeight.Medium,
                color = gray500
            )
            HeightSpacer(40.dp)
            OtpInputField(
                otpText = code,
                onOtpTextChange = { phoneViewModel.updateCode(it) },
                modifier = Modifier.width((localWitdh - 48).dp)
            )
        } else {
            HeightSpacer(130.dp)
            SingleLineTitleText("휴대폰 번호를 알려주세요")
            HeightSpacer(56.dp)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    LabelText(labelText = if (phoneNumber.isNotEmpty()) "휴대폰 번호" else "")
                    PlaceholderTextField(
                        placeHolder = "휴대폰 번호",
                        inputText = phoneNumber,
                        inputType = InputType.NUMBER,
                        onTextChanged = {
                            phoneNumber = it
                            phoneViewModel.updatePhoneNumber(it)
                        },
                        onFocusChanged = {
                            isFocused = it
                        }
                    )
                }
            }
            HeightSpacer(14.dp)
            HighlightingLine(text = phoneNumber, isFocused = isFocused)
            HeightSpacer(14.dp)
            Text(
                text = "입력된 정보는 회원가입에만 사용돼요.",
                style = TextStyle(
                    fontSize = 12.sp,
                    fontFamily = pretendard,
                    fontWeight = FontWeight(500),
                    color = gray700
                )
            )
        }
        Spacer(modifier = Modifier.weight(1f))
        NextButton(
            mModifier = buttonModifier,
            buttonColor = if (
                (verificationId == null && phoneNumber.length >= 11) ||
                (verificationId != null && code.length == 6)
            ) Color(0xFF397CDB) else Color(0xFFCADCF5),
            text = if (
                (verificationId != null && code.length == 6)
            ) "인증하기" else "다음",
            onClick = {
                if (
                    (verificationId == null && phoneNumber.length >= 11) ||
                    (verificationId != null && code.length == 6)
                ) {
                    activity?.let {
                        if (verificationId == null) {
                            phoneViewModel.requestVerification(
                                activity = it,
                                onSent = {},
                                onFailed = {}
                            )
                        } else {
                            phoneViewModel.verifyCodeInput(
                                onSuccess = {
                                    viewModel.updatePhone(phoneNumber)
                                    navController.navigate("ProfilePage")
                                },
                                onFailure = {}
                            )
                        }
                    }
                }
            }
        )
    }
}

@Composable
fun ProfilePage(
    navController: NavController,
    viewModel: SignUpViewModel
) {
    var nickname by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("") }
    var year by remember { mutableStateOf(0) }
    var month by remember { mutableStateOf(0) }
    var day by remember { mutableStateOf(0) }
    var height by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }

    val isFormValid = nickname.isNotBlank()
            && gender.isNotBlank()
            && year > 0 && month > 0 && day > 0
            && height.length >= 2
            && weight.length >= 2

    Column(
        modifier = WhiteScreenModifier.padding(horizontal = 24.dp)
    ) {
        HeightSpacer(50.dp)
        AppBar(
            horizontalPadding = 0.dp,
            LNB = R.drawable.btn_left_gray_arrow,
            LNBDesc = "뒤로가기 버튼",
            LNBClickable = { navController.navigate("SelectPage") },
            TitleText = "프로필 등록",
        )
        HeightSpacer(36.dp)
        ProfileStepDescription("닉네임")
        HeightSpacer(12.dp)
        NicknameField(
            nickname,
            nicknameChange = { nickname = it }
        )
        HeightSpacer(28.dp)
        ProfileStepDescription("성별")
        HeightSpacer(12.dp)
        ProfileGenderPick(select = { gender = it })
        HeightSpacer(28.dp)
        ProfileStepDescription("연령")
        HeightSpacer(12.dp)
        AgeField { selectedYear, selectedMonth, selectedDay ->
            year = selectedYear
            month = selectedMonth
            day = selectedDay
        }
        HeightSpacer(28.dp)
        Row {
            ProfileStepDescription("연령")
            WidthSpacer(4.dp)
            Image(
                imageVector = ImageVector.vectorResource(R.drawable.btn_profile_question),
                contentDescription = "물음표 아이콘을 누를 시 간단한 알림 문구를 표출합니다"
            )
        }
        HeightSpacer(12.dp)
        BodyProfile { selectedHeight, selectedWeight ->
            height = selectedHeight
            weight = selectedWeight
        }
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = "등록된 정보는 나중에 수정할 수 있어요!",
            style = TextStyle(
                fontSize = 14.sp,
                fontFamily = pretendard,
                fontWeight = FontWeight(500),
                color = primaryColor,
                textAlign = TextAlign.Center
            ),
            modifier = Modifier.fillMaxWidth()
        )
        NextButton(
            mModifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .padding(bottom = 46.dp)
                .height(58.dp),
            buttonColor = if (isFormValid) Color(0xFF397CDB) else Color(0xFFCADCF5),
            onClick = {
                if (height.length >= 2 && weight.length >= 2) {
                    viewModel.updateBirthDate(
                        year,
                        month,
                        day
                    )
                    viewModel.updateNickname(nickname)
                    viewModel.updateGender(gender)
                    viewModel.updateHeight(height.toInt())
                    viewModel.updateWeight(weight.toInt())
                    navController.navigate("InterestPage")
                }
            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun InterestPage(
    navController: NavController,
    viewModel: SignUpViewModel
) {
    val selectedKeywords = remember { mutableStateListOf<String>() }
    val allKeywords = List(20) { index -> "키워드~${index + 1}" }
    val context = LocalContext.current


    Column(
        modifier = WhiteScreenModifier,
    ) {
        HeightSpacer(50.dp)
        AppBar(
            horizontalPadding = 20.dp,
            LNB = R.drawable.btn_left_gray_arrow,
            LNBDesc = "뒤로가기 버튼",
            LNBClickable = { navController.navigate("ProfilePage") },
            TitleText = "관심사 선택",
        )
        HeightSpacer(56.dp)
        DoubleLineTitleText(upperTextLine = "관심있는 키워드를", lowerTextLine = "선택해보세요", padding = 20.dp)
        HeightSpacer(12.dp)
        Text(
            text = "최대 5개까지 선택할 수 있어요",
            fontSize = 12.sp,
            fontFamily = pretendard,
            fontWeight = FontWeight(500),
            color = gray500,
            modifier = Modifier.padding(horizontal = 20.dp)
        )
        HeightSpacer(40.dp)
        FlowRow(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            allKeywords.forEach { keyword ->
                val isSelected = keyword in selectedKeywords
                TagButton(
                    keyword = keyword,
                    isSelected = isSelected,
                    onClick = {
                        if (isSelected) {
                            selectedKeywords.remove(keyword)
                        } else if (selectedKeywords.size < 5) {
                            selectedKeywords.add(keyword)
                        }
                    }
                )
            }
        }
        Spacer(modifier = Modifier.weight(1f))
        NextButton(
            text = "저장하기",
            mModifier = buttonModifier,
            buttonColor = if (selectedKeywords.isNotEmpty()) Color(0xFF397CDB) else Color(0xFFCADCF5),
            onClick = {
                if (selectedKeywords.isNotEmpty()) {
                    viewModel.updateInterest(interest = selectedKeywords.joinToString(","))
                    viewModel.logSignUpData()
                    viewModel.completeSignUp(
                        onSuccess = { accessToken, refreshToken ->
                            TokenManager.saveTokens(context, accessToken, refreshToken)
                            viewModel.submitTerms(
                                token = accessToken,
                                onSuccess = {
                                    navController.navigate("PillMainPage")
                                },
                                onFailure = { error ->
                                    Toast.makeText(
                                        context,
                                        "약관 전송 실패: ${error?.message ?: "알 수 없는 오류"}",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    navController.navigate("PillMainPage")
                                }
                            )
                        },
                        onFailure = { error ->
                            Toast.makeText(
                                context,
                                error?.message ?: "회원가입에 실패했습니다.",
                                Toast.LENGTH_SHORT
                            ).show()
                            navController.navigate("PillMainPage")
                        }
                    )
                }
            }
        )
    }
}




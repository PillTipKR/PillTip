package com.pilltip.pilltip.view.questionnaire

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.pilltip.pilltip.R
import com.pilltip.pilltip.composable.AuthComposable.RoundTextField
import com.pilltip.pilltip.composable.BackButton
import com.pilltip.pilltip.composable.HeightSpacer
import com.pilltip.pilltip.composable.IosButton
import com.pilltip.pilltip.composable.NextButton
import com.pilltip.pilltip.composable.QuestionnaireComposable.DottedDivider
import com.pilltip.pilltip.composable.QuestionnaireComposable.EditableProfileField
import com.pilltip.pilltip.composable.QuestionnaireComposable.FixedProfiledField
import com.pilltip.pilltip.composable.QuestionnaireComposable.InformationBox
import com.pilltip.pilltip.composable.SearchComposable.AutoCompleteList
import com.pilltip.pilltip.composable.SearchComposable.PillSearchField
import com.pilltip.pilltip.composable.WhiteScreenModifier
import com.pilltip.pilltip.composable.WidthSpacer
import com.pilltip.pilltip.composable.buttonModifier
import com.pilltip.pilltip.composable.noRippleClickable
import com.pilltip.pilltip.model.UserInfoManager
import com.pilltip.pilltip.model.search.AllergyEntry
import com.pilltip.pilltip.model.search.ChronicDiseaseEntry
import com.pilltip.pilltip.model.search.LogViewModel
import com.pilltip.pilltip.model.search.MedicationEntry
import com.pilltip.pilltip.model.search.QuestionnaireViewModel
import com.pilltip.pilltip.model.search.SearchHiltViewModel
import com.pilltip.pilltip.model.search.SurgeryHistoryEntry
import com.pilltip.pilltip.ui.theme.gray200
import com.pilltip.pilltip.ui.theme.gray400
import com.pilltip.pilltip.ui.theme.gray500
import com.pilltip.pilltip.ui.theme.gray600
import com.pilltip.pilltip.ui.theme.gray700
import com.pilltip.pilltip.ui.theme.gray800
import com.pilltip.pilltip.ui.theme.pretendard
import com.pilltip.pilltip.ui.theme.primaryColor
import com.pilltip.pilltip.ui.theme.primaryColor050
import com.pilltip.pilltip.view.auth.logic.EssentialTerms
import com.pilltip.pilltip.view.auth.logic.OptionalTerms
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter

@Composable
fun QuestionnairePage(
    navController: NavController
) {
    val scrollState = rememberScrollState()

    Box(
        modifier = WhiteScreenModifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        Box(
            modifier = Modifier
                .background(Color.White)
                .zIndex(1f)
                .align(Alignment.TopStart)
        ) {
            BackButton(
                horizontalPadding = 22.dp,
                verticalPadding = 0.dp
            ) {
                navController.navigate("DetailPage") {
                    popUpTo("DetailPage") {
                        inclusive = false

                    }
                }
            }

            BackHandler {
                navController.navigate("DetailPage") {
                    popUpTo("DetailPage") {
                        inclusive = false
                    }
                }
            }
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 22.dp)
                .padding(top = 80.dp, bottom = 100.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .border(
                        width = 1.4.dp,
                        color = primaryColor,
                        shape = RoundedCornerShape(100.dp)
                    )
                    .background(Color.White, RoundedCornerShape(100.dp))
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Image(
                    imageVector = ImageVector.vectorResource(R.drawable.logo_pilltip_typo),
                    contentDescription = "필팁 문진표 로고",
                    modifier = Modifier
                        .width(30.dp)
                        .height(10.dp)
                )
            }

            HeightSpacer(18.dp)
            InformationBox(
                header = "스마트 문진표",
                headerSize = 30,
                headerColor = primaryColor,
                desc = "손으로 적는 기존의 문진은 그만\n이제 간편한 문진의 시작"
            )

            HeightSpacer(56.dp)
            InformationBox(
                header = "언제 어느 곳에서든",
                desc = "내 정보를 바탕으로\n자동으로 문진표를 작성해요"
            )

            HeightSpacer(56.dp)
            InformationBox(
                header = "간편한 정보 선택",
                desc = "문진표 제출 시, 제공할 정보를\n간편하게 선택할 수 있어요"
            )

            HeightSpacer(56.dp)
            InformationBox(
                header = "개인정보 걱정마세요",
                desc = "문진표는 일정 기간이 지나면\n자동 삭제되니 유출 걱정없어요"
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 20.dp,
                    clip = false
                )
                .align(Alignment.BottomCenter)
                .background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            NextButton(
                mModifier = buttonModifier,
                text = "작성하기",
                onClick = {
                    navController.navigate("EssentialPage")
                }
            )
        }
    }
}

@Composable
fun EssentialPage(
    navController: NavController,
    questionnaireViewModel: QuestionnaireViewModel
) {
    var isEssentialChecked by remember { mutableStateOf(false) }
    var isOptionalChecked by remember { mutableStateOf(false) }
    var isEssentialExpanded by remember { mutableStateOf(false) }
    var isOptionalExpanded by remember { mutableStateOf(false) }

    val essentialRotation by animateFloatAsState(
        targetValue = if (isEssentialExpanded) 90f else 0f,
        animationSpec = tween(durationMillis = 600),
        label = "essential_arrow_rotation"
    )

    val optionalRotation by animateFloatAsState(
        targetValue = if (isOptionalExpanded) 90f else 0f,
        animationSpec = tween(durationMillis = 600),
        label = "optional_arrow_rotation"
    )
    Column(
        modifier = WhiteScreenModifier
            .statusBarsPadding()
            .padding(horizontal = 22.dp)
    ) {
        BackButton(
            horizontalPadding = 0.dp,
            verticalPadding = 0.dp
        ) {
            navController.navigate("DetailPage") {
                popUpTo("DetailPage") {
                    inclusive = false

                }
            }
        }

        BackHandler {
            navController.navigate("DetailPage") {
                popUpTo("DetailPage") {
                    inclusive = false
                }
            }
        }
        HeightSpacer(62.dp)
        Image(
            imageVector = ImageVector.vectorResource(R.drawable.ic_details_blue_common_pills),
            contentDescription = "문진표 방패 이미지",
            modifier = Modifier
                .size(32.dp)
                .padding(1.dp)
        )
        HeightSpacer(12.dp)
        Text(
            text = "추가 동의가 필요해요.",
            fontSize = 26.sp,
            lineHeight = 33.8.sp,
            fontFamily = pretendard,
            fontWeight = FontWeight(700),
            color = gray800
        )
        HeightSpacer(12.dp)
        Text(
            text = "모든 정보는 암호화되어 안전하게 보관돼요",
            fontSize = 14.sp,
            lineHeight = 19.6.sp,
            fontFamily = pretendard,
            fontWeight = FontWeight(600),
            color = gray400,
        )
        HeightSpacer(34.dp)
        Row(
            modifier = Modifier.padding(vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                imageVector =
                    if (!isEssentialChecked)
                        ImageVector.vectorResource(R.drawable.btn_gray_checkmark)
                    else
                        ImageVector.vectorResource(R.drawable.btn_blue_checkmark),
                contentDescription = "checkBtn",
                modifier = Modifier
                    .size(20.dp, 20.dp)
                    .noRippleClickable { isEssentialChecked = !isEssentialChecked }
            )
            WidthSpacer(8.dp)
            Text(
                text = "[필수] 서비스 이용약관",
                style = TextStyle(
                    fontSize = 14.sp,
                    fontFamily = pretendard,
                    fontWeight = FontWeight(500),
                    color = Color(0xFF686D78),
                ),
                modifier = Modifier.noRippleClickable {
                    isEssentialChecked = !isEssentialChecked
                }
            )
            Spacer(modifier = Modifier.weight(1f))
            Image(
                imageVector = ImageVector.vectorResource(R.drawable.btn_announce_arrow),
                contentDescription = "description",
                modifier = Modifier
                    .noRippleClickable {
                        isEssentialExpanded = !isEssentialExpanded
                    }
                    .graphicsLayer(rotationZ = essentialRotation)
            )
        }
        AnimatedVisibility(visible = isEssentialExpanded) {
            EssentialTerms()
        }
        Row(
            modifier = Modifier.padding(vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                imageVector =
                    if (!isOptionalChecked)
                        ImageVector.vectorResource(R.drawable.btn_gray_checkmark)
                    else
                        ImageVector.vectorResource(R.drawable.btn_blue_checkmark),
                contentDescription = "checkBtn",
                modifier = Modifier
                    .size(20.dp, 20.dp)
                    .noRippleClickable { isOptionalChecked = !isOptionalChecked }
            )
            WidthSpacer(8.dp)
            Text(
                text = "[필수] 의료법에 관한 정보 수집 및 이용 동의서",
                style = TextStyle(
                    fontSize = 14.sp,
                    fontFamily = pretendard,
                    fontWeight = FontWeight(500),
                    color = Color(0xFF686D78),
                ),
                modifier = Modifier.noRippleClickable { isOptionalChecked = !isOptionalChecked }
            )
            Spacer(modifier = Modifier.weight(1f))
            Image(
                imageVector = ImageVector.vectorResource(R.drawable.btn_announce_arrow),
                contentDescription = "grayCheck",
                modifier = Modifier
                    .noRippleClickable {
                        isOptionalExpanded = !isOptionalExpanded
                    }
                    .graphicsLayer(rotationZ = optionalRotation)
            )
        }
        AnimatedVisibility(visible = isOptionalExpanded) {
            OptionalTerms()
        }
        Spacer(modifier = Modifier.weight(1f))
        NextButton(
            mModifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .padding(bottom = 46.dp)
                .height(58.dp),
            buttonColor = if (isEssentialChecked && isOptionalChecked) primaryColor else Color(
                0xFFCADCF5
            ),
            text = "동의하기",
            onClick = {
                if (isEssentialChecked && isOptionalChecked) {
                    questionnaireViewModel.updateSensitivePermissions(
                        sensitiveInfo = true,
                        medicalInfo = true
                    )
                    navController.navigate("AreYouPage/약")
                }
            }
        )
    }
}

@Composable
fun AreYouPage(
    navController: NavController,
    query: String,
    title: String,
    onYesClicked: () -> Unit,
    onNoClicked: () -> Unit
) {
    Column(
        modifier = WhiteScreenModifier
            .statusBarsPadding()
            .padding(horizontal = 22.dp)
    ) {
        BackButton(horizontalPadding = 0.dp, verticalPadding = 0.dp) {
            val target = when (query) {
                "알러지" -> "AreYouPage/약"
                "기저질환" -> "AreYouPage/알러지"
                "수술" -> "AreYouPage/기저질환"
                else -> "QuestionnairePage"
            }

            navController.navigate(target) {
                popUpTo("AreYouPage/$query") { inclusive = true }
            }
        }
        BackHandler {
            val target = when (query) {
                "알러지" -> "AreYouPage/약"
                "기저질환" -> "AreYouPage/알러지"
                "수술" -> "AreYouPage/기저질환"
                else -> "QuestionnairePage"
            }

            navController.navigate(target) {
                popUpTo("AreYouPage/$query") { inclusive = true }
            }
        }
        HeightSpacer(62.dp)
        Text(
            text = "Q.",
            fontSize = 26.sp,
            lineHeight = 33.8.sp,
            fontFamily = pretendard,
            fontWeight = FontWeight(600),
            color = primaryColor
        )
        HeightSpacer(4.dp)
        Text(
            text = title,
            fontSize = 26.sp,
            lineHeight = 33.8.sp,
            fontFamily = pretendard,
            fontWeight = FontWeight(700),
            color = Color(0xFF121212)
        )
        HeightSpacer(100.dp)
        Row {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .height(185.dp)
                    .background(
                        color = primaryColor050,
                        shape = RoundedCornerShape(size = 24.dp)
                    )
                    .padding(start = 17.dp, top = 22.dp, end = 17.dp, bottom = 30.dp)
                    .noRippleClickable { onYesClicked() },
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    imageVector = ImageVector.vectorResource(R.drawable.btn_blue_circle),
                    contentDescription = "맞아요"
                )
                HeightSpacer(14.dp)
                Text(
                    text = "맞아요",
                    fontSize = 16.sp,
                    fontFamily = pretendard,
                    fontWeight = FontWeight(700),
                    color = primaryColor,
                    textAlign = TextAlign.Center
                )
            }
            WidthSpacer(16.dp)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .height(185.dp)
                    .background(
                        color = Color(0xFFFFF3F3),
                        shape = RoundedCornerShape(size = 24.dp)
                    )
                    .padding(start = 17.dp, top = 22.dp, end = 17.dp, bottom = 30.dp)
                    .noRippleClickable { onNoClicked() },
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    imageVector = ImageVector.vectorResource(R.drawable.btn_red_xmark),
                    contentDescription = "아니에요"
                )
                HeightSpacer(14.dp)
                Text(
                    text = "아니에요",
                    fontSize = 16.sp,
                    fontFamily = pretendard,
                    fontWeight = FontWeight(700),
                    color = Color(0xFFD51713),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}


data class SelectedDrug(
    val id: Long,
    val name: String
)

@OptIn(ExperimentalLayoutApi::class, FlowPreview::class, ExperimentalMaterial3Api::class)
@Composable
fun QuestionnaireSearchPage(
    navController: NavController,
    logViewModel: LogViewModel,
    searchViewModel: SearchHiltViewModel,
    questionnaireViewModel: QuestionnaireViewModel
) {
    var inputText by remember { mutableStateOf("") }
    val autoCompleted by searchViewModel.autoCompleted.collectAsState()
    val isLoading by searchViewModel.isAutoCompleteLoading.collectAsState()

    LaunchedEffect(Unit) {
        questionnaireViewModel.resetAll()
    }
    val selectedDrugs = remember { mutableStateListOf<SelectedDrug>() }

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

    val systemUiController = rememberSystemUiController()
    SideEffect {
        systemUiController.isNavigationBarVisible = true
    }
    val showBottomSheet = selectedDrugs.isNotEmpty()
    val keyboardController = LocalSoftwareKeyboardController.current

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFFDFDFD))
                .padding(horizontal = 22.dp, vertical = 18.dp)
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            PillSearchField(
                initialQuery = "",
                navController = navController,
                nowTyping = { inputText = it },
                searching = { inputText = it },
                onNavigateToResult = { query ->
                    logViewModel.addSearchQuery(inputText)
                    searchViewModel.fetchDrugSearch(query)
                    Log.d("Query: ", query)
                }
            )
            HeightSpacer(28.dp)
            if (inputText.isEmpty()) {
                Text(
                    text = "약품을 검색해보세요!",
                    style = TextStyle(
                        fontSize = 14.sp,
                        fontFamily = pretendard,
                        fontWeight = FontWeight(600),
                        color = gray700,
                    )
                )
            } else {
                Box(modifier = Modifier.fillMaxSize()) {
                    AutoCompleteList(
                        query = inputText,
                        searched = autoCompleted,
                        onClick = { },
                        onLoadMore = { searchViewModel.fetchAutoComplete(inputText) },
                        onAddClick = { selected ->
                            if (selectedDrugs.none { it.id == selected.id }) {
                                keyboardController?.hide()
                                selectedDrugs.add(
                                    SelectedDrug(
                                        id = selected.id,
                                        name = selected.value
                                    )
                                )
                            }
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

        AnimatedVisibility(
            visible = showBottomSheet,
            enter = slideInVertically(initialOffsetY = { it }),
            exit = slideOutVertically(targetOffsetY = { it }),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Surface(
                tonalElevation = 6.dp,
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                color = Color.White,
                shadowElevation = 10.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 22.dp, vertical = 16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .width(36.dp)
                            .height(4.dp)
                            .background(Color(0xFFD9D9D9), RoundedCornerShape(2.dp))
                    )
                    HeightSpacer(8.dp)
                    Text(
                        text = "선택 의약품",
                        fontSize = 14.sp,
                        fontFamily = pretendard,
                        fontWeight = FontWeight(600),
                        color = gray800
                    )
                    HeightSpacer(14.dp)
                    Box(
                        modifier = Modifier
                            .heightIn(max = 150.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(2.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            selectedDrugs.forEach { drug ->
                                Row(
                                    modifier = Modifier
                                        .border(
                                            width = 1.dp,
                                            color = gray200,
                                            shape = RoundedCornerShape(size = 100.dp)
                                        )
                                        .height(30.dp)
                                        .background(
                                            color = Color.White,
                                            shape = RoundedCornerShape(size = 100.dp)
                                        )
                                        .padding(
                                            start = 12.dp,
                                            top = 8.dp,
                                            end = 12.dp,
                                            bottom = 8.dp
                                        ),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = drug.name,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        style = TextStyle(
                                            fontSize = 12.sp,
                                            fontFamily = pretendard,
                                            fontWeight = FontWeight(500),
                                            color = gray700,
                                            textAlign = TextAlign.Center,
                                        ),
                                        modifier = Modifier.widthIn(max = 130.dp)
                                    )
                                    WidthSpacer(4.dp)
                                    Image(
                                        imageVector = ImageVector.vectorResource(R.drawable.btn_tag_erase),
                                        contentDescription = "삭제",
                                        modifier = Modifier.noRippleClickable {
                                            selectedDrugs.remove(
                                                drug
                                            )
                                        }
                                    )
                                }
                            }
                        }
                    }
                    NextButton(
                        mModifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp)
                            .height(58.dp),
                        text = "등록하기",
                        buttonColor = if (selectedDrugs.isEmpty()) Color(0xFF348ADF) else primaryColor
                    ) {
                        val medicationList = selectedDrugs.map {
                            MedicationEntry(
                                medicationId = it.id,
                                medicationName = it.name,
                                submitted = true
                            )
                        }
                        questionnaireViewModel.medicationInfo = medicationList
                        navController.navigate("AreYouPage/알러지")
                    }

                    HeightSpacer(8.dp)
                    Text(
                        text = "나중에 하기",
                        fontSize = 12.sp,
                        fontFamily = pretendard,
                        fontWeight = FontWeight(600),
                        color = gray400,
                        textAlign = TextAlign.Center,
                        textDecoration = TextDecoration.Underline,
                        modifier = Modifier
                            .fillMaxWidth()
                            .noRippleClickable {
                                selectedDrugs.clear()
                            }
                    )
                    Spacer(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(
                                WindowInsets.navigationBars.asPaddingValues()
                                    .calculateBottomPadding()
                            )
                            .background(Color.White)
                    )
                }
            }
        }
    }
}

@Composable
fun WritePage(
    navController: NavController,
    title: String,
    description: String,
    placeholder: String,
    mode: String,
    viewModel: QuestionnaireViewModel
) {
    var textList by remember { mutableStateOf(mutableListOf("")) }
    val allFilled = textList.all { it.trim().isNotEmpty() }

    Box(
        modifier = WhiteScreenModifier
            .fillMaxSize()
            .padding(horizontal = 22.dp)
            .statusBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 80.dp)
        ) {
            BackButton(horizontalPadding = 0.dp, verticalPadding = 0.dp) {
                val targetRoute = when (mode) {
                    "allergy" -> "AreYouPage/약"
                    "etc" -> "AreYouPage/알러지"
                    else -> "AreYouPage/기저질환"
                }

                navController.navigate(targetRoute) {
                    popUpTo("WritePage/$mode") { inclusive = true }
                }
            }
            BackHandler {
                val targetRoute = when (mode) {
                    "allergy" -> "AreYouPage/약"
                    "etc" -> "AreYouPage/알러지"
                    else -> "AreYouPage/기저질환"
                }

                navController.navigate(targetRoute) {
                    popUpTo("WritePage/$mode") { inclusive = true }
                }
            }

            HeightSpacer(62.dp)

            Text(
                text = "Q.",
                fontSize = 26.sp,
                lineHeight = 33.8.sp,
                fontFamily = pretendard,
                fontWeight = FontWeight(600),
                color = primaryColor
            )

            HeightSpacer(4.dp)

            Text(
                text = title,
                fontSize = 26.sp,
                lineHeight = 33.8.sp,
                fontFamily = pretendard,
                fontWeight = FontWeight(700),
                color = Color(0xFF121212)
            )

            HeightSpacer(12.dp)

            Text(
                text = description,
                style = TextStyle(
                    fontSize = 14.sp,
                    lineHeight = 19.6.sp,
                    fontFamily = pretendard,
                    fontWeight = FontWeight(600),
                    color = gray400,
                )
            )

            HeightSpacer(30.dp)

            if (textList.size < 10) {
                Text(
                    text = "+ 추가하기",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = primaryColor,
                    modifier = Modifier
                        .clickable {
                            textList = textList.toMutableList().apply { add("") }
                        }
                        .padding(vertical = 8.dp)
                )
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                items(textList.size) { index ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RoundTextField(
                            text = textList[index],
                            textChange = { newValue ->
                                textList =
                                    textList.toMutableList().also { it[index] = newValue }
                            },
                            placeholder = placeholder,
                            isLogin = false,
                            modifier = Modifier.weight(1f)
                        )

                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "X",
                            fontSize = 18.sp,
                            color = Color.Gray,
                            modifier = Modifier
                                .clickable {
                                    textList =
                                        textList.toMutableList().apply { removeAt(index) }
                                }
                                .padding(8.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                item {
                    Spacer(modifier = Modifier.height(100.dp))
                }
            }
        }

        NextButton(
            mModifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .padding(bottom = 46.dp)
                .height(58.dp),
            text = "다음",
            buttonColor = if (allFilled) primaryColor else Color(0xFFCADCF5),
            onClick = {
                if (allFilled) {
                    val processedList = textList.map { it.trim() }

                    when (mode) {
                        "allergy" -> {
                            viewModel.allergyInfo =
                                processedList.map { AllergyEntry(it, submitted = true) }
                            navController.navigate("AreYouPage/기저질환")
                        }

                        "etc" -> {
                            viewModel.chronicDiseaseInfo =
                                processedList.map { ChronicDiseaseEntry(it, submitted = true) }
                            navController.navigate("AreYouPage/수술")
                        }

                        else -> {
                            viewModel.surgeryHistoryInfo =
                                processedList.map { SurgeryHistoryEntry(it, submitted = true) }
                            navController.navigate("QuestionnaireCheckPage")
                        }
                    }
                }
            }
        )
    }
}

@Composable
fun QuestionnaireCheckPage(
    navController: NavController,
    viewModel: QuestionnaireViewModel
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    var questionnaireName by remember { mutableStateOf("내 문진표") }
    var realName by remember { mutableStateOf("김필팁") }
    var isEditingRealName by remember { mutableStateOf(false) }
    var gender by remember { mutableStateOf(UserInfoManager.getUserData(context)?.gender) }
    var phone by remember { mutableStateOf(UserInfoManager.getUserData(context)?.phone!!) }
    var isEditingPhone by remember { mutableStateOf(false) }

    var address by remember { mutableStateOf("부산광역시 금정구 부산대학교로\n어쩌구 저쩌구") }
    var isEditingAddress by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }
    var option by remember { mutableStateOf("수정") }
    var isEditMode by remember { mutableStateOf(false) }

    val medicationList = remember { viewModel.medicationInfo.toMutableStateList() }
    val allergyList = remember { viewModel.allergyInfo.toMutableStateList() }
    val diseaseList = remember { viewModel.chronicDiseaseInfo.toMutableStateList() }
    val surgeryList = remember { viewModel.surgeryHistoryInfo.toMutableStateList() }



    Column(
        modifier = WhiteScreenModifier
            .padding(horizontal = 22.dp)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
    ) {
        Box() {
            BackButton(
                title = "문진표 확인",
                horizontalPadding = 0.dp,
                verticalPadding = 0.dp,
                iconDrawable = R.drawable.btn_vertical_dots,
                onClick = { expanded = true }
            ) {
                navController.navigate("DetailPage")
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier
                    .background(Color.White)
                    .align(Alignment.TopEnd)
            ) {
                DropdownMenuItem(text = { Text("수정") }, onClick = {
                    option = "수정"
                    isEditMode = !isEditMode
                    expanded = false
                })

            }
        }
        BasicTextField(
            value = questionnaireName,
            onValueChange = { questionnaireName = it },
            singleLine = true,
            textStyle = TextStyle(
                fontSize = 20.sp,
                fontFamily = pretendard,
                fontWeight = FontWeight(600),
                color = gray800,
                textAlign = TextAlign.Center,
            ),
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    keyboardController?.hide()
                    focusManager.clearFocus()
                }
            ),
            modifier = Modifier
                .fillMaxWidth()
        )
        HeightSpacer(24.dp)
        EditableProfileField(
            label = "이름",
            value = realName,
            onValueChange = { realName = it },
            isEditable = isEditingRealName,
            onEditToggle = { isEditingRealName = !isEditingRealName }
        )

        FixedProfiledField(
            "생년월일",
            "${UserInfoManager.getUserData(context)?.birthDate} (만 ${
                UserInfoManager.getUserData(context)?.age
            }세)"
        )
        FixedProfiledField("성별", "${gender}성")


        EditableProfileField(
            label = "전화번호",
            value = phone,
            onValueChange = { phone = it },
            isEditable = isEditingPhone,
            onEditToggle = { isEditingPhone = !isEditingPhone }
        )

        EditableProfileField(
            label = "주소",
            value = address,
            onValueChange = { address = it },
            isEditable = isEditingAddress,
            onEditToggle = { isEditingAddress = !isEditingAddress }
        )

        HeightSpacer(20.dp)

        Section(
            "복약 정보", medicationList, isEditMode,
            onToggle = {
                medicationList[it] =
                    medicationList[it].copy(submitted = !medicationList[it].submitted)
                viewModel.medicationInfo = medicationList
            },
            onDelete = {
                medicationList.removeAt(it)
                viewModel.medicationInfo = medicationList
            }
        )

        DottedDivider()

        Section(
            "알러지 정보", allergyList, isEditMode,
            onToggle = {
                allergyList[it] = allergyList[it].copy(submitted = !allergyList[it].submitted)
                viewModel.allergyInfo = allergyList
            },
            onDelete = {
                allergyList.removeAt(it)
                viewModel.allergyInfo = allergyList
            }
        )

        DottedDivider()

        Section(
            "기저질환", diseaseList, isEditMode,
            onToggle = {
                diseaseList[it] = diseaseList[it].copy(submitted = !diseaseList[it].submitted)
                viewModel.chronicDiseaseInfo = diseaseList
            },
            onDelete = {
                diseaseList.removeAt(it)
                viewModel.chronicDiseaseInfo = diseaseList
            }
        )

        DottedDivider()

        Section(
            "수술 이력", surgeryList, isEditMode,
            onToggle = {
                surgeryList[it] = surgeryList[it].copy(submitted = !surgeryList[it].submitted)
                viewModel.surgeryHistoryInfo = surgeryList
            },
            onDelete = {
                surgeryList.removeAt(it)
                viewModel.surgeryHistoryInfo = surgeryList
            }
        )

        NextButton(
            mModifier = buttonModifier,
            text = if (isEditMode) "수정완료" else "제출하기",
            onClick = {
                if (isEditMode) {
                    isEditMode = false
                } else {
                    viewModel.realName = realName
                    viewModel.address = address
                    viewModel.phoneNumber = phone
                    viewModel.questionnaireName = questionnaireName

                    viewModel.submit()
                    navController.navigate("DetailPage")
                }
            }
        )
    }
}

@Composable
fun <T> Section(
    title: String,
    items: List<T>,
    isEditMode: Boolean,
    onDelete: (Int) -> Unit,
    onToggle: (Int) -> Unit
) where T : Any {
    HeightSpacer(24.dp)
    Text(text = title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
    HeightSpacer(16.dp)
    items.forEachIndexed { index, item ->
        val label = when (item) {
            is MedicationEntry -> item.medicationName
            is AllergyEntry -> item.allergyName
            is ChronicDiseaseEntry -> item.chronicDiseaseName
            is SurgeryHistoryEntry -> item.surgeryHistoryName
            else -> ""
        }
        val submitted = when (item) {
            is MedicationEntry -> item.submitted
            is AllergyEntry -> item.submitted
            is ChronicDiseaseEntry -> item.submitted
            is SurgeryHistoryEntry -> item.submitted
            else -> false
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
        ) {
            Text(
                text = label,
                style = TextStyle(
                    fontSize = 14.sp,
                    fontFamily = pretendard,
                    fontWeight = FontWeight(500),
                    color = gray800,
                    textAlign = TextAlign.Justify,
                )
            )
            Spacer(modifier = Modifier.weight(1f))
            if (isEditMode) {
                Text(
                    text = "X",
                    color = Color.Red,
                    modifier = Modifier
                        .padding(horizontal = 8.dp)
                        .clickable { onDelete(index) }
                )
            } else {
                IosButton(checked = submitted, onCheckedChange = { onToggle(index) })
            }
        }
    }
    HeightSpacer(24.dp)
}


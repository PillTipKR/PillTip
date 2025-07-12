package com.pilltip.pilltip.view.main

import android.util.Log
import android.widget.Toast
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.google.firebase.messaging.FirebaseMessaging
import com.pilltip.pilltip.R
import com.pilltip.pilltip.composable.HeightSpacer
import com.pilltip.pilltip.composable.MainComposable.AnnouncementCard
import com.pilltip.pilltip.composable.MainComposable.BottomBar
import com.pilltip.pilltip.composable.MainComposable.BottomTab
import com.pilltip.pilltip.composable.MainComposable.DosageCard
import com.pilltip.pilltip.composable.MainComposable.DosagePage
import com.pilltip.pilltip.composable.MainComposable.FeatureButton
import com.pilltip.pilltip.composable.MainComposable.LogoField
import com.pilltip.pilltip.composable.MainComposable.MainSearchField
import com.pilltip.pilltip.composable.MainComposable.ProfileTagButton
import com.pilltip.pilltip.composable.MainComposable.SmallTabCard
import com.pilltip.pilltip.composable.MainComposable.formatDate
import com.pilltip.pilltip.composable.QuestionnaireComposable.QuestionnaireCard
import com.pilltip.pilltip.composable.WidthSpacer
import com.pilltip.pilltip.composable.noRippleClickable
import com.pilltip.pilltip.model.HandleBackPressToExitApp
import com.pilltip.pilltip.model.UserInfoManager
import com.pilltip.pilltip.model.search.QuestionnaireViewModel
import com.pilltip.pilltip.model.search.SearchHiltViewModel
import com.pilltip.pilltip.ui.theme.backgroundColor
import com.pilltip.pilltip.ui.theme.gray050
import com.pilltip.pilltip.ui.theme.gray200
import com.pilltip.pilltip.ui.theme.gray600
import com.pilltip.pilltip.ui.theme.gray800
import com.pilltip.pilltip.ui.theme.pretendard
import com.pilltip.pilltip.ui.theme.primaryColor
import com.pilltip.pilltip.ui.theme.primaryColor050
import com.pilltip.pilltip.ui.theme.primaryColor100
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDate

@Composable
fun PillMainPage(
    navController: NavController,
    searchHiltViewModel: SearchHiltViewModel,
    questionnaireViewModel: QuestionnaireViewModel,
    initialTab: BottomTab = BottomTab.Home
) {
    var selectedTab by remember { mutableStateOf(BottomTab.Home) }
    val systemUiController = rememberSystemUiController()
    val scrollState = rememberScrollState()

    val baseColor = Color(0xFFD0E6FD)
    val endColor = Color.White
    val steps = 12

    val gradientColors = remember {
        List(steps) { i ->
            lerp(baseColor, endColor, i / (steps - 1).toFloat())
        }
    }
    HandleBackPressToExitApp(navController)
    FirebaseMessaging.getInstance().token
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val token = task.result
                Log.d("FCM", "토큰: $token")
                searchHiltViewModel.sendFcmToken(token)
            } else {
                Log.w("FCM", "토큰 받기 실패", task.exception)
            }
        }

    SideEffect {
        systemUiController.setStatusBarColor(
            color = Color.Transparent,
            darkIcons = true
        )
        systemUiController.isNavigationBarVisible = true
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
                .verticalScroll(rememberScrollState())
                .drawBehind {
                    val gradient = Brush.verticalGradient(
                        colors = gradientColors,
                        startY = 0f,
                        endY = size.height
                    )
                    drawRect(brush = gradient)
                }
        ) {
            when (selectedTab) {
                BottomTab.Home -> HomePage(navController, searchHiltViewModel)
                BottomTab.Interaction -> navController.navigate("DURPage")
                BottomTab.Chart -> MyQuestionnairePage(navController, questionnaireViewModel)
                BottomTab.Calendar -> CalenderPage(navController, searchHiltViewModel)
                BottomTab.MyPage -> MyPage(
                    navController,
                    searchHiltViewModel,
                    questionnaireViewModel
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomePage(
    navController: NavController,
    searchHiltViewModel: SearchHiltViewModel
) {
    val systemUiController = rememberSystemUiController()
    SideEffect {
        systemUiController.setStatusBarColor(
            color = Color(0xFFD0E6FD),
            darkIcons = true
        )
        systemUiController.isNavigationBarVisible = true
    }
    LogoField(
        onClick = { navController.navigate("NotificationPage") }
    )
    MainSearchField(
        onClick = { navController.navigate("SearchPage") }
    )
    HeightSpacer(14.dp)
    Row(
        modifier = Modifier.padding(horizontal = 22.dp)
    ) {
        Box(
            modifier = Modifier
                .shadow(
                    elevation = 5.dp,
                    spotColor = gray600,
                    ambientColor = Color(0x14000000),
                    clip = false,
                    shape = RoundedCornerShape(size = 12.dp)
                )
                .weight(1f)
                .height(243.11111.dp)
                .background(color = Color.White, shape = RoundedCornerShape(size = 12.dp))
                .padding(start = 16.dp, top = 20.dp, end = 20.dp, bottom = 16.dp),
            contentAlignment = Alignment.BottomEnd
        ) {
            Column(
                Modifier
                    .fillMaxSize()
            ) {
                Text(
                    text = "빠른 약 검색",
                    style = TextStyle(
                        fontSize = 18.sp,
                        fontFamily = pretendard,
                        fontWeight = FontWeight(600),
                        color = gray800
                    )
                )
                HeightSpacer(6.dp)
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "내 복약 찾아보기",
                        style = TextStyle(
                            fontSize = 12.sp,
                            fontFamily = pretendard,
                            fontWeight = FontWeight(600),
                            color = Color(0xFF408AF1),
                        )
                    )
                    WidthSpacer(1.dp)
                    Image(
                        imageVector = ImageVector.vectorResource(R.drawable.btn_main_blue_right_arrow),
                        contentDescription = "우측 arrow"
                    )
                }

            }
            Image(
                imageVector = ImageVector.vectorResource(id = R.drawable.img_doc),
                contentDescription = "logo",
                modifier = Modifier
                    .padding(1.dp)
                    .width(124.dp)
                    .height(134.dp)
                    .offset(x = 10.dp, y = 4.dp)
            )
        }
        WidthSpacer(12.dp)
        Column(
            modifier = Modifier
                .height(243.11111.dp)
                .weight(1f)
        ) {
            SmallTabCard(
                HeaderText = "잊지말고 드세요",
                SubHeaderText = "복약 관리",
                ImageField = R.drawable.img_pill
            )
            HeightSpacer(12.dp)
            SmallTabCard(
                HeaderText = "간편한 내 기록 관리",
                SubHeaderText = "스마트 문진표",
                ImageField = R.drawable.logo_pilltip_blue_pill
            )
        }
    }
    HeightSpacer(24.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 22.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        FeatureButton(
            imageResource = R.drawable.ic_main_friend,
            description = "친구 추가"
        )
        FeatureButton(
            imageResource = R.drawable.ic_main_cute_baby,
            description = "자녀 관리"
        )
        FeatureButton(
            imageResource = R.drawable.ic_main_qrcode,
            description = "문진표 QR"
        )
        FeatureButton(
            imageResource = R.drawable.ic_main_review,
            description = "최신 리뷰"
        )
    }
//    AnnouncementCard("당신만의 스마트한 복약 관리, 필팁")
    HeightSpacer(40.dp)
    Text(
        text = "내 복약 관리",
        style = TextStyle(
            fontSize = 12.sp,
            fontFamily = pretendard,
            fontWeight = FontWeight(600),
            color = primaryColor,
        ),
        modifier = Modifier.padding(start = 22.dp)
    )
    HeightSpacer(4.dp)
    Text(
        text = "오늘의 내 복약률",
        style = TextStyle(
            fontSize = 16.sp,
            lineHeight = 22.4.sp,
            fontFamily = pretendard,
            fontWeight = FontWeight(700),
            color = Color(0xFF010913),
        ),
        modifier = Modifier.padding(start = 22.dp)
    )

    HeightSpacer(20.dp)
    val selectedDate by remember { mutableStateOf(LocalDate.now()) }
    val logData by searchHiltViewModel.dailyDosageLog.collectAsState()
    val dateText = formatDate(selectedDate)
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(selectedDate) {
        searchHiltViewModel.fetchDailyDosageLog(selectedDate)
    }

    logData?.let { data ->
        val items = listOf(DosagePage.Overall(dateText, data.percent)) +
                data.perDrugLogs.map {
                    DosagePage.PerDrug(it.medicationName, it.percent)
                }

        val pagerState = rememberPagerState(
            initialPage = 0,
            pageCount = { items.size }
        )

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxWidth()
        ) { page ->
            when (val item = items[page]) {
                is DosagePage.Overall -> DosageCard(
                    title = item.dateText,
                    percent = item.percent,
                    onClick = { navController.navigate("NotificationPage") }
                )

                is DosagePage.PerDrug -> DosageCard(
                    title = item.medicationName,
                    percent = item.percent,
                    onClick = { navController.navigate("NotificationPage") }
                )
            }
        }
    }
}

@Composable
fun MyQuestionnairePage(
    navController: NavController,
    viewModel: QuestionnaireViewModel
) {

    val systemUiController = rememberSystemUiController()
    SideEffect {
        systemUiController.setStatusBarColor(
            color = gray050,
            darkIcons = true
        )
        systemUiController.isNavigationBarVisible = true
    }
    val context = LocalContext.current
    var scrollState = rememberScrollState()
    var firstSelected by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }
    var sortOption by remember { mutableStateOf("최신순") }
    val list by remember { derivedStateOf { viewModel.questionnaireList } }
    val loading by remember { derivedStateOf { viewModel.isListLoading } }
    val localHeight = LocalConfiguration.current.screenHeightDp
    val permission = UserInfoManager.getUserData(LocalContext.current)?.permissions

    LaunchedEffect(Unit) {
        viewModel.fetchQuestionnaireList()
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(localHeight.dp)
            .background(color = gray050)
            .statusBarsPadding()
    ) {
        Text(
            text = "내 문진표",
            style = TextStyle(
                fontSize = 16.sp,
                fontFamily = pretendard,
                fontWeight = FontWeight(500),
                color = gray800,
            ),
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier
                .horizontalScroll(scrollState)
                .padding(start = 22.dp)
        ) {
            Box {
                ProfileTagButton(
                    text = sortOption,
                    image = R.drawable.btn_blue_dropdown,
                    selected = false,
                    onClick = { expanded = true }
                )

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.background(Color.White)
                ) {
                    DropdownMenuItem(text = { Text("최신순") }, onClick = {
                        sortOption = "최신순"
                        expanded = false
                    })
                    DropdownMenuItem(text = { Text("오래된 순") }, onClick = {
                        sortOption = "오래된 순"
                        expanded = false
                    })
                    DropdownMenuItem(text = { Text("가나다순") }, onClick = {
                        sortOption = "가나다순"
                        expanded = false
                    })
                }
            }
        }
//        HeightSpacer(10.dp)
//        Row(
//            modifier = Modifier
//                .border(
//                    width = 0.5.dp,
//                    color = primaryColor100,
//                    shape = RoundedCornerShape(size = 12.dp)
//                )
//                .padding(0.25.dp)
//                .fillMaxWidth()
//                .height(50.dp)
//                .background(color = primaryColor050, shape = RoundedCornerShape(size = 12.dp))
//                .padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 16.dp)
//        ) {
//            Image(
//                imageVector = ImageVector.vectorResource(R.drawable.ic_announce_speakerphone),
//                contentDescription = "문진표 공지"
//            )
//            WidthSpacer(8.dp)
//            Text(
//                text = "스마트 문진표에 대해 알려드려요",
//                style = TextStyle(
//                    fontSize = 14.sp,
//                    fontFamily = pretendard,
//                    fontWeight = FontWeight(600),
//                    color = primaryColor,
//                )
//            )
//        }
        HeightSpacer(12.dp)
        if (loading) {
            CircularProgressIndicator()
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                contentPadding = PaddingValues(vertical = 12.dp)
            ) {
                if (list.isEmpty()) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .fillMaxHeight(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            HeightSpacer(200.dp)
                            Image(
                                painter = painterResource(R.drawable.ic_questionnaire_none),
                                contentDescription = "문진표 없음"
                            )
                            HeightSpacer(10.dp)
                            Text(
                                text = "문진표가 없어요\n문진표를 추가해보세요",
                                style = TextStyle(
                                    fontSize = 16.sp,
                                    lineHeight = 22.4.sp,
                                    fontFamily = pretendard,
                                    fontWeight = FontWeight(500),
                                    color = gray600,
                                    textAlign = TextAlign.Center,
                                )
                            )
                            HeightSpacer(20.dp)
                            Box(
                                modifier = Modifier
                                    .border(
                                        width = 1.dp,
                                        color = primaryColor,
                                        shape = RoundedCornerShape(size = 12.dp)
                                    )
                                    .height(45.dp)
                                    .background(
                                        color = primaryColor,
                                        shape = RoundedCornerShape(size = 12.dp)
                                    )
                                    .padding(
                                        start = 22.dp,
                                        top = 14.dp,
                                        end = 22.dp,
                                        bottom = 14.dp
                                    )
                                    .noRippleClickable {
                                        if (permission == true) navController.navigate("AreYouPage/약")
                                        else navController.navigate("QuestionnairePage")
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "문진표 추가하기",
                                    style = TextStyle(
                                        fontSize = 14.sp,
                                        fontFamily = pretendard,
                                        fontWeight = FontWeight(600),
                                        color = Color.White,
                                    )
                                )
                            }
                        }
                    }
                } else {
                    items(list) { item ->
                        QuestionnaireCard(
                            questionnaire = item,
                            onClick = { navController.navigate("questionnaire_check/${item.questionnaireId}") },
                            onEdit = {

                            },
                            onDelete = {
                                viewModel.delete(
                                    id = item.questionnaireId,
                                    onSuccess = {
                                        Toast.makeText(context, "삭제 완료", Toast.LENGTH_SHORT).show()
                                        viewModel.fetchQuestionnaireList()
                                    },
                                    onError = {
                                        Toast.makeText(context, "삭제 실패: $it", Toast.LENGTH_SHORT)
                                            .show()
                                    }
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}
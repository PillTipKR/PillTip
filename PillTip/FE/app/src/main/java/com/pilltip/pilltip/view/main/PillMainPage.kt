package com.pilltip.pilltip.view.main

import android.util.Log
import android.widget.Toast
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
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
import com.pilltip.pilltip.composable.MainComposable.LogoField
import com.pilltip.pilltip.composable.MainComposable.MainSearchField
import com.pilltip.pilltip.composable.MainComposable.ProfileTagButton
import com.pilltip.pilltip.composable.MainComposable.SmallTabCard
import com.pilltip.pilltip.composable.QuestionnaireComposable.QuestionnaireCard
import com.pilltip.pilltip.composable.WidthSpacer
import com.pilltip.pilltip.model.HandleBackPressToExitApp
import com.pilltip.pilltip.model.search.QuestionnaireViewModel
import com.pilltip.pilltip.model.search.SearchHiltViewModel
import com.pilltip.pilltip.ui.theme.backgroundColor
import com.pilltip.pilltip.ui.theme.gray050
import com.pilltip.pilltip.ui.theme.gray200
import com.pilltip.pilltip.ui.theme.gray800
import com.pilltip.pilltip.ui.theme.pretendard
import com.pilltip.pilltip.ui.theme.primaryColor
import com.pilltip.pilltip.ui.theme.primaryColor050
import com.pilltip.pilltip.ui.theme.primaryColor100

@Composable
fun PillMainPage(
    navController: NavController,
    searchHiltViewModel: SearchHiltViewModel,
    questionnaireViewModel: QuestionnaireViewModel,
    initialTab: BottomTab = BottomTab.Home
) {
    var selectedTab by remember { mutableStateOf(BottomTab.Home) }
    val systemUiController = rememberSystemUiController()
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
                BottomTab.Home -> HomePage(navController)
                BottomTab.Interaction -> navController.navigate("DURPage")
                BottomTab.Chart -> MyQuestionnairePage(navController, questionnaireViewModel)
                BottomTab.Calendar -> CalenderPage(navController, searchHiltViewModel)
                BottomTab.MyPage -> MyPage(navController, searchHiltViewModel, questionnaireViewModel)
            }
        }
    }
}

@Composable
fun HomePage(
    navController: NavController
) {
    LogoField()
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
                    elevation = 8.dp,
                    spotColor = Color(0x14000000),
                    ambientColor = Color(0x14000000)
                )
                .weight(1f)
                .height(243.11111.dp)
                .background(color = Color.White, shape = RoundedCornerShape(size = 12.dp))
                .padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 16.dp),
            contentAlignment = Alignment.BottomEnd
        ) {
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 16.dp)
            ) {
                Text(
                    text = "약 검색",
                    style = TextStyle(
                        fontSize = 20.sp,
                        fontFamily = pretendard,
                        fontWeight = FontWeight(600),
                        color = Color(0xFF323439)
                    )
                )
                HeightSpacer(8.dp)
                Text(
                    text = "모든 약 검색은\n여기서",
                    style = TextStyle(
                        fontSize = 12.sp,
                        lineHeight = 16.sp,
                        fontFamily = pretendard,
                        fontWeight = FontWeight(500),
                        color = Color(0xFF858C9A)
                    )
                )
            }
            Image(
                imageVector = ImageVector.vectorResource(id = R.drawable.img_recommend_person),
                contentDescription = "logo",
                modifier = Modifier
                    .padding(1.dp)
                    .width(124.dp)
                    .height(134.dp)
                    .offset(x = -1.dp, y = 4.dp)
            )
        }
        WidthSpacer(12.dp)
        Column(
            modifier = Modifier
                .height(243.11111.dp)
                .weight(1f)
        ) {
            SmallTabCard(
                HeaderText = "복약 관리",
                SubHeaderText = "약 관리를\n간편하게",
                ImageField = R.drawable.logo_pilltip_blue_pill
            )
            HeightSpacer(12.dp)
            SmallTabCard(
                HeaderText = "스마트 문진표",
                SubHeaderText = "내 정보\n 편하고 안전하게",
                ImageField = R.drawable.logo_pilltip_blue_pill
            )
        }
    }
    HeightSpacer(14.dp)
    AnnouncementCard("필팁은 여러분의 가장 안전한 복약도우미입니다. 더욱 노력하겠습니다.")
    HeightSpacer(34.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 22.dp)
    ) {
        Text(
            text = "통계/ 리뷰",
            fontSize = 16.sp,
            fontFamily = pretendard,
            fontWeight = FontWeight(700),
            color = gray800
        )
        Spacer(modifier = Modifier.weight(1f))
        Image(
            imageVector = ImageVector.vectorResource(R.drawable.btn_right_gray_arrow),
            contentDescription = "통계/리뷰 보러가기",
            modifier = Modifier
                .padding(1.dp)
                .width(20.dp)
                .height(20.dp)
        )
        R.drawable.logo_pilltip_blue_pill
    }
}

@Composable
fun MyQuestionnairePage(
    navController: NavController,
    viewModel: QuestionnaireViewModel
) {
    val context = LocalContext.current
    var scrollState = rememberScrollState()
    var firstSelected by remember { mutableStateOf(false) }
    var secondSelected by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }
    var sortOption by remember { mutableStateOf("최신순") }
    val list by remember { derivedStateOf { viewModel.questionnaireList } }
    val loading by remember { derivedStateOf { viewModel.isListLoading } }

    LaunchedEffect(Unit) {
        viewModel.fetchQuestionnaireList()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .background(color = gray050)
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
            ProfileTagButton(
                text = "처방약만",
                selected = firstSelected,
                onClick = { firstSelected = !firstSelected }
            )

            Image(
                imageVector = ImageVector.vectorResource(R.drawable.ic_login_vertical_divider),
                contentDescription = "디바이더",
                modifier = Modifier
                    .padding(horizontal = 6.dp)
                    .width(1.dp)
                    .background(gray200)
                    .height(20.dp)
            )
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
        HeightSpacer(10.dp)
        Row(
            modifier = Modifier
                .border(
                    width = 0.5.dp,
                    color = primaryColor100,
                    shape = RoundedCornerShape(size = 12.dp)
                )
                .padding(0.25.dp)
                .fillMaxWidth()
                .height(50.dp)
                .background(color = primaryColor050, shape = RoundedCornerShape(size = 12.dp))
                .padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 16.dp)
        ) {
            Image(
                imageVector = ImageVector.vectorResource(R.drawable.ic_announce_speakerphone),
                contentDescription = "문진표 공지"
            )
            WidthSpacer(8.dp)
            Text(
                text = "스마트 문진표에 대해 알려드려요",
                style = TextStyle(
                    fontSize = 14.sp,
                    fontFamily = pretendard,
                    fontWeight = FontWeight(600),
                    color = primaryColor,
                )
            )
        }
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
                                    Toast.makeText(context, "삭제 실패: $it", Toast.LENGTH_SHORT).show()
                                }
                            )
                        }
                    )
                }
            }
        }
    }
}
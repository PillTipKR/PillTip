package com.pilltip.pilltip.view.main

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.pilltip.pilltip.R
import com.pilltip.pilltip.composable.HeightSpacer
import com.pilltip.pilltip.composable.MainComposable.AnnouncementCard
import com.pilltip.pilltip.composable.MainComposable.BottomBar
import com.pilltip.pilltip.composable.MainComposable.BottomTab
import com.pilltip.pilltip.composable.MainComposable.LogoField
import com.pilltip.pilltip.composable.MainComposable.MainSearchField
import com.pilltip.pilltip.composable.MainComposable.SmallTabCard
import com.pilltip.pilltip.composable.WidthSpacer
import com.pilltip.pilltip.model.HandleBackPressToExitApp
import com.pilltip.pilltip.model.search.SearchHiltViewModel
import com.pilltip.pilltip.ui.theme.backgroundColor
import com.pilltip.pilltip.ui.theme.gray800
import com.pilltip.pilltip.ui.theme.pretendard

@Composable
fun PillMainPage(
    navController: NavController,
    searchHiltViewModel: SearchHiltViewModel
) {
    var selectedTab by remember { mutableStateOf(BottomTab.Home) }
    val systemUiController = rememberSystemUiController()
    HandleBackPressToExitApp(navController)

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
                BottomTab.Interaction -> InteractionPage()
                BottomTab.Chart -> ChartPage()
                BottomTab.Calendar -> CalendarPage()
                BottomTab.MyPage -> MyPage(navController, searchHiltViewModel)
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
    }

}

@Composable
fun InteractionPage() {
    Text("상충 비교 화면")
}

@Composable
fun ChartPage() {
    Text("차트 화면")
}

@Composable
fun CalendarPage() {
    Text("캘린더 화면")
}
package com.pilltip.pilltip.view.main

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.pilltip.pilltip.R
import com.pilltip.pilltip.composable.BackButton
import com.pilltip.pilltip.composable.HeightSpacer
import com.pilltip.pilltip.composable.IosButton
import com.pilltip.pilltip.composable.MainComposable.DrugSummaryCard
import com.pilltip.pilltip.composable.MainComposable.ProfileTagButton
import com.pilltip.pilltip.composable.WhiteScreenModifier
import com.pilltip.pilltip.composable.WidthSpacer
import com.pilltip.pilltip.composable.noRippleClickable
import com.pilltip.pilltip.model.UserInfoManager
import com.pilltip.pilltip.model.search.SearchHiltViewModel
import com.pilltip.pilltip.ui.theme.gray200
import com.pilltip.pilltip.ui.theme.pretendard
import com.pilltip.pilltip.ui.theme.primaryColor
import com.pilltip.pilltip.ui.theme.primaryColor050

@Composable
fun MyPage(
    navController: NavController,
    searchHiltViewModel: SearchHiltViewModel
) {
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val nickname = UserInfoManager.getUserData(LocalContext.current)?.nickname

    Column(
        modifier = WhiteScreenModifier.padding(horizontal = 22.dp)
    ) {
        Box(
            modifier = Modifier.padding(horizontal = ((screenWidth - 184.dp) / 2)),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .padding(top = 40.dp, bottom = 20.dp)
                    .border(
                        width = 2.dp,
                        color = Color(0xFFE2E4EC),
                        shape = RoundedCornerShape(size = 60.dp)
                    )
                    .padding(2.dp)
                    .width(140.dp)
                    .height(140.dp)
                    .background(
                        color = Color(0xFF81ACE8),
                        shape = RoundedCornerShape(size = 58.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    imageVector = ImageVector.vectorResource(R.drawable.logo_pilltip_typo),
                    contentDescription = "업로드 된 이미지가 없을 때 기본 이미지를 사용합니다."
                )
            }
        }
        Text(
            text = "$nickname 님",
            style = TextStyle(
                fontSize = 24.sp,
                fontFamily = pretendard,
                fontWeight = FontWeight(700),
                color = Color(0xFF121212),
                textAlign = TextAlign.Center,
            ),
            modifier = Modifier.fillMaxWidth()
        )
        HeightSpacer(12.dp)

        MyPageMenuItem(text = "내 복약정보 관리") {
            navController.navigate("MyDrugInfoPage")
        }
        MyPageMenuItem(text = "내 건강정보 관리") {
            // TODO: navController.navigate(...)
        }
        MyPageMenuItem(text = "내 리뷰 관리") {
            // TODO: navController.navigate(...)
        }

        // 푸시알람 동의 - 스위치
        MyPageToggleItem(
            text = "푸시알람 동의",
            isChecked = true,
            onCheckedChange = { /* TODO */ }
        )

        MyPageMenuItem(text = "앱 이용 약관") { /* TODO */ }
        MyPageMenuItem(text = "로그아웃") { /* TODO */ }
        MyPageMenuItem(text = "회원탈퇴") { /* TODO */ }
    }
}

@Composable
fun MyPageMenuItem(
    text: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .border(
                width = 0.5.dp,
                color = gray200,
                shape = RoundedCornerShape(size = 12.dp)
            )
            .padding(0.25.dp)
            .fillMaxWidth()
            .height(49.dp)
            .background(color = Color(0xFFFFFFFF), shape = RoundedCornerShape(size = 12.dp))
            .padding(start = 16.dp, end = 16.dp)
            .noRippleClickable { onClick() },
        contentAlignment = Alignment.CenterStart
    ) {
        Text(
            text = text,
            fontSize = 14.sp,
            fontFamily = pretendard,
            fontWeight = FontWeight(700),
            color = Color(0xFF000000)
        )
    }
    Spacer(modifier = Modifier.height(12.dp))
}

@Composable
fun MyPageToggleItem(
    text: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Box(
        modifier = Modifier
            .border(
                width = 0.5.dp,
                color = gray200,
                shape = RoundedCornerShape(size = 12.dp)
            )
            .padding(0.25.dp)
            .fillMaxWidth()
            .height(49.dp)
            .background(color = Color(0xFFFFFFFF), shape = RoundedCornerShape(size = 12.dp))
            .padding(start = 16.dp, end = 16.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = text,
                fontSize = 14.sp,
                fontFamily = pretendard,
                fontWeight = FontWeight(700),
                color = Color(0xFF000000)
            )
            IosButton(
                checked = isChecked,
                onCheckedChange = onCheckedChange
            )
        }
    }
    Spacer(modifier = Modifier.height(12.dp))
}

@Composable
fun MyDrugInfoPage(
    navController: NavController,
    viewModel: SearchHiltViewModel
) {
    var scrollState = rememberScrollState()
    var firstSelected by remember { mutableStateOf(false) }
    var secondSelected by remember { mutableStateOf(false) }
    val pillList by viewModel.pillSummaryList.collectAsState()
    var expanded by remember { mutableStateOf(false) }
    var sortOption by remember { mutableStateOf("최신순") }

    LaunchedEffect(Unit) {
        viewModel.fetchDosageSummary()
    }

    Column(
        modifier = WhiteScreenModifier.padding(horizontal = 22.dp)
    ) {
        BackButton(
            title = "내 복약정보 관리",
            horizontalPadding = 0.dp,
            verticalPadding = 0.dp
        ) { navController.popBackStack() }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.horizontalScroll(scrollState)
        ) {
            ProfileTagButton(
                text = "처방약만",
                selected = firstSelected,
                onClick = {firstSelected = !firstSelected}
            )
            ProfileTagButton(
                text = "복약 중인 약만",
                selected = secondSelected,
                onClick = {secondSelected = !secondSelected}
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
        if (pillList.isEmpty()) {
            Text(
                text = "등록된 복약 정보가 없어요.",
                style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Medium),
                modifier = Modifier.padding(vertical = 32.dp)
            )
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxHeight()
            ) {
                items(pillList) { pill ->
                    DrugSummaryCard(pill = pill)
                }
            }
        }
    }
}
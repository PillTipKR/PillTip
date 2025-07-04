package com.pilltip.pilltip.view.main

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.pilltip.pilltip.R
import com.pilltip.pilltip.composable.BackButton
import com.pilltip.pilltip.composable.HeightSpacer
import com.pilltip.pilltip.composable.IosButton
import com.pilltip.pilltip.composable.MainComposable.DrugSummaryCard
import com.pilltip.pilltip.composable.MainComposable.ProfileTagButton
import com.pilltip.pilltip.composable.NextButton
import com.pilltip.pilltip.composable.WhiteScreenModifier
import com.pilltip.pilltip.composable.WidthSpacer
import com.pilltip.pilltip.composable.noRippleClickable
import com.pilltip.pilltip.model.UserInfoManager
import com.pilltip.pilltip.model.search.SearchHiltViewModel
import com.pilltip.pilltip.model.signUp.TokenManager
import com.pilltip.pilltip.ui.theme.gray100
import com.pilltip.pilltip.ui.theme.gray200
import com.pilltip.pilltip.ui.theme.gray500
import com.pilltip.pilltip.ui.theme.gray600
import com.pilltip.pilltip.ui.theme.gray700
import com.pilltip.pilltip.ui.theme.gray800
import com.pilltip.pilltip.ui.theme.gray900
import com.pilltip.pilltip.ui.theme.pretendard
import com.pilltip.pilltip.ui.theme.primaryColor
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyPage(
    navController: NavController,
    searchHiltViewModel: SearchHiltViewModel
) {
    val context = LocalContext.current
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    var toggle by remember { mutableStateOf(true) }
    val nickname = UserInfoManager.getUserData(LocalContext.current)?.nickname
    val scope = rememberCoroutineScope()
    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var isSheetVisible by remember { mutableStateOf(false) }

    Column(
        modifier = WhiteScreenModifier.padding(horizontal = 22.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                imageVector = ImageVector.vectorResource(R.drawable.logo_pilltip_typo),
                contentDescription = "프로필 이미지",
                modifier = Modifier
                    .padding(0.46154.dp)
                    .width(60.dp)
                    .height(60.dp)
                    .background(color = gray200, shape = RoundedCornerShape(size = 46.15385.dp))
            )
            WidthSpacer(20.dp)
            Text(
                text = "$nickname 님",
                style = TextStyle(
                    fontSize = 20.sp,
                    fontFamily = pretendard,
                    fontWeight = FontWeight(700),
                    color = gray800
                )
            )
        }
        HeightSpacer(32.dp)
        Column(
            modifier = Modifier
                .shadow(
                    elevation = 8.dp,
                    spotColor = Color(0x1F000000),
                    ambientColor = Color(0x1F000000)
                )
                .padding(0.5.dp)
                .fillMaxWidth()
                .height(132.dp)
                .background(color = Color(0xFFFFFFFF), shape = RoundedCornerShape(size = 12.dp))
                .padding(start = 20.dp, top = 24.dp, end = 20.dp, bottom = 24.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    imageVector = ImageVector.vectorResource(R.drawable.ic_dosage_fire),
                    contentDescription = "복약완료율"
                )
                WidthSpacer(6.dp)
                Text(
                    text = "7월 1일 화요일",
                    style = TextStyle(
                        fontSize = 14.sp,
                        lineHeight = 21.sp,
                        fontFamily = pretendard,
                        fontWeight = FontWeight(700),
                        color = primaryColor,
                    )
                )
            }
            HeightSpacer(6.dp)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "복약 완료율",
                    style = TextStyle(
                        fontSize = 20.sp,
                        lineHeight = 30.sp,
                        fontFamily = pretendard,
                        fontWeight = FontWeight(700),
                        color = gray800,
                    )
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "28%",
                    style = TextStyle(
                        fontSize = 28.sp,
                        lineHeight = 42.sp,
                        fontFamily = pretendard,
                        fontWeight = FontWeight(700),
                        color = gray800,
                    )
                )
            }
            HeightSpacer(22.dp)
            LinearProgressIndicator(
                progress = { 24 / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(100.dp)),
                color = primaryColor,
                trackColor = gray200,
            )
        }
        HeightSpacer(32.dp)
        MyPageMenuItem(text = "내 복약정보 관리") {
            navController.navigate("MyDrugInfoPage")
        }
        MyPageMenuItem(text = "내 건강정보 관리") {
            // TODO: navController.navigate(...)
        }
        MyPageMenuItem(text = "내 리뷰 관리") {
            // TODO: navController.navigate(...)
        }
        HeightSpacer(24.dp)
        Text(
            text = "알림",
            style = TextStyle(
                fontSize = 16.sp,
                fontFamily = pretendard,
                fontWeight = FontWeight(600),
                color = gray600,
            )
        )
        HeightSpacer(20.dp)
        MyPageToggleItem(
            text = "푸시알람 동의",
            isChecked = toggle,
            onCheckedChange = { toggle = !toggle }
        )
        MyPageMenuItem(text = "앱 이용 약관") { navController.navigate("EssentialInfoPage") }
        MyPageMenuItem(text = "로그아웃") {
            UserInfoManager.clear(context)
            TokenManager.clear(context)
            navController.navigate("SelectPage") {
                popUpTo(0) { inclusive = true }
                launchSingleTop = true
            }
        }
        Text(
            text = "회원 탈퇴",
            style = TextStyle(
                fontSize = 14.sp,
                fontFamily = pretendard,
                fontWeight = FontWeight(500),
                color = gray500,
            ),
            modifier = Modifier.noRippleClickable {
                if (!isSheetVisible) {
                    isSheetVisible = true
                }
            }
        )
    }
    if (isSheetVisible) {
        LaunchedEffect(Unit) {
            bottomSheetState.show()
        }
        ModalBottomSheet(
            onDismissRequest = {
                scope.launch {
                    bottomSheetState.hide()
                }.invokeOnCompletion {
                    isSheetVisible = false
                }
            },
            sheetState = bottomSheetState,
            containerColor = Color.White,
            dragHandle = {
                Box(
                    Modifier
                        .padding(top = 8.dp, bottom = 11.dp)
                        .width(48.dp)
                        .height(5.dp)
                        .background(Color(0xFFE2E4EC), RoundedCornerShape(12.dp))
                )
            }
        ) {

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                HeightSpacer(12.dp)
                Image(
                    imageVector = ImageVector.vectorResource(R.drawable.ic_details_blue_common_pills),
                    contentDescription = "회원탈퇴",
                    modifier = Modifier
                        .padding(1.4.dp)
                        .width(28.dp)
                        .height(28.dp)
                )
                HeightSpacer(15.dp)
                Text(
                    text = "정말 탈퇴하시겠어요?",
                    style = TextStyle(
                        fontSize = 18.sp,
                        lineHeight = 25.2.sp,
                        fontFamily = pretendard,
                        fontWeight = FontWeight(700),
                        color = Color(0xFF1A1A1A)
                    )
                )
                HeightSpacer(8.dp)
                Text(
                    text = "보안을 위해 저장된 모든 정보가 파기되며,\n복구할 수 없습니다.",
                    style = TextStyle(
                        fontSize = 14.sp,
                        lineHeight = 19.6.sp,
                        fontFamily = pretendard,
                        fontWeight = FontWeight(400),
                        color = gray500
                    )
                )
                HeightSpacer(12.dp)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 22.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    NextButton(
                        mModifier = Modifier
                            .weight(1f)
                            .padding(vertical = 16.dp)
                            .height(58.dp),
                        text = "탈퇴하기",
                        buttonColor = gray100,
                        textColor = gray700,
                        onClick = {
                            scope.launch {
                                bottomSheetState.hide()
                            }.invokeOnCompletion {
                                isSheetVisible = false
                            }
                        }
                    )
                    NextButton(
                        mModifier = Modifier
                            .weight(1f)
                            .padding(vertical = 16.dp)
                            .height(58.dp),
                        text = "뒤로가기",
                        buttonColor = primaryColor,
                        onClick = {
                            scope.launch {
                                bottomSheetState.hide()
                            }.invokeOnCompletion {
                                isSheetVisible = false
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun MyPageMenuItem(
    text: String,
    onClick: () -> Unit
) {
    Text(
        text = text,
        style = TextStyle(
            fontSize = 16.sp,
            fontFamily = pretendard,
            fontWeight = FontWeight(400),
            color = gray900,
        ),
        modifier = Modifier
            .fillMaxWidth()
            .noRippleClickable {
                onClick()
            }
    )
    Spacer(modifier = Modifier.height(24.dp))
}

@Composable
fun MyPageToggleItem(
    text: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = text,
            style = TextStyle(
                fontSize = 16.sp,
                fontFamily = pretendard,
                fontWeight = FontWeight(400),
                color = gray900,
            )
        )
        IosButton(
            checked = isChecked,
            onCheckedChange = onCheckedChange
        )
    }
    Spacer(modifier = Modifier.height(24.dp))
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
    val pillDetail by viewModel.pillDetail.collectAsState()

    LaunchedEffect(pillDetail) {
        if (pillDetail != null) {
            navController.navigate("DosagePage/${pillDetail!!.medicationId}/${pillDetail!!.medicationName}")
        }
    }

    LaunchedEffect(Unit) {
        viewModel.fetchDosageSummary()
    }

    Column(
        modifier = WhiteScreenModifier
            .statusBarsPadding()
            .padding(horizontal = 22.dp)
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
                onClick = { firstSelected = !firstSelected }
            )
            ProfileTagButton(
                text = "복약 중인 약만",
                selected = secondSelected,
                onClick = { secondSelected = !secondSelected }
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
                    DrugSummaryCard(
                        pill = pill,
                        onDelete = { viewModel.deletePill(it.medicationId) },
                        onEdit = { viewModel.fetchTakingPillDetail(it.medicationId) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EssentialInfoPage(
    navController: NavController,
    searchHiltViewModel: SearchHiltViewModel
) {
    var toggle by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()
    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var isSheetVisible by remember { mutableStateOf(false) }
    var isEssential1Checked by remember { mutableStateOf(false) }
    var isEssential2Checked by remember { mutableStateOf(false) }
    Column(
        modifier = WhiteScreenModifier
            .padding(horizontal = 22.dp)
            .systemBarsPadding()
    ) {
        BackButton(
            title = "앱 이용 약관",
            horizontalPadding = 0.dp,
            verticalPadding = 0.dp
        ) { navController.popBackStack() }
        HeightSpacer(24.dp)
        MyPageMenuItem(text = "내 민감정보 삭제") {
            isSheetVisible = true
        }
        MyPageToggleItem(
            text = "푸시알람 동의",
            isChecked = toggle,
            onCheckedChange = { toggle = !toggle }
        )
    }
    if (isSheetVisible) {
        LaunchedEffect(Unit) {
            bottomSheetState.show()
        }
        ModalBottomSheet(
            onDismissRequest = {
                scope.launch {
                    bottomSheetState.hide()
                }.invokeOnCompletion {
                    isSheetVisible = false
                }
            },
            sheetState = bottomSheetState,
            containerColor = Color.White,
            dragHandle = {
                Box(
                    Modifier
                        .padding(top = 8.dp, bottom = 11.dp)
                        .width(48.dp)
                        .height(5.dp)
                        .background(Color(0xFFE2E4EC), RoundedCornerShape(12.dp))
                )
            }
        ) {

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
            ) {
//                Image(
//                    imageVector = ImageVector.vectorResource(R.drawable.ic_details_blue_common_pills),
//                    contentDescription = "회원탈퇴",
//                    modifier = Modifier
//                        .padding(1.4.dp)
//                        .width(28.dp)
//                        .height(28.dp)
//                )
                HeightSpacer(16.dp)
                Text(
                    text = "모든 민감정보를 삭제합니다",
                    style = TextStyle(
                        fontSize = 18.sp,
                        lineHeight = 20.sp,
                        fontFamily = pretendard,
                        fontWeight = FontWeight(700),
                        color = Color.Black,
                    ),
                    modifier = Modifier.padding(vertical = 10.dp)
                )
                HeightSpacer(8.dp)
                Row(
                    modifier = Modifier.padding(vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        imageVector =
                            if (!isEssential1Checked)
                                ImageVector.vectorResource(R.drawable.btn_gray_checkmark)
                            else
                                ImageVector.vectorResource(R.drawable.btn_blue_checkmark),
                        contentDescription = "checkBtn",
                        modifier = Modifier
                            .size(20.dp, 20.dp)
                            .noRippleClickable { isEssential1Checked = !isEssential1Checked }
                    )
                    WidthSpacer(8.dp)
                    Text(
                        text = "내 복약정보, 문진표 등 민감정보가 포함된\n모든 데이터가 파기되며, 복구가 불가능합니다",
                        style = TextStyle(
                            fontSize = 14.sp,
                            fontFamily = pretendard,
                            fontWeight = FontWeight(500),
                            color = Color(0xFF686D78),
                        ),
                        modifier = Modifier.noRippleClickable {
                            isEssential1Checked = !isEssential1Checked
                        }
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Image(
                        imageVector = ImageVector.vectorResource(R.drawable.btn_announce_arrow),
                        contentDescription = "description",
                        modifier = Modifier
                            .noRippleClickable {

                            }
                    )
                }
                HeightSpacer(4.dp)
                Row(
                    modifier = Modifier.padding(vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        imageVector =
                            if (!isEssential2Checked)
                                ImageVector.vectorResource(R.drawable.btn_gray_checkmark)
                            else
                                ImageVector.vectorResource(R.drawable.btn_blue_checkmark),
                        contentDescription = "checkBtn",
                        modifier = Modifier
                            .size(20.dp, 20.dp)
                            .noRippleClickable { isEssential2Checked = !isEssential2Checked }
                    )
                    WidthSpacer(8.dp)
                    Text(
                        text = "민감정보수집동의가 철회되며, 일부서비스를\n이용하시려면 다시 동의해야합니다.",
                        style = TextStyle(
                            fontSize = 14.sp,
                            fontFamily = pretendard,
                            fontWeight = FontWeight(500),
                            color = Color(0xFF686D78),
                        ),
                        modifier = Modifier.noRippleClickable {
                            isEssential2Checked = !isEssential2Checked
                        }
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Image(
                        imageVector = ImageVector.vectorResource(R.drawable.btn_announce_arrow),
                        contentDescription = "description",
                        modifier = Modifier
                            .noRippleClickable {

                            }
                    )
                }
                HeightSpacer(16.dp)
                NextButton(
                    mModifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp)
                        .height(58.dp),
                    text = "모두 동의하고 삭제하기",
                    buttonColor = if (isEssential1Checked && isEssential2Checked) primaryColor else Color(0xFFCADCF5),
                    onClick = {
                        if (isEssential1Checked && isEssential2Checked){
                            scope.launch {
                                bottomSheetState.hide()
                            }.invokeOnCompletion {
                                isSheetVisible = false
                            }

                        }
                    }
                )

            }
        }
    }
}
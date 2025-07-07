package com.pilltip.pilltip.composable.MainComposable

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pilltip.pilltip.R
import com.pilltip.pilltip.composable.HeightSpacer
import com.pilltip.pilltip.composable.noRippleClickable
import com.pilltip.pilltip.ui.theme.backgroundColor
import com.pilltip.pilltip.ui.theme.gray400
import com.pilltip.pilltip.ui.theme.pretendard

/**
 * 메인 화면에 있는 로고 이미지 필드 컴포저블 입니다.
 * @param horizontalPadding: 좌우패딩을 설정합니다.
 */
@Composable
fun LogoField(
    horizontalPadding: Dp = 22.dp,
    verticalPadding: Dp = 15.dp,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .height(54.dp)
            .fillMaxWidth()
            .background(backgroundColor)
            .padding(horizontal = horizontalPadding, vertical = verticalPadding),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            imageVector = ImageVector.vectorResource(id = R.drawable.logo_pilltip_blue_pill),
            contentDescription = "귀여운 필팁 알약",
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.weight(1f))
        Image(
            imageVector = ImageVector.vectorResource(id = R.drawable.btn_alarmbell),
            contentDescription = "alarm",
            modifier = Modifier
                .padding(1.dp)
                .height(24.dp)
                .noRippleClickable {
                    onClick()
                }
        )
    }
}

/**
 * 메인 화면에 있는 검색 필드 컴포저블 입니다.
 * @param horizontalPadding 좌우패딩을 설정합니다.
 */
@Composable
fun MainSearchField(
    horizontalPadding: Dp = 22.dp,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = horizontalPadding)
            .height(48.dp)
            .shadow(
                elevation = 8.dp,
                spotColor = Color(0x14000000),
                ambientColor = Color(0x14000000)
            )
            .background(color = Color(0xFFFFFFFF), shape = RoundedCornerShape(size = 12.dp))
            .padding(horizontal = 18.dp, vertical = 12.dp)
            .noRippleClickable { onClick() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "어떤 약이 필요하신가요?",
            style = TextStyle(
                fontSize = 14.sp,
                fontFamily = pretendard,
                fontWeight = FontWeight(400),
                color = gray400,
                textAlign = TextAlign.Start
            ),
            modifier = Modifier
                .height(19.dp)
                .weight(1f)
        )
        Image(
            imageVector = ImageVector.vectorResource(id = R.drawable.btn_gray_searchfield_magnifier),
            contentDescription = "logo",
            modifier = Modifier
                .height(20.dp)
                .width(20.dp)
                .padding(1.dp)
        )
    }
}

/**
 * 메인 화면에 있는 작은 카드입니다.
 * @param HeaderText: title을 작성합니다.
 * @param SubHeaderText: desc을 작성합니다.
 * @param ImageField: 이미지를 추가해줍니다.
 */
@Composable
fun SmallTabCard(
    HeaderText: String = "테스트 문구",
    SubHeaderText: String = "테스트 문구\n테스트 문구",
    ImageField: Int,
    onClick: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(116.dp)
            .shadow(
                elevation = 8.dp,
                spotColor = Color(0x14000000),
                ambientColor = Color(0x14000000)
            )
            .background(
                color = Color(0xFFFFFFFF),
                shape = RoundedCornerShape(size = 12.dp)
            )
            .padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 14.dp)
            .clickable {
                onClick()
            }
    ) {
        Column {
            Text(
                text = HeaderText,
                style = TextStyle(
                    fontSize = 20.sp,
                    fontFamily = pretendard,
                    fontWeight = FontWeight(600),
                    color = Color(0xFF323439)
                )
            )
            Row {
                Text(
                    text = SubHeaderText,
                    style = TextStyle(
                        fontSize = 12.sp,
                        lineHeight = 16.sp,
                        fontFamily = pretendard,
                        fontWeight = FontWeight(500),
                        color = Color(0xFF858C9A),
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .padding(top = 5.44.dp)
                )
                Image(
                    painter = painterResource(id = ImageField),
                    contentDescription = "logo",
                    modifier = Modifier
                        .width(60.dp)
                        .height(57.dp)
                )
            }
        }
    }
}

/**
 * 메인 화면에 있는 공지사항 카드입니다.
 * announcementText: 공지사항 카드 설명란
 */
@Composable
fun AnnouncementCard(announcementText: String = "TEST") {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .height(82.dp)
            .shadow(
                elevation = 8.dp,
                spotColor = Color(0x14000000),
                ambientColor = Color(0x14000000)
            )
            .background(color = Color(0xFFFFFFFF), shape = RoundedCornerShape(size = 12.dp))
            .padding(start = 15.dp, top = 14.dp, end = 15.dp, bottom = 14.dp)
            .noRippleClickable {  },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_announcement),
            contentDescription = "megaphone",
            modifier = Modifier
                .offset(x = -2.dp)
                .width(47.dp)
                .height(41.dp)
        )
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = "공지사항",
                style = TextStyle(
                    fontSize = 12.sp,
                    fontFamily = pretendard,
                    fontWeight = FontWeight(500),
                    color = Color(0xFF949BA8)
                )
            )
            HeightSpacer(4.dp)
            Text(
                text = announcementText,
                style = TextStyle(
                    fontSize = 14.sp,
                    lineHeight = 18.sp,
                    fontFamily = pretendard,
                    fontWeight = FontWeight(500),
                    color = Color(0xFF686D78)
                )
            )
        }
//        Spacer(modifier = Modifier.weight(1f))
        Image(
            imageVector = ImageVector.vectorResource(id = R.drawable.btn_announce_arrow),
            contentDescription = "arrow",
            modifier = Modifier
                .padding(1.dp)
                .width(20.dp)
                .height(20.dp)
        )
    }
}
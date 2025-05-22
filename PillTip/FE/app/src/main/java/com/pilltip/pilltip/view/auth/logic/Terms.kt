package com.pilltip.pilltip.view.auth.logic

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pilltip.pilltip.composable.HeightSpacer
import com.pilltip.pilltip.ui.theme.pretendard

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun EssentialTerms(){
    BoxWithConstraints(
        Modifier
            .padding(0.5.dp)
            .fillMaxWidth()
            .heightIn(max = 300.dp)
            .background(
                color = Color(0xFFF9FAFB),
                shape = RoundedCornerShape(size = 18.dp)
            )
            .padding(horizontal = 24.dp, vertical = 26.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Column{
            Text(
                text = "제  1 조 (목적)",
                style = TextStyle(
                    fontSize = 16.sp,
                    lineHeight = 22.sp,
                    fontFamily = pretendard,
                    fontWeight = FontWeight(600),
                    color = Color(0xFF686D78),
                )
            )
            HeightSpacer(12.dp)
            Text(
                text = "본 약관은 본인확인 서비스 제공자가 이용자의 본인확인을 위해 개인정보를 수집, 이용, 제공하는 것과 관련하여 이용자의 권리 및 의무를 규정하는 것을 목적으로 합니다.",
                style = TextStyle(
                    fontSize = 14.sp,
                    lineHeight = 22.sp,
                    fontFamily = pretendard,
                    fontWeight = FontWeight(400),
                    color = Color(0xFF858C9A),
                )
            )
            HeightSpacer(32.dp)
            Text(
                text = "제  2 조 (개인정보 수집 및 이용 동의)",
                style = TextStyle(
                    fontSize = 16.sp,
                    lineHeight = 22.sp,
                    fontFamily = pretendard,
                    fontWeight = FontWeight(600),
                    color = Color(0xFF686D78),
                )
            )
            HeightSpacer(12.dp)
            Text(
                text = "1.  수집하는 개인정보 항목",
                style = TextStyle(
                    fontSize = 14.sp,
                    lineHeight = 22.sp,
                    fontFamily = pretendard,
                    fontWeight = FontWeight(600),
                    color = Color(0xFF686D78),
                )
            )
            HeightSpacer(4.dp)
            Text(
                text = "본인확인 서비스 제공자는 아래와 같은 개인정보를 수집합니다.\n" +
                        " · 이름\n · 생년월일\n · 성별\n · 휴대전화 번호\n" +
                        " · 본인확인정보(CI, DI)\n · 이동통신사 정보",
                style = TextStyle(
                    fontSize = 14.sp,
                    lineHeight = 22.sp,
                    fontFamily = pretendard,
                    fontWeight = FontWeight(400),
                    color = Color(0xFF858C9A),
                )
            )
            HeightSpacer(12.dp)
            Text(
                text = "2. 개인정보의 수집 및 이용 목적",
                style = TextStyle(
                    fontSize = 14.sp,
                    lineHeight = 22.sp,
                    fontFamily = pretendard,
                    fontWeight = FontWeight(600),
                    color = Color(0xFF686D78),
                )
            )
            HeightSpacer(4.dp)
            Text(
                text = "수집된 개인정보는 다음과 같은 목적을 위해 이용됩니다.\n" +
                        " · 본인 확인 및 인증 서비스 제공\n · 서비스 가입 및 이용 편의 제공\n · 부정 이용 방지 및 보안 강화",
                style = TextStyle(
                    fontSize = 14.sp,
                    lineHeight = 22.sp,
                    fontFamily = pretendard,
                    fontWeight = FontWeight(400),
                    color = Color(0xFF858C9A),
                )
            )
            HeightSpacer(12.dp)
            Text(
                text = "3. 개인정보 보유 및 이용 기간",
                style = TextStyle(
                    fontSize = 14.sp,
                    lineHeight = 22.sp,
                    fontFamily = pretendard,
                    fontWeight = FontWeight(600),
                    color = Color(0xFF686D78),
                )
            )
            HeightSpacer(4.dp)
            Text(
                text = " · 본인 확인이 완료된 후 해당 개인정보는 관련 법령에 따라 보관하며, 법적 보관 기간이 지난 후 즉시 파기됩니다.\n" +
                        " · CI 및 DI 등 본인확인 정보는 서비스 이용 기록 확인을 위해 보관될 수 있습니다.",
                style = TextStyle(
                    fontSize = 14.sp,
                    lineHeight = 22.sp,
                    fontFamily = pretendard,
                    fontWeight = FontWeight(400),
                    color = Color(0xFF858C9A),
                )
            )
            HeightSpacer(32.dp)
            Text(
                text = "제  3 조 (개인정보의 제3자 제공 동의)",
                style = TextStyle(
                    fontSize = 16.sp,
                    lineHeight = 22.sp,
                    fontFamily = pretendard,
                    fontWeight = FontWeight(600),
                    color = Color(0xFF686D78),
                )
            )
            HeightSpacer(12.dp)
            Text(
                text = "본 서비스는 이용자의 본인확인을 위해 아래와 같이 개인정보를 제3자에게 제공합니다.",
                style = TextStyle(
                    fontSize = 14.sp,
                    lineHeight = 22.sp,
                    fontFamily = pretendard,
                    fontWeight = FontWeight(400),
                    color = Color(0xFF858C9A),
                )
            )
            HeightSpacer(12.dp)
            Text(
                text = "1. 개인정보를 제공받는 자",
                style = TextStyle(
                    fontSize = 14.sp,
                    lineHeight = 22.sp,
                    fontFamily = pretendard,
                    fontWeight = FontWeight(600),
                    color = Color(0xFF686D78),
                )
            )
            HeightSpacer(4.dp)
            Text(
                text = " · 이동통신사(SKT, KT, LG U+)\n · 본인확인기관(KMC, 나이스평가정보, SCI 평가정보 등)",
                style = TextStyle(
                    fontSize = 14.sp,
                    lineHeight = 22.sp,
                    fontFamily = pretendard,
                    fontWeight = FontWeight(400),
                    color = Color(0xFF858C9A),
                )
            )
            HeightSpacer(12.dp)
            Text(
                text = "2. 제공 목적",
                style = TextStyle(
                    fontSize = 14.sp,
                    lineHeight = 22.sp,
                    fontFamily = pretendard,
                    fontWeight = FontWeight(600),
                    color = Color(0xFF686D78),
                )
            )
            HeightSpacer(4.dp)
            Text(
                text = "본인확인 및 인증 서비스 제공",
                style = TextStyle(
                    fontSize = 14.sp,
                    lineHeight = 22.sp,
                    fontFamily = pretendard,
                    fontWeight = FontWeight(400),
                    color = Color(0xFF858C9A),
                )
            )
            HeightSpacer(12.dp)
            Text(
                text = "3. 제공 항목",
                style = TextStyle(
                    fontSize = 14.sp,
                    lineHeight = 22.sp,
                    fontFamily = pretendard,
                    fontWeight = FontWeight(600),
                    color = Color(0xFF686D78),
                )
            )
            HeightSpacer(4.dp)
            Text(
                text = "이름, 생년월일, 성별, 휴대전화 번호, 본인확인정보(CI, DI), 인증결과",
                style = TextStyle(
                    fontSize = 14.sp,
                    lineHeight = 22.sp,
                    fontFamily = pretendard,
                    fontWeight = FontWeight(400),
                    color = Color(0xFF858C9A),
                )
            )
            HeightSpacer(12.dp)
            Text(
                text = "4. 개인정보 보유 및 이용 기간",
                style = TextStyle(
                    fontSize = 14.sp,
                    lineHeight = 22.sp,
                    fontFamily = pretendard,
                    fontWeight = FontWeight(600),
                    color = Color(0xFF686D78),
                )
            )
            HeightSpacer(4.dp)
            Text(
                text = "제공된 정보는 본인확인 용도로만 사용되며, 이용 목적 달성 후 즉시 파기됩니다.",
                style = TextStyle(
                    fontSize = 14.sp,
                    lineHeight = 22.sp,
                    fontFamily = pretendard,
                    fontWeight = FontWeight(400),
                    color = Color(0xFF858C9A),
                )
            )
            HeightSpacer(32.dp)
            Text(
                text = "제  4 조 (고유식별정보 처리 동의)",
                style = TextStyle(
                    fontSize = 16.sp,
                    lineHeight = 22.sp,
                    fontFamily = pretendard,
                    fontWeight = FontWeight(600),
                    color = Color(0xFF686D78),
                )
            )
            HeightSpacer(12.dp)
            Text(
                text = "법률에 따라 특정 서비스 이용 시 주민등록번호 등 고유식별정보가 요구될 수 있습니다.",
                style = TextStyle(
                    fontSize = 14.sp,
                    lineHeight = 22.sp,
                    fontFamily = pretendard,
                    fontWeight = FontWeight(400),
                    color = Color(0xFF858C9A),
                )
            )
            HeightSpacer(12.dp)
            Text(
                text = "1. 수집 항목",
                style = TextStyle(
                    fontSize = 14.sp,
                    lineHeight = 22.sp,
                    fontFamily = pretendard,
                    fontWeight = FontWeight(600),
                    color = Color(0xFF686D78),
                )
            )
            HeightSpacer(4.dp)
            Text(
                text = " · 주민등록번호(해당 시)\n · 외국인등록번호(해당 시)",
                style = TextStyle(
                    fontSize = 14.sp,
                    lineHeight = 22.sp,
                    fontFamily = pretendard,
                    fontWeight = FontWeight(400),
                    color = Color(0xFF858C9A),
                )
            )
            HeightSpacer(12.dp)
            Text(
                text = "2. 이용 목적",
                style = TextStyle(
                    fontSize = 14.sp,
                    lineHeight = 22.sp,
                    fontFamily = pretendard,
                    fontWeight = FontWeight(600),
                    color = Color(0xFF686D78),
                )
            )
            HeightSpacer(4.dp)
            Text(
                text = "금융 서비스(대출, 보험 가입 등) 또는 특정 기관의 본인 확인 절차 수행",
                style = TextStyle(
                    fontSize = 14.sp,
                    lineHeight = 22.sp,
                    fontFamily = pretendard,
                    fontWeight = FontWeight(400),
                    color = Color(0xFF858C9A),
                )
            )
            HeightSpacer(12.dp)
            Text(
                text = "3. 보유 및 이용 기간",
                style = TextStyle(
                    fontSize = 14.sp,
                    lineHeight = 22.sp,
                    fontFamily = pretendard,
                    fontWeight = FontWeight(600),
                    color = Color(0xFF686D78),
                )
            )
            HeightSpacer(4.dp)
            Text(
                text = "법령에 따라 보관 의무가 있는 경우 해당 기간 동안 보관 후 파기",
                style = TextStyle(
                    fontSize = 14.sp,
                    lineHeight = 22.sp,
                    fontFamily = pretendard,
                    fontWeight = FontWeight(400),
                    color = Color(0xFF858C9A),
                )
            )
            HeightSpacer(32.dp)
            Text(
                text = "제  5 조 (통신사 본인확인 서비스 이용 동의)",
                style = TextStyle(
                    fontSize = 16.sp,
                    lineHeight = 22.sp,
                    fontFamily = pretendard,
                    fontWeight = FontWeight(600),
                    color = Color(0xFF686D78),
                )
            )
            HeightSpacer(12.dp)
            Text(
                text = "본 서비스는 이동통신사를 통해 본인확인 절차를 수행합니다.",
                style = TextStyle(
                    fontSize = 14.sp,
                    lineHeight = 22.sp,
                    fontFamily = pretendard,
                    fontWeight = FontWeight(400),
                    color = Color(0xFF858C9A),
                )
            )
            HeightSpacer(12.dp)
            Text(
                text = "1. 본인확인 방식",
                style = TextStyle(
                    fontSize = 14.sp,
                    lineHeight = 22.sp,
                    fontFamily = pretendard,
                    fontWeight = FontWeight(600),
                    color = Color(0xFF686D78),
                )
            )
            HeightSpacer(4.dp)
            Text(
                text = " · SMS 인증\n · 이동통신사 앱을 통한 인증",
                style = TextStyle(
                    fontSize = 14.sp,
                    lineHeight = 22.sp,
                    fontFamily = pretendard,
                    fontWeight = FontWeight(400),
                    color = Color(0xFF858C9A),
                )
            )
            HeightSpacer(12.dp)
            Text(
                text = "2. 이용 목적",
                style = TextStyle(
                    fontSize = 14.sp,
                    lineHeight = 22.sp,
                    fontFamily = pretendard,
                    fontWeight = FontWeight(600),
                    color = Color(0xFF686D78),
                )
            )
            HeightSpacer(4.dp)
            Text(
                text = "본인확인 및 서비스 이용 편의 제공",
                style = TextStyle(
                    fontSize = 14.sp,
                    lineHeight = 22.sp,
                    fontFamily = pretendard,
                    fontWeight = FontWeight(400),
                    color = Color(0xFF858C9A),
                )
            )
            HeightSpacer(12.dp)
            Text(
                text = "3. 이용자의 권리",
                style = TextStyle(
                    fontSize = 14.sp,
                    lineHeight = 22.sp,
                    fontFamily = pretendard,
                    fontWeight = FontWeight(600),
                    color = Color(0xFF686D78),
                )
            )
            HeightSpacer(4.dp)
            Text(
                text = "이용자는 본인확인 요청을 거부할 수 있으며, 이 경우 서비스 이용이 제한될 수 있습니다.",
                style = TextStyle(
                    fontSize = 14.sp,
                    lineHeight = 22.sp,
                    fontFamily = pretendard,
                    fontWeight = FontWeight(400),
                    color = Color(0xFF858C9A),
                )
            )
            HeightSpacer(32.dp)
            Text(
                text = "제  6 조 (전자서명 및 인증서 이용 동의)",
                style = TextStyle(
                    fontSize = 16.sp,
                    lineHeight = 22.sp,
                    fontFamily = pretendard,
                    fontWeight = FontWeight(600),
                    color = Color(0xFF686D78),
                )
            )
            HeightSpacer(12.dp)
            Text(
                text = "본인확인 서비스는 공인인증서를 활용할 수 있습니다.",
                style = TextStyle(
                    fontSize = 14.sp,
                    lineHeight = 22.sp,
                    fontFamily = pretendard,
                    fontWeight = FontWeight(400),
                    color = Color(0xFF858C9A),
                )
            )
            HeightSpacer(12.dp)
            Text(
                text = "1. 인증서 이용 목적",
                style = TextStyle(
                    fontSize = 14.sp,
                    lineHeight = 22.sp,
                    fontFamily = pretendard,
                    fontWeight = FontWeight(600),
                    color = Color(0xFF686D78),
                )
            )
            HeightSpacer(4.dp)
            Text(
                text = " · 본인 확인 및 전자서명 서비스 제공",
                style = TextStyle(
                    fontSize = 14.sp,
                    lineHeight = 22.sp,
                    fontFamily = pretendard,
                    fontWeight = FontWeight(400),
                    color = Color(0xFF858C9A),
                )
            )
            HeightSpacer(12.dp)
            Text(
                text = "2. 보유 및 이용 기간",
                style = TextStyle(
                    fontSize = 14.sp,
                    lineHeight = 22.sp,
                    fontFamily = pretendard,
                    fontWeight = FontWeight(600),
                    color = Color(0xFF686D78),
                )
            )
            HeightSpacer(4.dp)
            Text(
                text = "인증서 정보는 서비스 제공 후 즉시 파기됩니다.",
                style = TextStyle(
                    fontSize = 14.sp,
                    lineHeight = 22.sp,
                    fontFamily = pretendard,
                    fontWeight = FontWeight(400),
                    color = Color(0xFF858C9A),
                )
            )
            HeightSpacer(32.dp)
            Text(
                text = "제  7 조 (이용자의 권리 및 행사 방법)",
                style = TextStyle(
                    fontSize = 16.sp,
                    lineHeight = 22.sp,
                    fontFamily = pretendard,
                    fontWeight = FontWeight(600),
                    color = Color(0xFF686D78),
                )
            )
            HeightSpacer(12.dp)
            Text(
                text = "이용자는 언제든지 개인정보 제공에 대한 동의를 철회할 수 있으며, 본인확인 정보의 열람, 정정, 삭제를 요청할 수 있습니다.",
                style = TextStyle(
                    fontSize = 14.sp,
                    lineHeight = 22.sp,
                    fontFamily = pretendard,
                    fontWeight = FontWeight(400),
                    color = Color(0xFF858C9A),
                )
            )
            HeightSpacer(12.dp)
            Text(
                text = "1. 권리 행사 방법",
                style = TextStyle(
                    fontSize = 14.sp,
                    lineHeight = 22.sp,
                    fontFamily = pretendard,
                    fontWeight = FontWeight(600),
                    color = Color(0xFF686D78),
                )
            )
            HeightSpacer(4.dp)
            Text(
                text = " · 고객센터를 통한 요청\n · 서비스 내 설정을 통한 변경",
                style = TextStyle(
                    fontSize = 14.sp,
                    lineHeight = 22.sp,
                    fontFamily = pretendard,
                    fontWeight = FontWeight(400),
                    color = Color(0xFF858C9A),
                )
            )
            HeightSpacer(12.dp)
            Text(
                text = "2. 개인정보 보호 책임자 연락처",
                style = TextStyle(
                    fontSize = 14.sp,
                    lineHeight = 22.sp,
                    fontFamily = pretendard,
                    fontWeight = FontWeight(600),
                    color = Color(0xFF686D78),
                )
            )
            HeightSpacer(4.dp)
            Text(
                text = " · 개인정보 보호 담당자: (담당자명)\n · 연락처: (전화번호 또는 이메일)",
                style = TextStyle(
                    fontSize = 14.sp,
                    lineHeight = 22.sp,
                    fontFamily = pretendard,
                    fontWeight = FontWeight(400),
                    color = Color(0xFF858C9A),
                )
            )
            HeightSpacer(32.dp)
            Text(
                text = "제  8 조 (기타 사항)",
                style = TextStyle(
                    fontSize = 16.sp,
                    lineHeight = 22.sp,
                    fontFamily = pretendard,
                    fontWeight = FontWeight(600),
                    color = Color(0xFF686D78),
                )
            )
            HeightSpacer(12.dp)
            Text(
                text = " · 본 약관은 관련 법령의 변경 및 회사의 내부 정책에 따라 변경될 수 있으며, 변경 사항은 사전 고지 후 적용됩니다.\n" +
                        " · 본 약관에 대한 문의는 고객센터를 통해 가능합니다.",
                style = TextStyle(
                    fontSize = 14.sp,
                    lineHeight = 22.sp,
                    fontFamily = pretendard,
                    fontWeight = FontWeight(400),
                    color = Color(0xFF858C9A),
                )
            )
            HeightSpacer(32.dp)
            Text(
                text = "부칙",
                style = TextStyle(
                    fontSize = 16.sp,
                    lineHeight = 22.sp,
                    fontFamily = pretendard,
                    fontWeight = FontWeight(600),
                    color = Color(0xFF686D78),
                )
            )
            HeightSpacer(12.dp)
            Text(
                text = " · 본 약관은 (2026년 01월 01일)부터 적용됩니다.",
                style = TextStyle(
                    fontSize = 14.sp,
                    lineHeight = 22.sp,
                    fontFamily = pretendard,
                    fontWeight = FontWeight(400),
                    color = Color(0xFF858C9A),
                )
            )
        }
    }
}

@Composable
fun OptionalTerms(){
    Column(
        Modifier
            .padding(0.5.dp)
            .fillMaxWidth()
            .background(
                color = Color(0xFFF9FAFB),
                shape = RoundedCornerShape(size = 18.dp)
            )
            .padding(start = 24.dp, top = 26.dp, end = 24.dp, bottom = 26.dp)
    ) {
        Text(
            text = "제  9 조 (마케팅 활용 동의 - 선택 사항)",
            style = TextStyle(
                fontSize = 16.sp,
                lineHeight = 22.sp,
                fontFamily = pretendard,
                fontWeight = FontWeight(600),
                color = Color(0xFF686D78),
            )
        )
        HeightSpacer(12.dp)
        Text(
            text = "서비스 제공자는 이용자의 동의 하에 마케팅 목적으로 개인정보를 활용할 수 있습니다.",
            style = TextStyle(
                fontSize = 14.sp,
                lineHeight = 22.sp,
                fontFamily = pretendard,
                fontWeight = FontWeight(400),
                color = Color(0xFF858C9A),
            )
        )
        HeightSpacer(12.dp)
        Text(
            text = "1. 마케팅 활용 목적",
            style = TextStyle(
                fontSize = 14.sp,
                lineHeight = 22.sp,
                fontFamily = pretendard,
                fontWeight = FontWeight(600),
                color = Color(0xFF686D78),
            )
        )
        HeightSpacer(4.dp)
        Text(
            text = " · 이벤트 및 혜택 안내\n · 서비스 관련 공지사항 제공",
            style = TextStyle(
                fontSize = 14.sp,
                lineHeight = 22.sp,
                fontFamily = pretendard,
                fontWeight = FontWeight(400),
                color = Color(0xFF858C9A),
            )
        )
        HeightSpacer(12.dp)
        Text(
            text = "2. 활용 항목",
            style = TextStyle(
                fontSize = 14.sp,
                lineHeight = 22.sp,
                fontFamily = pretendard,
                fontWeight = FontWeight(600),
                color = Color(0xFF686D78),
            )
        )
        HeightSpacer(4.dp)
        Text(
            text = "이름, 휴대전화 번호, 이메일 주소",
            style = TextStyle(
                fontSize = 14.sp,
                lineHeight = 22.sp,
                fontFamily = pretendard,
                fontWeight = FontWeight(400),
                color = Color(0xFF858C9A),
            )
        )
        HeightSpacer(12.dp)
        Text(
            text = "3. 보유 및 이용 기간",
            style = TextStyle(
                fontSize = 14.sp,
                lineHeight = 22.sp,
                fontFamily = pretendard,
                fontWeight = FontWeight(600),
                color = Color(0xFF686D78),
            )
        )
        HeightSpacer(4.dp)
        Text(
            text = "이용자의 동의 철회 시 즉시 파기",
            style = TextStyle(
                fontSize = 14.sp,
                lineHeight = 22.sp,
                fontFamily = pretendard,
                fontWeight = FontWeight(400),
                color = Color(0xFF858C9A),
            )
        )
        HeightSpacer(12.dp)
        Text(
            text = "4. 동의 철회 방법",
            style = TextStyle(
                fontSize = 14.sp,
                lineHeight = 22.sp,
                fontFamily = pretendard,
                fontWeight = FontWeight(600),
                color = Color(0xFF686D78),
            )
        )
        HeightSpacer(4.dp)
        Text(
            text = " · 고객센터 또는 서비스 내 설정을 통해 마케팅 동의를 철회할 수 있습니다.",
            style = TextStyle(
                fontSize = 14.sp,
                lineHeight = 22.sp,
                fontFamily = pretendard,
                fontWeight = FontWeight(400),
                color = Color(0xFF858C9A),
            )
        )
    }
}
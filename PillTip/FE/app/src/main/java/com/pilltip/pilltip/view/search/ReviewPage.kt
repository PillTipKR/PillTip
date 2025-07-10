package com.pilltip.pilltip.view.search

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.pilltip.pilltip.composable.HeightSpacer
import com.pilltip.pilltip.composable.SearchComposable.DashedBorderBox
import com.pilltip.pilltip.composable.SearchComposable.EfficiencyRow
import com.pilltip.pilltip.composable.SearchComposable.ReviewItemCard
import com.pilltip.pilltip.composable.SearchComposable.ReviewRatingBar
import com.pilltip.pilltip.composable.SearchComposable.StarRatingBar
import com.pilltip.pilltip.composable.WidthSpacer
import com.pilltip.pilltip.model.UserInfoManager
import com.pilltip.pilltip.model.search.DetailDrugData
import com.pilltip.pilltip.model.search.ReviewListData
import com.pilltip.pilltip.model.search.ReviewStatsData
import com.pilltip.pilltip.model.search.ReviewViewModel
import com.pilltip.pilltip.model.search.SearchHiltViewModel
import com.pilltip.pilltip.ui.theme.gray050
import com.pilltip.pilltip.ui.theme.gray100
import com.pilltip.pilltip.ui.theme.gray200
import com.pilltip.pilltip.ui.theme.gray500
import com.pilltip.pilltip.ui.theme.gray800
import com.pilltip.pilltip.ui.theme.pretendard
import com.pilltip.pilltip.ui.theme.primaryColor

@Composable
fun ReviewTab(
    navController: NavController,
    detail: DetailDrugData,
    searchHiltViewModel: SearchHiltViewModel,
    reviewViewModel: ReviewViewModel
) {
    val reviewStats by searchHiltViewModel.reviewStats.collectAsState()

    LaunchedEffect(Unit) {
        searchHiltViewModel.fetchReviewStats(detail.id)
    }
    Column(

    ) {
        reviewStats?.let { ReviewStatisticsSection(navController, it) }
        HorizontalDivider(thickness = 10.dp, color = gray100)
        reviewStats?.let { ReviewSection(navController, reviewStats, reviewViewModel, detail.id) }
        HeightSpacer(100.dp)

    }

}

@Composable
fun ReviewStatisticsSection(
    navController: NavController,
    reviewStats: ReviewStatsData
) {
    val nickname = UserInfoManager.getUserData(LocalContext.current)?.nickname
    Column(
        modifier = Modifier
            .fillMaxSize()
            .heightIn(min = 300.dp)
            .padding(start = 22.dp, end = 22.dp, top = 22.dp, bottom = 36.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "리뷰",
                style = TextStyle(
                    fontSize = 18.sp,
                    fontFamily = pretendard,
                    fontWeight = FontWeight(600),
                    color = gray800,
                )
            )
            WidthSpacer(4.dp)
            Text(
                text = reviewStats?.total?.toString() ?: "-",
                style = TextStyle(
                    fontSize = 18.sp,
                    fontFamily = pretendard,
                    fontWeight = FontWeight(600),
                    color = primaryColor,
                )
            )
        }
        HeightSpacer(8.dp)
        Text(
            text = "${reviewStats?.like?.toString() ?: "0"}명의 이용자들이 해당 약품에 만족하고 있어요!",
            style = TextStyle(
                fontSize = 12.sp,
                fontFamily = pretendard,
                fontWeight = FontWeight(600),
                color = primaryColor,
            )
        )
        HeightSpacer(18.dp)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(148.dp)
                .background(color = gray050, shape = RoundedCornerShape(size = 16.dp))
                .padding(start = 22.dp, top = 24.dp, end = 22.dp, bottom = 24.dp)
        ) {
            Column(
                modifier = Modifier.weight(1.2f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = reviewStats.ratingStatsResponse.average.toString(),
                    style = TextStyle(
                        fontSize = 32.sp,
                        fontFamily = pretendard,
                        fontWeight = FontWeight(600),
                        color = gray800,
                    ),
                    modifier = Modifier.height(38.dp)
                )
                HeightSpacer(8.dp)
                StarRatingBar(
                    rating = reviewStats.ratingStatsResponse.average.toFloat()
                ) {}
                HeightSpacer(8.dp)
                Text(
                    text = "만족도 ${
                        reviewStats.ratingStatsResponse.average.toFloat().div(5)
                            .times(100).toInt()
                    }%",
                    style = TextStyle(
                        fontSize = 12.sp,
                        fontFamily = pretendard,
                        fontWeight = FontWeight(500),
                        color = gray500,
                    )
                )
            }
            VerticalDivider(
                thickness = 1.dp,
                color = gray200,
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(horizontal = 22.dp)
            )
            Column(
                modifier = Modifier
                    .weight(1.8f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                ReviewRatingBar(
                    title = "5점",
                    total = reviewStats?.total ?: 0,
                    progress = reviewStats?.ratingStatsResponse?.ratingCounts?.get("5")
                        ?.div(100f) ?: 0f,
                )
                ReviewRatingBar(
                    title = "4점",
                    total = reviewStats?.total ?: 0,
                    progress = reviewStats?.ratingStatsResponse?.ratingCounts?.get("4")
                        ?.div(100f) ?: 0f,
                )
                ReviewRatingBar(
                    title = "3점",
                    total = reviewStats?.total ?: 0,
                    progress = reviewStats?.ratingStatsResponse?.ratingCounts?.get("3")
                        ?.div(100f) ?: 0f,
                )
                ReviewRatingBar(
                    title = "2점",
                    total = reviewStats?.total ?: 0,
                    progress = reviewStats?.ratingStatsResponse?.ratingCounts?.get("2")
                        ?.div(100f) ?: 0f,
                )
                ReviewRatingBar(
                    title = "1점",
                    total = reviewStats?.total ?: 0,
                    progress = reviewStats?.ratingStatsResponse?.ratingCounts?.get("1")
                        ?.div(100f) ?: 0f,
                )
            }
        }
        HeightSpacer(6.dp)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(115.dp)
                .padding(start = 0.dp, top = 12.dp, end = 0.dp, bottom = 12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            EfficiencyRow(
                title = "효과",
                description = reviewStats?.tagStatsByType?.get("EFFICACY")?.mostUsedTagName
                    ?: "집계 중",
                percentage = reviewStats?.tagStatsByType?.get("EFFICACY")?.mostUsedTagCount ?: 0,
                num = reviewStats?.tagStatsByType?.get("EFFICACY")?.totalTagCount ?: 0
            )
            EfficiencyRow(
                title = "부작용",
                description = reviewStats?.tagStatsByType?.get("SIDE_EFFECT")?.mostUsedTagName
                    ?: "집계 중",
                percentage = reviewStats?.tagStatsByType?.get("SIDE_EFFECT")?.mostUsedTagCount ?: 0,
                num = reviewStats?.tagStatsByType?.get("SIDE_EFFECT")?.totalTagCount ?: 0
            )
            EfficiencyRow(
                title = "기타",
                description = reviewStats?.tagStatsByType?.get("OTHER")?.mostUsedTagName ?: "집계 중",
                percentage = reviewStats?.tagStatsByType?.get("OTHER")?.mostUsedTagCount ?: 0,
                num = reviewStats?.tagStatsByType?.get("OTHER")?.totalTagCount ?: 0
            )
        }
        HeightSpacer(12.dp)
        Text(
            text = "${nickname}님 맞춤 리뷰 ",
            style = TextStyle(
                fontSize = 16.sp,
                fontFamily = pretendard,
                fontWeight = FontWeight(600),
                color = primaryColor,
            ),
            modifier = Modifier.padding(vertical = 12.dp)
        )
        HeightSpacer(4.dp)
        DashedBorderBox(
            onRegisterClick = {
                navController.navigate("QuestionnairePage")
            }
        )
    }
}

@Composable
fun ReviewSection(
    navController: NavController,
    reviewStats: ReviewStatsData?,
    reviewViewModel: ReviewViewModel,
    id: Long
) {
    Log.d("진입", "여기")
    val reviewListData by reviewViewModel.reviewListData.collectAsState()
    LaunchedEffect(Unit) {
        reviewViewModel.loadReviews(id)
    }
    val localHeight = LocalConfiguration.current.screenHeightDp
    LazyColumn(
        modifier = Modifier.height(if (reviewStats?.total == 0) 200.dp else localHeight.dp),
        contentPadding = PaddingValues(horizontal = 0.dp, vertical = 0.dp)
    ) {
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 22.dp, top = 24.dp, bottom = 16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "리뷰",
                        style = TextStyle(
                            fontSize = 16.sp,
                            fontFamily = pretendard,
                            fontWeight = FontWeight(600),
                            color = gray800,
                        )
                    )
                    WidthSpacer(4.dp)
                    Text(
                        text = "${reviewStats?.total ?: 0}",
                        style = TextStyle(
                            fontSize = 16.sp,
                            fontFamily = pretendard,
                            fontWeight = FontWeight(600),
                            color = primaryColor,
                        )
                    )
                }
            }

        }

        reviewListData?.content?.let { reviews ->
            items(reviews) { review ->
                ReviewItemCard(review)
                HorizontalDivider(thickness = 4.dp, color = gray100)
            }
        }
    }
}

@Composable
fun ReviewWritePage(

) {

}


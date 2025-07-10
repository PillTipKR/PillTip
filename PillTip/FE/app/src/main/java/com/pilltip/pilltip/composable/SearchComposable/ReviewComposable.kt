package com.pilltip.pilltip.composable.SearchComposable

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.kakao.sdk.common.model.Description
import com.pilltip.pilltip.R
import com.pilltip.pilltip.composable.HeightSpacer
import com.pilltip.pilltip.composable.QuestionnaireComposable.DottedDivider
import com.pilltip.pilltip.composable.WidthSpacer
import com.pilltip.pilltip.composable.noRippleClickable
import com.pilltip.pilltip.model.search.ReviewItem
import com.pilltip.pilltip.ui.theme.gray100
import com.pilltip.pilltip.ui.theme.gray200
import com.pilltip.pilltip.ui.theme.gray300
import com.pilltip.pilltip.ui.theme.gray400
import com.pilltip.pilltip.ui.theme.gray500
import com.pilltip.pilltip.ui.theme.gray600
import com.pilltip.pilltip.ui.theme.gray700
import com.pilltip.pilltip.ui.theme.gray800
import com.pilltip.pilltip.ui.theme.pretendard
import com.pilltip.pilltip.ui.theme.primaryColor
import okhttp3.internal.notify

@Composable
fun InteractiveImageRatingBar(
    rating: Int,
    onRatingChanged: (Int) -> Unit,
    modifier: Modifier = Modifier,
    maxRating: Int = 5,
    starSize: Dp = 32.dp,
    starSpacing: Dp = 4.dp,
    filledStarResId: Int = R.drawable.ic_review_star_fill, // 벡터 XML
    emptyStarResId: Int = R.drawable.ic_review_star_empty    // 벡터 XML
) {
    var containerWidth by remember { mutableStateOf(0f) }

    Box(
        modifier = modifier
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    val starWidth = containerWidth / maxRating
                    val touchedRating = ((offset.x / starWidth) + 1).toInt().coerceIn(1, maxRating)
                    onRatingChanged(touchedRating)
                }
            }
            .onGloballyPositioned { coordinates ->
                containerWidth = coordinates.size.width.toFloat()
            }
    ) {
        Row {
            for (i in 1..maxRating) {
                val resId = if (i <= rating) filledStarResId else emptyStarResId
                Image(
                    imageVector = ImageVector.vectorResource(id = resId),
                    contentDescription = "별 $i",
                    modifier = Modifier
                        .size(starSize)
                        .padding(end = if (i < maxRating) starSpacing else 0.dp)
                )
            }
        }
    }
}

@Composable
fun StarRatingBar(
    maxStars: Int = 5,
    size: Int = 18,
    rating: Float,
    onRatingChanged: (Float) -> Unit
) {
    val starSize = size.dp
    val starSpacing = 2.dp

    Row(
        modifier = Modifier.selectableGroup(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (i in 1..maxStars) {
            val isSelected = i <= rating
            val iconTintColor = if (isSelected) primaryColor else gray200
            Icon(
                imageVector = ImageVector.vectorResource(if (isSelected) R.drawable.ic_review_star_fill else R.drawable.ic_review_star_empty),
                contentDescription = null,
                tint = iconTintColor,
                modifier = Modifier
                    .selectable(
                        selected = isSelected,
                        onClick = {
                            onRatingChanged(i.toFloat())
                        }
                    )
                    .width(starSize)
                    .height(starSize)
            )

            if (i < maxStars) {
                Spacer(modifier = Modifier.width(starSpacing))
            }
        }
    }
}

@Composable
fun ReviewRatingBar(
    title: String,
    total: Int,
    progress: Float
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            style = TextStyle(
                fontSize = 12.sp,
                fontFamily = pretendard,
                fontWeight = FontWeight(400),
                color = gray800,
            ),
            modifier = Modifier.width(22.dp)
        )
        WidthSpacer(10.dp)
        LinearProgressIndicator(
            progress = if (total == 0) 0f else progress.div(total),
            modifier = Modifier
                .weight(1f)
                .height(6.dp)
                .clip(RoundedCornerShape(100.dp)),
            color = primaryColor,
            trackColor = gray200,
        )
        WidthSpacer(10.dp)
        Text(
            text = progress.toInt().toString(),
            style = TextStyle(
                fontSize = 12.sp,
                fontFamily = pretendard,
                fontWeight = FontWeight(400),
                color = gray800,
            ),
            modifier = Modifier.width(30.dp)
        )
    }
}

@Composable
fun EfficiencyRow(
    title: String,
    description: String,
    percentage: Int,
    num: Int
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            style = TextStyle(
                fontSize = 14.sp,
                fontFamily = pretendard,
                fontWeight = FontWeight(500),
                color = gray400
            ),
            modifier = Modifier.width(38.dp)
        )
        WidthSpacer(12.dp)
        Text(
            text = description,
            style = TextStyle(
                fontSize = 14.sp,
                fontFamily = pretendard,
                fontWeight = FontWeight(500),
                color = gray700
            )
        )
        WidthSpacer(20.dp)
        DottedDivider(
            color = gray200,
            modifier = Modifier.weight(1f)
        )
        WidthSpacer(20.dp)
        Text(
            text = "${if (num != 0) percentage / num * 100 else 0}%",
            style = TextStyle(
                fontSize = 14.sp,
                fontFamily = pretendard,
                fontWeight = FontWeight(500),
                color = gray700,
            ),
            modifier = Modifier.width(38.dp)
        )
        Text(
            text = "(${num}명)",
            style = TextStyle(
                fontSize = 14.sp,
                fontFamily = pretendard,
                fontWeight = FontWeight(400),
                color = gray400,
                textAlign = TextAlign.Right,
            ),
            modifier = Modifier.width(60.dp)
        )
    }
}

@Composable
fun ReviewItemCard(review: ReviewItem) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 22.dp)
            .padding(top = 24.dp, bottom = 18.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = review.userNickname,
                style = TextStyle(
                    fontSize = 14.sp,
                    fontFamily = pretendard,
                    fontWeight = FontWeight(600),
                    color = gray800,
                )
            )
            WidthSpacer(8.dp)
            StarRatingBar(
                rating = review.rating.toFloat(),
                size = 16
            ) {}
            WidthSpacer(6.dp)
            Text(
                text = "${review.rating}",
                style = TextStyle(
                    fontSize = 14.sp,
                    fontFamily = pretendard,
                    fontWeight = FontWeight(600),
                    color = gray800,
                )
            )
            Spacer(modifier = Modifier.weight(1f))
            Image(
                imageVector = ImageVector.vectorResource(R.drawable.btn_review_thumb),
                contentDescription = "좋아요",
                modifier = Modifier.noRippleClickable {

                }
            )
            WidthSpacer(4.dp)
            Text(
                text = "${review.isLiked}",
                style = TextStyle(
                    fontSize = 14.sp,
                    fontFamily = pretendard,
                    fontWeight = FontWeight(400),
                    color = gray400,
                )
            )
        }
        HeightSpacer(6.dp)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = review.gender,
                style = TextStyle(
                    fontSize = 14.sp,
                    fontFamily = pretendard,
                    fontWeight = FontWeight(500),
                    color = gray600,
                )
            )
            VerticalDivider(
                thickness = 1.dp,
                color = gray300,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
            Text(
                text = review.createdAt,
                style = TextStyle(
                    fontSize = 14.sp,
                    fontFamily = pretendard,
                    fontWeight = FontWeight(400),
                    color = gray400,
                )
            )
        }
        HeightSpacer(16.dp)
        ReviewImageRow(review.imageUrls)
        HeightSpacer(16.dp)
        Text(
            text = review.content,
            style = TextStyle(
                fontSize = 14.sp,
                fontFamily = pretendard,
                fontWeight = FontWeight(400),
                color = gray800,
            )
        )
        ExpandableText(text = review.content)
        HeightSpacer(16.dp)
        if (review.efficacyTags.isNotEmpty()) {
            Row {
                review.efficacyTags.forEach {
                    TagChip(label = it)
                }
            }
        }
        HorizontalDivider(
            thickness = 1.dp,
            color = gray100,
            modifier = Modifier.padding(vertical = 18.dp)
        )
        Text(
            text = "신고하기",
            style = TextStyle(
                fontSize = 12.sp,
                fontFamily = pretendard,
                fontWeight = FontWeight(400),
                color = gray500,
            )
        )
    }
}

@Composable
fun TagChip(label: String) {
    Box(
        modifier = Modifier
            .border(width = 1.dp, color = gray300, shape = RoundedCornerShape(size = 1000.dp))
            .height(30.dp)
            .background(color = Color(0xFFFFFFFF), shape = RoundedCornerShape(size = 1000.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Text(
            text = label,
            style = TextStyle(
                fontSize = 12.sp,
                fontFamily = pretendard,
                fontWeight = FontWeight(400),
                color = gray800,
            )
        )
    }
}

@Composable
fun ReviewImageRow(imageUrls: List<String>) {
    if (imageUrls.isEmpty()) return

    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        items(imageUrls.size) { index ->
            AsyncImage(
                model = imageUrls[index],
                contentDescription = "리뷰 이미지 $index",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(100.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.LightGray)
            )
        }
    }
}

@Composable
fun ExpandableText(
    text: String,
    maxLength: Int = 300
) {
    var expanded by remember { mutableStateOf(false) }

    val displayText = remember(text, expanded) {
        if (expanded || text.length <= maxLength) {
            text
        } else {
            text.take(maxLength) + "..."
        }
    }

    Text(
        text = displayText,
        style = TextStyle(
            fontSize = 14.sp,
            fontFamily = pretendard,
            fontWeight = FontWeight(400),
            color = gray800,
        ),
        modifier = Modifier
            .fillMaxWidth()
            .noRippleClickable {
                if (text.length > maxLength) expanded = !expanded
            }
    )
}
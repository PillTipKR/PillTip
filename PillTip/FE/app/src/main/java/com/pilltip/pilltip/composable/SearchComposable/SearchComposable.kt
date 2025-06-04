package com.pilltip.pilltip.composable.SearchComposable

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.pilltip.pilltip.R
import com.pilltip.pilltip.composable.HeightSpacer
import com.pilltip.pilltip.composable.WidthSpacer
import com.pilltip.pilltip.composable.noRippleClickable
import com.pilltip.pilltip.model.search.DrugSearchResult
import com.pilltip.pilltip.model.search.SearchData
import com.pilltip.pilltip.model.search.LogViewModel
import com.pilltip.pilltip.ui.theme.gray050
import com.pilltip.pilltip.ui.theme.gray100
import com.pilltip.pilltip.ui.theme.gray200
import com.pilltip.pilltip.ui.theme.gray500
import com.pilltip.pilltip.ui.theme.gray600
import com.pilltip.pilltip.ui.theme.gray700
import com.pilltip.pilltip.ui.theme.gray800
import com.pilltip.pilltip.ui.theme.pretendard
import com.pilltip.pilltip.ui.theme.primaryColor
import kotlinx.coroutines.flow.distinctUntilChanged

@Composable
fun PillSearchField(
    initialQuery: String ="",
    navController: NavController,
    nowTyping: (String) -> Unit,
    searching: (String) -> Unit,
    onNavigateToResult: (String) -> Unit
) {
    var inputText by remember { mutableStateOf(initialQuery) }
    val keyboardController = LocalSoftwareKeyboardController.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            imageVector = ImageVector.vectorResource(R.drawable.btn_left_gray_arrow),
            contentDescription = "뒤로가기",
            modifier = Modifier
                .size(20.dp)
                .noRippleClickable { navController.navigate("PillMainPage") }
        )
        WidthSpacer(14.dp)
        Row(
            modifier = Modifier
                .weight(1f)
                .height(44.dp)
                .background(color = gray100, shape = RoundedCornerShape(size = 12.dp))
                .padding(start = 16.dp, top = 12.dp, end = 14.dp, bottom = 12.dp)
        ) {
            BasicTextField(
                value = inputText,
                onValueChange = {
                    inputText = it
                    nowTyping(it)
                },
                modifier = Modifier.weight(1f),
                textStyle = TextStyle(
                    fontSize = 14.sp,
                    fontFamily = pretendard,
                    fontWeight = FontWeight.W500,
                    color = gray800
                ),
                keyboardOptions = KeyboardOptions.Default.copy(
                    imeAction = ImeAction.Search
                ),
                keyboardActions = KeyboardActions(
                    onSearch = {
                        searching(inputText)
                        keyboardController?.hide()
                        onNavigateToResult(inputText)
                    }
                ),
                decorationBox = { innerTextField ->
                    Box(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (inputText.isEmpty()) {
                            Text(
                                modifier = Modifier
                                    .fillMaxWidth(),
                                text = "어떤 약이 필요하신가요?",
                                style = TextStyle(
                                    fontFamily = pretendard,
                                    fontWeight = FontWeight.W400,
                                    fontSize = 14.sp,
                                    color = gray600
                                )
                            )
                        }
                        innerTextField()
                    }
                }
            )
            Image(
                imageVector = ImageVector.vectorResource(R.drawable.btn_search_mic),
                contentDescription = "음성 검색",
                modifier = Modifier
                    .size(20.dp)
                    .padding(1.dp)
                    .noRippleClickable { }
            )
        }
        WidthSpacer(6.dp)
        Box(
            modifier = Modifier
                .width(44.dp)
                .height(44.dp)
                .background(color = primaryColor, shape = RoundedCornerShape(size = 12.dp))
                .padding(start = 13.dp, top = 8.dp, end = 13.dp, bottom = 8.dp)
                .noRippleClickable { }
        ) {
            Column() {
                Image(
                    imageVector = ImageVector.vectorResource(R.drawable.btn_search_camera),
                    contentDescription = "카메라 검색"
                )
                Text(
                    text = "검색",
                    fontSize = 10.sp,
                    fontFamily = pretendard,
                    fontWeight = FontWeight(400),
                    color = Color(0xFFFDFDFD)
                )
            }
        }
    }
}

@Composable
fun SearchTag(
    tagText: String,
    onNavigateToResult: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .height(30.dp)
            .background(color = gray100, shape = RoundedCornerShape(size = 100.dp))
            .padding(start = 12.dp, top = 8.dp, end = 12.dp, bottom = 8.dp)
            .noRippleClickable { onNavigateToResult() },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Text(
            text = tagText,
            fontSize = 12.sp,
            fontFamily = pretendard,
            fontWeight = FontWeight(500),
            color = gray700,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.widthIn(max = 80.dp)
        )
        WidthSpacer(4.dp)
        Image(
            imageVector = ImageVector.vectorResource(R.drawable.btn_tag_erase),
            contentDescription = "최근 검색어 삭제",
            modifier = Modifier
                .size(14.dp)
                .padding(1.dp)
                .noRippleClickable { onDelete() }
        )
    }
}

@Composable
fun HighlightedText(fullText: String, keyword: String) {
    val start = fullText.indexOf(keyword, ignoreCase = true)
    if (start < 0) {
        Text(
            fullText,
            fontSize = 16.sp,
            fontFamily = pretendard,
            fontWeight = FontWeight(400),
            color = gray500
        )
        return
    }
    val end = start + keyword.length

    val annotated = buildAnnotatedString {
        append(fullText.substring(0, start))
        withStyle(
            SpanStyle(
                color = primaryColor,
                fontFamily = pretendard,
                fontWeight = FontWeight.Bold
            )
        ) {
            append(fullText.substring(start, end))
        }
        append(fullText.substring(end))
    }

    Text(
        annotated,
        fontSize = 16.sp,
        fontFamily = pretendard,
        fontWeight = FontWeight(400),
        color = gray500
    )
}

@Composable
fun AutoCompleteList(
    query: String,
    searched: List<SearchData>,
    onClick: (SearchData) -> Unit,
    onLoadMore: () -> Unit
) {
    val listState = rememberLazyListState()

    LazyColumn(state = listState) {
        items(searched) { item ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onClick(item) }
                    .padding(horizontal = 22.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    imageVector = ImageVector.vectorResource(R.drawable.logo_pilltip_blue_pill),
                    contentDescription = null,
                    modifier = Modifier
                        .width(42.dp)
                        .height(36.dp)
                )
                Spacer(modifier = Modifier.width(14.dp))
                HighlightedText(fullText = item.value, keyword = query)
            }
        }
    }

    LaunchedEffect(listState) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
            .distinctUntilChanged()
            .collect { lastVisibleIndex ->
                if (lastVisibleIndex != null && lastVisibleIndex >= searched.size - 3) {
                    onLoadMore()
                }
            }
    }
}

@Composable
fun DrugSearchResultList(drugs: List<DrugSearchResult>) {
    LazyColumn(modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 22.dp, vertical = 16.dp)) {
        items(drugs) { drug ->
            DrugSearchResultCard(drug)
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
fun DrugSearchResultCard(drug: DrugSearchResult) {
    Card(modifier = Modifier.fillMaxWidth().height(202.dp)) {
        Column(
            modifier = Modifier.padding(
                top = 22.dp,
                bottom = 16.dp,
                start = 20.dp,
                end = 20.dp
            )
        ) {
            Row() {
                Image(
                    imageVector = ImageVector.vectorResource(R.drawable.logo_pilltip_blue_pill),
                    contentDescription = "기본 이미지",
                    Modifier
                        .border(
                            width = 0.6.dp,
                            color = gray200,
                            shape = RoundedCornerShape(size = 10.8.dp)
                        )
                        .width(90.dp)
                        .height(90.dp)
                        .background(color = gray050, shape = RoundedCornerShape(size = 10.8.dp))
                        .padding(start = 19.79992.dp, end = 19.80008.dp)
                )
                Column() {
                    Text(
                        text = drug.drugName,
                        fontSize = 14.sp,
                        fontFamily = pretendard,
                        fontWeight = FontWeight(700),
                        color = Color(0xFF000000),
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row() {
                        Image(
                            imageVector = ImageVector.vectorResource(R.drawable.ic_rating_star),
                            contentDescription = "별점",
                            modifier = Modifier.size(12.dp)
                        )
                        WidthSpacer(4.dp)
                        Text(
                            text = "0.0", /*아직 별점 데이터 없음. 향후 추가 예정*/
                            style = TextStyle(
                                fontSize = 12.sp,
                                fontFamily = pretendard,
                                fontWeight = FontWeight(500),
                                color = gray800,
                            )
                        )
                        WidthSpacer(2.dp)
                        Text(
                            text = "(0)", /*아직 별점 데이터 없음. 향후 추가 예정*/
                            style = TextStyle(
                                fontSize = 12.sp,
                                fontFamily = pretendard,
                                fontWeight = FontWeight(400),
                                color = gray800,
                            )
                        )
                        WidthSpacer(4.dp)
                        Box(
                            modifier = Modifier
                                .padding(1.dp)
                                .width(2.dp)
                                .height(2.dp)
                                .background(color = gray500)
                        )
                        WidthSpacer(4.dp)
                        Text(
                            text = "${drug.manufacturer}",
                            fontSize = 12.sp,
                            fontFamily = pretendard,
                            fontWeight = FontWeight(400),
                            color = gray500
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    drug.ingredients.forEach { ingredient ->
                        Text(
                            text = "- ${ingredient.name} (${ingredient.dose})" + if (ingredient.main) " [주성분]" else "",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
            HeightSpacer(14.dp)
            HeightSpacer(14.dp)

        }
    }
}

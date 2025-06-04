package com.pilltip.pilltip.model.search
/**
 * 약품 자동검색 API : http://164.125.253.20:20022/api/autocomplete/drugs?input=타이레놀&page=0
 *
 * */
data class SearchResponse( // JSON 전체 응답을 나타냄
    val data: List<SearchData>
)

data class SearchData( // 리스트 내부의 약품 정보 단위
    val type: String,
    val id: Long,
    val value: String
)

/**
 * 일반 검색 결과 API: http://164.125.253.20:20022/api/search/drugs?input=타이레놀&page=0
 *
 * */
data class DrugSearchResult(
    val id: Int,
    val drugName: String,
    val ingredients: List<Ingredient>,
    val manufacturer: String
)

data class Ingredient(
    val name: String, // 성분명
    val dose: String, // 함량
    val main: Boolean // 해당 약제 중 가장 성분함량이 높은 것에 true로 표시됨.
)
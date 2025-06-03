package com.pilltip.pilltip.model.search
/**
 * 약품 자동검색 API : http://164.125.253.20:20022/api/autocomplete/drugs?input=타이레놀&page=0
 *
 * */
data class SearchData(
    val type: String, // "drug"
    val id: String, // 약품 id
    val value: String // "타이레놀"
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
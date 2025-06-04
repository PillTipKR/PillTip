package com.pilltip.pilltip.model.search

import retrofit2.http.GET
import retrofit2.http.Query

/**
 * 약품명 자동완성 API
 * */
interface AutoCompleteApi {
    @GET("/api/autocomplete/drugs")
    suspend fun getAutoCompleteDatas(
        @Query("input") input: String,
        @Query("page") page: Int
    ): List<SearchData>
}

interface AutoCompleteRepository {
    suspend fun getAutoComplete(query: String, page: Int = 0): List<SearchData>
}

class AutoCompleteRepositoryImpl(
    private val api: AutoCompleteApi
) : AutoCompleteRepository {
    override suspend fun getAutoComplete(query: String, page: Int): List<SearchData> {
        return api.getAutoCompleteDatas(query, page)
    }
}

/**
 * 일반 검색 API
 * */
interface DrugSearchApi {
    @GET("/api/search/drugs")
    suspend fun searchDrugs(
        @Query("input") input: String,
        @Query("page") page: Int
    ): List<DrugSearchResult>
}

interface DrugSearchRepository {
    suspend fun search(query: String, page: Int = 0): List<DrugSearchResult>
}

class DrugSearchRepositoryImpl(
    private val api: DrugSearchApi
) : DrugSearchRepository {
    override suspend fun search(query: String, page: Int): List<DrugSearchResult> {
        return api.searchDrugs(query, page)
    }
}
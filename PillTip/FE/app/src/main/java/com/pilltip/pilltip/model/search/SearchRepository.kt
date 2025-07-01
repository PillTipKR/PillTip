package com.pilltip.pilltip.model.search

import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * 약품명 자동완성 API
 * */
interface AutoCompleteApi {
    @GET("/api/autocomplete/drugs")
    suspend fun getAutoCompleteDatas(
        @Query("input") input: String,
        @Query("page") page: Int
    ): SearchResponse
}

interface AutoCompleteRepository {
    suspend fun getAutoComplete(query: String, page: Int = 0): List<SearchData>
}

class AutoCompleteRepositoryImpl(
    private val api: AutoCompleteApi
) : AutoCompleteRepository {
    override suspend fun getAutoComplete(query: String, page: Int): List<SearchData> {
        return api.getAutoCompleteDatas(query, page).data
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
    ): DrugSearchResponse
}

interface DrugSearchRepository {
    suspend fun search(query: String, page: Int = 0): List<DrugSearchResult>
}

class DrugSearchRepositoryImpl(
    private val api: DrugSearchApi
) : DrugSearchRepository {
    override suspend fun search(query: String, page: Int): List<DrugSearchResult> {
        return api.searchDrugs(query, page).data
    }
}

/**
 * 약품 상세 페이지 API
 * */
interface DrugDetailApi {
    @GET("/api/detailPage")
    suspend fun getDrugDetail(@Query("id") id: Long): DetailDrugResponse
}

interface DrugDetailRepository {
    suspend fun getDetail(id: Long): DetailDrugData
}

class DrugDetailRepositoryImpl(
    private val api: DrugDetailApi
) : DrugDetailRepository {
    override suspend fun getDetail(id: Long): DetailDrugData {
        return api.getDrugDetail(id).data
    }
}

/**
 * 복약 등록 API
 *
 */
interface DosageRegisterApi {
    @POST("api/auth/taking-pill")
    suspend fun registerDosage(
        @Body request: RegisterDosageRequest
    ): RegisterDosageResponse
}

interface DosageRegisterRepository {
    suspend fun registerDosage(request: RegisterDosageRequest): RegisterDosageResponse
}

class DosageRegisterRepositoryImpl(
    private val api: DosageRegisterApi
) : DosageRegisterRepository {
    override suspend fun registerDosage(request: RegisterDosageRequest): RegisterDosageResponse {
        return api.registerDosage(request)
    }
}

/**
 * 복약 리스트 불러오기 API
 */

interface DosageSummaryApi {
    @GET("/api/auth/taking-pill")
    suspend fun getDosageSummary(): TakingPillSummaryResponse
}

interface DosageSummaryRepository {
    suspend fun getDosageSummary(): List<TakingPillSummary>
}

class DosageSummaryRepositoryImpl(
    private val api: DosageSummaryApi
) : DosageSummaryRepository {
    override suspend fun getDosageSummary(): List<TakingPillSummary> {
        return api.getDosageSummary().data.takingPills
    }
}

/**
 * 복약 데이터 삭제 API
 */

interface DosageDeleteApi {
    @DELETE("/api/auth/taking-pill/{medicationId}")
    suspend fun deleteTakingPill(
        @Path("medicationId") medicationId: Long
    ): TakingPillSummaryResponse
}

interface DosageDeleteRepository {
    suspend fun deleteTakingPill(medicationId: Long): List<TakingPillSummary>
}

class DosageDeleteRepositoryImpl(
    private val api: DosageDeleteApi
) : DosageDeleteRepository {
    override suspend fun deleteTakingPill(medicationId: Long): List<TakingPillSummary> {
        return api.deleteTakingPill(medicationId).data.takingPills
    }
}
/**
 * 복약 세부 데이터 불러오기 API
 */

interface DosageDetailApi {
    @GET("/api/auth/taking-pill/{medicationId}")
    suspend fun getDosageDetail(
        @Path("medicationId") medicationId: Long
    ): TakingPillDetailResponse
}

interface DosageDetailRepository {
    suspend fun getDosageDetail(medicationId: Long): TakingPillDetailData
}

class DosageDetailRepositoryImpl(
    private val api: DosageDetailApi
) : DosageDetailRepository {
    override suspend fun getDosageDetail(medicationId: Long): TakingPillDetailData {
        return api.getDosageDetail(medicationId).data
    }
}

/**
 * 복약 데이터 수정 API
 */
interface DosageModifyApi {
    @PUT("/api/auth/taking-pill/{medicationId}")
    suspend fun updateDosage(
        @Path("medicationId") medicationId: Long,
        @Body request: RegisterDosageRequest
    ): TakingPillSummaryResponse
}

interface DosageModifyRepository {
    suspend fun updateDosage(medicationId: Long, request: RegisterDosageRequest): List<TakingPillSummary>
}

class DosageModifyRepositoryImpl(
    private val api: DosageModifyApi
) : DosageModifyRepository {
    override suspend fun updateDosage(medicationId: Long, request: RegisterDosageRequest): List<TakingPillSummary> {
        return api.updateDosage(medicationId, request).data.takingPills
    }
}
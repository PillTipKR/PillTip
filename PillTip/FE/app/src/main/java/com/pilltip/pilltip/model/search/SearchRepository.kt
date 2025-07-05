package com.pilltip.pilltip.model.search

import retrofit2.Response
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
    @POST("api/taking-pill")
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
    @GET("/api/taking-pill")
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
    @DELETE("/api/taking-pill/{medicationId}")
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
    @GET("/api/taking-pill/{medicationId}")
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
    @PUT("/api/taking-pill/{medicationId}")
    suspend fun updateDosage(
        @Path("medicationId") medicationId: Long,
        @Body request: RegisterDosageRequest
    ): TakingPillSummaryResponse
}

interface DosageModifyRepository {
    suspend fun updateDosage(
        medicationId: Long,
        request: RegisterDosageRequest
    ): List<TakingPillSummary>
}

class DosageModifyRepositoryImpl(
    private val api: DosageModifyApi
) : DosageModifyRepository {
    override suspend fun updateDosage(
        medicationId: Long,
        request: RegisterDosageRequest
    ): List<TakingPillSummary> {
        return api.updateDosage(medicationId, request).data.takingPills
    }
}

/**
 * 문진표 API
 */

interface QuestionnaireApi {
    @POST("/api/questionnaire")
    suspend fun submitQuestionnaire(
        @Body request: QuestionnaireSubmitRequest
    ): QuestionnaireResponse

    @GET("/api/questionnaire/list")
    suspend fun getQuestionnaireList(): QuestionnaireListResponse

    @GET("/api/questionnaire/{id}")
    suspend fun getQuestionnaireDetail(
        @Path("id") id: Long
    ): QuestionnaireDetailResponse

    @PUT("/api/questionnaire/{id}")
    suspend fun updateQuestionnaire(
        @Path("id") id: Long,
        @Body request: QuestionnaireSubmitRequest
    ): QuestionnaireResponse

    @DELETE("/api/questionnaire/{questionnaire_id}")
    suspend fun deleteQuestionnaire(
        @Path("questionnaire_id") id: Long
    ): QuestionnaireListResponse
}

interface QuestionnaireRepository {
    suspend fun submit(request: QuestionnaireSubmitRequest): QuestionnaireData
    suspend fun getList(): List<QuestionnaireSummary>
    suspend fun getDetail(id: Long): QuestionnaireDetail
    suspend fun update(id: Long, request: QuestionnaireSubmitRequest): QuestionnaireData
    suspend fun delete(id: Long)
}

class QuestionnaireRepositoryImpl(
    private val api: QuestionnaireApi
) : QuestionnaireRepository {
    override suspend fun submit(request: QuestionnaireSubmitRequest): QuestionnaireData {
        return api.submitQuestionnaire(request).data
    }

    override suspend fun getList(): List<QuestionnaireSummary> {
        return api.getQuestionnaireList().data
    }

    override suspend fun getDetail(id: Long): QuestionnaireDetail {
        return api.getQuestionnaireDetail(id).data
    }

    override suspend fun update(id: Long, request: QuestionnaireSubmitRequest): QuestionnaireData {
        return api.updateQuestionnaire(id, request).data
    }

    override suspend fun delete(id: Long) {
        api.deleteQuestionnaire(id)
    }
}

/**
 * DUR 기능
 */

interface DurGptApi {
    @GET("/api/dur/gpt")
    suspend fun getDurGptResult(
        @Query("drugId1") drugId1: Long,
        @Query("drugId2") drugId2: Long
    ): DurGptResponse
}

interface DurGptRepository {
    suspend fun getDurResult(drugId1: Long, drugId2: Long): DurGptData
}

class DurGptRepositoryImpl(
    private val api: DurGptApi
) : DurGptRepository {
    override suspend fun getDurResult(drugId1: Long, drugId2: Long): DurGptData {
        return api.getDurGptResult(drugId1, drugId2).data
    }
}

/**
 * FCM 토큰
 */
//SearcgRepository.kt
interface FcmApi {
    @POST("/api/token")
    suspend fun sendFcmToken(
        @Query("token") token: String
    ): FcmTokenResponse
}

interface FcmTokenRepository {
    suspend fun sendToken(token: String): FcmTokenResponse
}

class FcmTokenRepositoryImpl(
    private val api: FcmApi
) : FcmTokenRepository {
    override suspend fun sendToken(token: String): FcmTokenResponse {
        return api.sendFcmToken(token)
    }
}

/**
 * 민감정보 동의
 */
interface PermissionApi {
    @PUT("/api/questionnaire/permissions/multi")
    suspend fun updatePermissions(
        @Body request: PermissionRequest
    ): PermissionResponse

    @GET("/api/questionnaire/permissions")
    suspend fun getPermissions(): PermissionResponse
}

interface PermissionRepository {
    suspend fun updatePermissions(request: PermissionRequest): PermissionResponse
    suspend fun getPermissions(): PermissionResponse
}

class PermissionRepositoryImpl(
    private val api: PermissionApi
) : PermissionRepository {
    override suspend fun updatePermissions(request: PermissionRequest): PermissionResponse {
        return api.updatePermissions(request)
    }

    override suspend fun getPermissions(): PermissionResponse {
        return api.getPermissions()
    }
}

/**
 * 민감정보 동의 여부 조회
 */


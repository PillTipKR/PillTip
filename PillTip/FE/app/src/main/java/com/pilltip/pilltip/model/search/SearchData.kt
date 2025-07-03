package com.pilltip.pilltip.model.search
/**
 * 약품 자동검색 API
 * http://164.125.253.20:20022/api/autocomplete/drugs?input=타이레놀&page=0
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
 * 일반 검색 결과 API
 * http://164.125.253.20:20022/api/search/drugs?input=타이레놀&page=0
 *
 * */
data class DrugSearchResponse(
    val data: List<DrugSearchResult>
)

data class DrugSearchResult(
    val id: Long,
    val drugName: String,
    val ingredients: List<Ingredient>,
    val manufacturer: String
)

data class Ingredient(
    val name: String,
    val dose: String,
    val main: Boolean
)

/**
 * 약품 상세 페이지
 * [GET] /api/detailPage?id=(약 ID)
 * */

data class DetailDrugResponse(
    val status: String,
    val message: String?,
    val data: DetailDrugData
)

data class DetailDrugData(
    val id: Long,
    val name: String,
    val manufacturer: String,
    val ingredients: List<Ingredient>,
    val form: String,
    val packaging: String,
    val atcCode: String,
    val tag: String,
    val approvalDate: String,
    val storageDetails: List<StorageDetail>,
    val effect: EffectDetail,
    val usage: EffectDetail,
    val caution: EffectDetail,
    val durTags: List<DurTag>
)

data class StorageDetail(
    val category: String,
    val value: String,
    val active: Boolean
)

data class EffectDetail(
    val Type: String,
    val effect: String
)

data class DurTag(
    val title: String,
    val description: List<DurDto>,
    val isTrue: Boolean
)

data class DurDto(
    val name: String,
    val reason: String,
    val note: String
)

/**
 * 복약 등록
 */
data class RegisterDosageRequest(
    val medicationId: Long,
    val medicationName: String,
    val startDate: String, // yyyy-MM-dd
    val endDate: String,
    val alarmName: String,
    val dosageAmount: Double,
    val daysOfWeek: List<String>,
    val dosageSchedules: List<DosageSchedule>
)

data class DosageSchedule(
    val hour: Int,
    val minute: Int,
    val period: String, // AM or PM
    val alarmOnOff: Boolean,
    val dosageUnit: String
)

data class RegisterDosageResponse(
    val status: String,
    val message: String,
    val data: TakingPillDetailData
)

data class TakingPillDetailData(
    val medicationId: Long,
    val medicationName: String,
    val startDate: String,
    val endDate: String,
    val alarmName: String,
    val daysOfWeek: List<String>,
    val dosageAmount: Double,
    val dosageSchedules: List<DosageScheduleDetail>
)

data class DosageScheduleDetail(
    val hour: Int,
    val minute: Int,
    val period: String,
    val dosageUnit: String,
    val alarmOnOff: Boolean
)

/**
 * 복약 리스트 불러오기
 */

data class TakingPillSummaryResponse(
    val status: String,
    val message: String,
    val data: TakingPillSummaryData
)

data class TakingPillSummaryData(
    val takingPills: List<TakingPillSummary>
)

data class TakingPillSummary(
    val medicationId: Long,
    val medicationName: String,
    val alarmName: String,
    val startDate: String, // yyyy-MM-dd
    val endDate: String,
    val dosageAmount: Double
)

/**
 * 복약 세부 데이터 불러오기
 */

data class TakingPillDetailResponse(
    val status: String,
    val message: String,
    val data: TakingPillDetailData
)

/**
 * 문진표
 */

data class QuestionnaireSubmitRequest(
    val realName: String,
    val address: String,
    val questionnaireName: String,
    val medicationInfo: List<MedicationEntry>,
    val allergyInfo: List<AllergyEntry>,
    val chronicDiseaseInfo: List<ChronicDiseaseEntry>,
    val surgeryHistoryInfo: List<SurgeryHistoryEntry>,
    val notes: String
)

data class MedicationEntry(
    val medicationId: Long,
    val medicationName: String,
    val submitted: Boolean
)

data class AllergyEntry(
    val allergyName: String,
    val submitted: Boolean
)

data class ChronicDiseaseEntry(
    val chronicDiseaseName: String,
    val submitted: Boolean
)

data class SurgeryHistoryEntry(
    val surgeryHistoryName: String,
    val submitted: Boolean
)
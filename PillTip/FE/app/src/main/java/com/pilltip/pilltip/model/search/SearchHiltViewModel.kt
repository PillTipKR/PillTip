package com.pilltip.pilltip.model.search

import android.content.Context
import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pilltip.pilltip.model.AuthInterceptor
import com.pilltip.pilltip.model.UserInfoManager
import com.pilltip.pilltip.model.signUp.NetworkModule
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@HiltViewModel
class SearchHiltViewModel @Inject constructor(
    private val repository: AutoCompleteRepository,
    private val drugSearchRepo: DrugSearchRepository,
    private val drugDetailRepo: DrugDetailRepository,
    private val dosageRegisterRepo: DosageRegisterRepository,
    private val dosageSummaryRepo: DosageSummaryRepository,
    private val dosageDetailRepo: DosageDetailRepository,
    private val dosageDeleteRepo: DosageDeleteRepository,
    private val dosageModifyRepo: DosageModifyRepository,
    private val fcmRepo: FcmTokenRepository,
    private val durGptRepo: DurGptRepository,
    private val sensitiveInfoRepo: SensitiveInfoRepository,
    private val dosageLogRepo: DosageLogRepository
) : ViewModel() {

    /* 약품명 자동 완성 API*/

    private val _autoComplete = MutableStateFlow<List<SearchData>>(emptyList())
    val autoCompleted: StateFlow<List<SearchData>> = _autoComplete.asStateFlow()

    private val _isAutoCompleteLoading = MutableStateFlow(false)
    val isAutoCompleteLoading: StateFlow<Boolean> = _isAutoCompleteLoading.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private var currentPage = 0
    private var currentQuery = ""

    fun fetchAutoComplete(query: String, reset: Boolean = false) {
        if (_isAutoCompleteLoading.value) return
        viewModelScope.launch {
            _isAutoCompleteLoading.value = true
            try {
                if (reset || query != currentQuery) {
                    currentPage = 0
                    currentQuery = query
                    _autoComplete.value = emptyList()
                }
                val newResults = try {
                    repository.getAutoComplete(currentQuery, currentPage)
                } catch (e: Exception) {
                    Log.e("AutoComplete", "자동완성 API 호출 실패: ${e.message}")
                    emptyList()
                }

                if (newResults.isNotEmpty()) {
                    _autoComplete.value += newResults
                    currentPage++
                }
            } finally {
                _isAutoCompleteLoading.value = false
            }
        }
    }

    /* 약품명 일반 검색 API*/

    private val _drugSearchResults = MutableStateFlow<List<DrugSearchResult>>(emptyList())
    val drugSearchResults: StateFlow<List<DrugSearchResult>> = _drugSearchResults.asStateFlow()

    fun fetchDrugSearch(query: String, reset: Boolean = true) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val results = drugSearchRepo.search(query, page = 0)
                _drugSearchResults.value = results
            } catch (e: Exception) {
                Log.e("DrugSearch", "일반 검색 API 호출 실패: ${e.message}")
                _drugSearchResults.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    /* 약품 상세 페이지 API */
    private val _drugDetail = MutableStateFlow<DetailDrugData?>(null)
    val drugDetail: StateFlow<DetailDrugData?> = _drugDetail.asStateFlow()

    fun fetchDrugDetail(id: Long) {
        Log.d("DrugDetail", "Fetching detail for drug ID: $id")
        viewModelScope.launch {
            try {
                _isLoading.value = true
//                val detail = drugDetailRepo.getDetail(id)
//                Log.d("DrugDetail", "Fetched drug detail: $detail")
                _drugDetail.value = drugDetailRepo.getDetail(id)
            } catch (e: Exception) {
                Log.e("DrugDetail", "상세정보 API 실패: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    /* 복약 등록 API */
    private val _registerResult = MutableStateFlow<RegisterDosageResponse?>(null)
    val registerResult: StateFlow<RegisterDosageResponse?> = _registerResult.asStateFlow()

    // 단일 복약 상세 객체 저장
    private val _pillDetail = MutableStateFlow<TakingPillDetailData?>(null)
    val pillDetail: StateFlow<TakingPillDetailData?> = _pillDetail.asStateFlow()

    fun registerDosage(request: RegisterDosageRequest) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = dosageRegisterRepo.registerDosage(request)
                _registerResult.value = response
                Log.d("DosageRegister", "등록 완료된 복약 정보: ${response.data}")
            } catch (e: Exception) {
                Log.e("DosageRegister", "복약 등록 실패: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    /* 복약 리스트 불러오기 */
    private val _pillSummaryList = MutableStateFlow<List<TakingPillSummary>>(emptyList())
    val pillSummaryList: StateFlow<List<TakingPillSummary>> = _pillSummaryList.asStateFlow()

    fun fetchDosageSummary() {
        viewModelScope.launch {
            try {
                val list = dosageSummaryRepo.getDosageSummary()
                _pillSummaryList.value = list
                Log.d("API Response", _pillSummaryList.value.toString())
            } catch (e: Exception) {
                Log.e("DosageSummary", "불러오기 실패: ${e.message}")
            }
        }
    }

    /* 복약 등록 삭제 */
    fun deletePill(medicationId: Long) {
        viewModelScope.launch {
            try {
                _pillSummaryList.value = dosageDeleteRepo.deleteTakingPill(medicationId)
            } catch (e: Exception) {
                Log.e("DosageDelete", "삭제 실패: ${e.message}")
            }
        }
    }

    /* 복약 세부 데이터 */
    fun fetchTakingPillDetail(medicationId: Long) {
        viewModelScope.launch {
            try {
                _pillDetail.value = dosageDetailRepo.getDosageDetail(medicationId)
            } catch (e: Exception) {
                _pillDetail.value = null
                Log.e("DosageDetail", "상세 조회 실패: ${e.message}")
            }
        }
    }

    /* 복약 데이터 수정 */
    fun modifyDosage(medicationId: Long, request: RegisterDosageRequest) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val updatedList = dosageModifyRepo.updateDosage(medicationId, request)
                _pillSummaryList.value = updatedList
                Log.d("DosageModify", "수정 완료. 복약 목록 업데이트됨.")
            } catch (e: Exception) {
                Log.e("DosageModify", "수정 실패: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }
    fun clearPillDetail() {
        _pillDetail.value = null
    }

    var pendingDosageRequest by mutableStateOf<RegisterDosageRequest?>(null)

    fun setPendingRequest(request: RegisterDosageRequest) {
        pendingDosageRequest = request
    }

    fun clearPendingRequest() {
        pendingDosageRequest = null
    }

    /* DUR 기능 */
    private val _durGptResult = MutableStateFlow<DurGptData?>(null)
    val durGptResult: StateFlow<DurGptData?> = _durGptResult.asStateFlow()

    private val _isDurGptLoading = MutableStateFlow(false)
    val isDurGptLoading: StateFlow<Boolean> = _isDurGptLoading.asStateFlow()

    fun fetchDurAi(drugId1: Long, drugId2: Long) {
        viewModelScope.launch {
            _isDurGptLoading.value = true
            try {
                val result = durGptRepo.getDurResult(drugId1, drugId2)
                _durGptResult.value = result
                Log.d("DurGpt", "결과: $result")
            } catch (e: Exception) {
                Log.e("DurGpt", "에러: ${e.message}")
                _durGptResult.value = null
            } finally {
                _isDurGptLoading.value = false
            }
        }
    }

    /* FCM 토큰 */
    fun sendFcmToken(token: String) {
        viewModelScope.launch {
            try {
                val result = fcmRepo.sendToken(token)
                Log.d("FCM", "서버 응답: ${result.status} - ${result.message}")
            } catch (e: Exception) {
                Log.e("FCM", "토큰 전송 실패: ${e.message}")
            }
        }
    }

    /* 건강정보 조회 */
    private val _sensitiveInfo = MutableStateFlow<SensitiveInfoData?>(null)
    val sensitiveInfo: StateFlow<SensitiveInfoData?> = _sensitiveInfo.asStateFlow()

    fun fetchSensitiveInfo() {
        viewModelScope.launch {
            try {
                _sensitiveInfo.value = sensitiveInfoRepo.fetchSensitiveInfo()
            } catch (e: Exception) {
                Log.e("SensitiveInfo", "조회 실패: ${e.message}")
            }
        }
    }

    /* 복약 알림 */
    private val _dailyDosageLog = MutableStateFlow<DailyDosageLogData?>(null)
    val dailyDosageLog: StateFlow<DailyDosageLogData?> = _dailyDosageLog.asStateFlow()
    var selectedDrugLog by mutableStateOf<DosageLogPerDrug?>(null)

    fun fetchDailyDosageLog(date: LocalDate) {
        viewModelScope.launch {
            try {
                val response = dosageLogRepo.getDailyDosageLog(date.toString())
                _dailyDosageLog.value = response.data
                Log.d("DailyDosageLog", "성공: ${response.data}")
            } catch (e: Exception) {
                Log.e("DailyDosageLog", "실패: ${e.message}")
                _dailyDosageLog.value = null
            }
        }
    }

    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate: StateFlow<LocalDate> = _selectedDate

    fun updateSelectedDate(date: LocalDate) {
        _selectedDate.value = date
        fetchDailyDosageLog(date)
    }

    fun toggleDosageTaken(
        logId: Long,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val response = dosageLogRepo.toggleDosageTaken(logId)
                if (response.status == "success") {
                    onSuccess(response.data)

                    val latest = dosageLogRepo.getDailyDosageLog(_selectedDate.value.toString()).data
                    _dailyDosageLog.value = latest

                    selectedDrugLog?.let { selected ->
                        val updated = latest.perDrugLogs.find { it.medicationName == selected.medicationName }
                        selectedDrugLog = updated
                    }
                } else {
                    onError(response.message ?: "실패했습니다.")
                }
            } catch (e: Exception) {
                onError(e.localizedMessage ?: "에러가 발생했습니다.")
            }
        }
    }

    fun updateSelectedDrugLog(updatedDrug: DosageLogPerDrug?) {
        selectedDrugLog = updatedDrug
    }

    fun fetchDosageLogMessage(
        logId: Long,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val response = dosageLogRepo.getDosageLogMessage(logId)
                if (response.status == "success") {
                    onSuccess(response.data)
                } else {
                    onError(response.message ?: "서버 응답이 실패했어요.")
                }
            } catch (e: Exception) {
                onError(e.localizedMessage ?: "에러가 발생했어요.")
            }
        }
    }
}

@HiltViewModel
class QuestionnaireViewModel @Inject constructor(
    private val repository: QuestionnaireRepository,
    private val permissionRepository: PermissionRepository
) : ViewModel() {

    var realName by mutableStateOf("")
    var address by mutableStateOf("")
    var phoneNumber by mutableStateOf("")
    var questionnaireName by mutableStateOf("")
    var notes by mutableStateOf("")

    var medicationInfo by mutableStateOf<List<MedicationEntry>>(emptyList())
    var allergyInfo by mutableStateOf<List<AllergyEntry>>(emptyList())
    var chronicDiseaseInfo by mutableStateOf<List<ChronicDiseaseEntry>>(emptyList())
    var surgeryHistoryInfo by mutableStateOf<List<SurgeryHistoryEntry>>(emptyList())

    var submittedQuestionnaire by mutableStateOf<QuestionnaireData?>(null)

    var permissionState by mutableStateOf<PermissionData?>(null)
    var isPermissionLoading by mutableStateOf(false)

    fun updateSensitivePermissions(
        sensitiveInfo: Boolean,
        medicalInfo: Boolean
    ) {
        viewModelScope.launch {
            isPermissionLoading = true
            try {
                val request = PermissionRequest(
                    sensitiveInfoPermission = sensitiveInfo,
                    medicalInfoPermission = medicalInfo
                )
                val response = permissionRepository.updatePermissions(request)
                permissionState = response.data
                Log.d("PermissionUpdate", "민감정보 동의 성공: ${response.message}")
            } catch (e: Exception) {
                Log.e("PermissionUpdate", "민감정보 동의 실패: ${e.message}")
            } finally {
                isPermissionLoading = false
            }
        }
    }

    private val _permissionUpdateResult = MutableStateFlow<PermissionData?>(null)
    val permissionUpdateResult: StateFlow<PermissionData?> = _permissionUpdateResult.asStateFlow()

    fun updateSinglePermission(permissionType: String, granted: Boolean) {
        viewModelScope.launch {
            try {
                val response = permissionRepository.updateSinglePermission(permissionType, granted)
                _permissionUpdateResult.value = response.data
                Log.d("Permission", "업데이트 완료: $permissionType = $granted")
            } catch (e: Exception) {
                Log.e("Permission", "업데이트 실패: ${e.message}")
            }
        }
    }

    fun loadPermissions() {
        viewModelScope.launch {
            isPermissionLoading = true
            try {
                val response = permissionRepository.getPermissions()
                permissionState = response.data
                Log.d("PermissionLoad", "현재 권한 상태: ${response.data}")
            } catch (e: Exception) {
                Log.e("PermissionLoad", "권한 불러오기 실패: ${e.message}")
            } finally {
                isPermissionLoading = false
            }
        }
    }

    fun resetAll() {
        realName = ""
        address = ""
        questionnaireName = ""
        notes = ""
        medicationInfo = emptyList()
        allergyInfo = emptyList()
        chronicDiseaseInfo = emptyList()
        surgeryHistoryInfo = emptyList()
    }

    fun toRequest(): QuestionnaireSubmitRequest {
        return QuestionnaireSubmitRequest(
            realName = realName,
            address = address,
            phoneNumber = phoneNumber,
            questionnaireName = questionnaireName,
            medicationInfo = medicationInfo,
            allergyInfo = allergyInfo,
            chronicDiseaseInfo = chronicDiseaseInfo,
            surgeryHistoryInfo = surgeryHistoryInfo,
            notes = notes
        )
    }

    fun submit() {
        viewModelScope.launch {
            try {
                val response = repository.submit(toRequest())
                submittedQuestionnaire = response
                Log.d("문진표", "제출 성공: ID=${response.questionnaireId}")
            } catch (e: Exception) {
                Log.e("문진표", "제출 실패: ${e.message}")
            }
        }
    }

    var questionnaireList by mutableStateOf<List<QuestionnaireSummary>>(emptyList())
        private set

    var isListLoading by mutableStateOf(false)

    fun fetchQuestionnaireList() {
        viewModelScope.launch {
            isListLoading = true
            try {
                questionnaireList = repository.getList()
                Log.d("문진표", "리스트 불러오기 성공: ${questionnaireList.size}개")
            } catch (e: Exception) {
                Log.e("문진표", "리스트 불러오기 실패: ${e.message}")
            } finally {
                isListLoading = false
            }
        }
    }

    var questionnaireDetail by mutableStateOf<QuestionnaireDetail?>(null)
        private set

    var isLoading by mutableStateOf(false)
        private set

    fun fetchQuestionnaireDetail(id: Long) {
        viewModelScope.launch {
            isLoading = true
            try {
                val result = repository.getDetail(id)
                questionnaireDetail = result
                Log.d("문진표", "상세 조회 성공: $result")
            } catch (e: Exception) {
                Log.e("문진표", "상세 조회 실패: ${e.message}")
            } finally {
                isLoading = false
            }
        }
    }

    fun loadQuestionnaireDetail(
        data: QuestionnaireDetail,
        realName: String,
        address: String,
        phoneNumber: String
    ) {
        questionnaireName = data.questionnaireName
        this.realName = realName
        this.address = address
        this.phoneNumber = phoneNumber

        medicationInfo = data.medicationInfo.toMutableStateList()
        allergyInfo = data.allergyInfo.toMutableStateList()
        chronicDiseaseInfo = data.chronicDiseaseInfo.toMutableStateList()
        surgeryHistoryInfo = data.surgeryHistoryInfo.toMutableStateList()
    }

    fun modify() {
        viewModelScope.launch {
            try {
                val id = questionnaireDetail?.questionnaireId ?: return@launch
                val request = toRequest()
                repository.update(id, request)
                Log.d("문진표", "수정 성공: ID=$id")
            } catch (e: Exception) {
                Log.e("문진표", "수정 실패: ${e.message}")
            }
        }
    }

    fun delete(id: Long, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                repository.delete(id)
                Log.d("문진표", "삭제 성공: $id")
                onSuccess()
            } catch (e: Exception) {
                Log.e("문진표", "삭제 실패: ${e.message}")
                onError(e.message ?: "알 수 없는 오류")
            }
        }
    }
}

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {
    @Provides
    @Singleton
    fun provideOkHttpClient(
        @ApplicationContext context: Context
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(context))
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .build()
    }

    @Provides
    @Singleton
    @Named("SearchRetrofit")
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://164.125.253.20:20022")
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build()
    }

    @Provides
    fun provideAutoCompleteApi(@Named("SearchRetrofit") retrofit: Retrofit): AutoCompleteApi {
        return retrofit.create(AutoCompleteApi::class.java)
    }

    @Provides
    fun provideAutoCompleteRepository(api: AutoCompleteApi): AutoCompleteRepository {
        return AutoCompleteRepositoryImpl(api)
    }

    @Provides
    fun provideDrugSearchApi(@Named("SearchRetrofit") retrofit: Retrofit): DrugSearchApi {
        return retrofit.create(DrugSearchApi::class.java)
    }

    @Provides
    fun provideDrugSearchRepository(api: DrugSearchApi): DrugSearchRepository {
        return DrugSearchRepositoryImpl(api)
    }

    @Provides
    fun provideDrugDetailApi(@Named("SearchRetrofit") retrofit: Retrofit): DrugDetailApi {
        return retrofit.create(DrugDetailApi::class.java)
    }

    @Provides
    fun provideDrugDetailRepository(api: DrugDetailApi): DrugDetailRepository {
        return DrugDetailRepositoryImpl(api)
    }

    @Provides
    fun provideDosageRegisterApi(@Named("SearchRetrofit") retrofit: Retrofit): DosageRegisterApi {
        return retrofit.create(DosageRegisterApi::class.java)
    }

    @Provides
    fun provideDosageRegisterRepository(api: DosageRegisterApi): DosageRegisterRepository {
        return DosageRegisterRepositoryImpl(api)
    }

    @Provides
    fun provideDosageSummaryApi(@Named("SearchRetrofit") retrofit: Retrofit): DosageSummaryApi {
        return retrofit.create(DosageSummaryApi::class.java)
    }

    @Provides
    fun provideDosageSummaryRepository(api: DosageSummaryApi): DosageSummaryRepository {
        return DosageSummaryRepositoryImpl(api)
    }

    @Provides
    fun provideDosageDeleteApi(@Named("SearchRetrofit") retrofit: Retrofit): DosageDeleteApi {
        return retrofit.create(DosageDeleteApi::class.java)
    }

    @Provides
    fun provideDosageDeleteRepository(api: DosageDeleteApi): DosageDeleteRepository {
        return DosageDeleteRepositoryImpl(api)
    }

    @Provides
    fun provideDosageDetailApi(@Named("SearchRetrofit") retrofit: Retrofit): DosageDetailApi =
        retrofit.create(DosageDetailApi::class.java)

    @Provides
    fun provideDosageDetailRepository(api: DosageDetailApi): DosageDetailRepository =
        DosageDetailRepositoryImpl(api)

    @Provides
    fun provideDosageModifyApi(@Named("SearchRetrofit") retrofit: Retrofit): DosageModifyApi {
        return retrofit.create(DosageModifyApi::class.java)
    }

    @Provides
    fun provideDosageModifyRepository(api: DosageModifyApi): DosageModifyRepository {
        return DosageModifyRepositoryImpl(api)
    }

    @Provides
    fun provideQuestionnaireApi(@Named("SearchRetrofit") retrofit: Retrofit): QuestionnaireApi =
        retrofit.create(QuestionnaireApi::class.java)

    @Provides
    fun provideQuestionnaireRepository(api: QuestionnaireApi): QuestionnaireRepository =
        QuestionnaireRepositoryImpl(api)

    @Provides
    fun provideFcmApi(@Named("SearchRetrofit") retrofit: Retrofit): FcmApi {
        return retrofit.create(FcmApi::class.java)
    }

    @Provides
    fun provideFcmTokenRepository(api: FcmApi): FcmTokenRepository {
        return FcmTokenRepositoryImpl(api)
    }

    @Provides
    fun providePermissionApi(@Named("SearchRetrofit") retrofit: Retrofit): PermissionApi {
        return retrofit.create(PermissionApi::class.java)
    }

    @Provides
    fun providePermissionRepository(api: PermissionApi): PermissionRepository {
        return PermissionRepositoryImpl(api)
    }

    @Provides
    fun provideDurGptApi(@Named("SearchRetrofit") retrofit: Retrofit): DurGptApi {
        return retrofit.create(DurGptApi::class.java)
    }

    @Provides
    fun provideDurGptRepository(api: DurGptApi): DurGptRepository {
        return DurGptRepositoryImpl(api)
    }

    @Provides
    fun provideSensitiveInfoApi(@Named("SearchRetrofit") retrofit: Retrofit): SensitiveInfoApi {
        return retrofit.create(SensitiveInfoApi::class.java)
    }

    @Provides
    fun provideSensitiveInfoRepository(api: SensitiveInfoApi): SensitiveInfoRepository {
        return SensitiveInfoRepositoryImpl(api)
    }

    @Provides
    fun provideDosageLogApi(@Named("SearchRetrofit") retrofit: Retrofit): DosageLogApi {
        return retrofit.create(DosageLogApi::class.java)
    }

    @Provides
    fun provideDosageLogRepository(api: DosageLogApi): DosageLogRepository {
        return DosageLogRepositoryImpl(api)
    }
}
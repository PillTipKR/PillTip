package com.pilltip.pilltip.model.search

import android.content.Context
import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pilltip.pilltip.model.AuthInterceptor
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
    private val dosageModifyRepo: DosageModifyRepository

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
            .baseUrl("http://164.125.253.20:20022")
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
}
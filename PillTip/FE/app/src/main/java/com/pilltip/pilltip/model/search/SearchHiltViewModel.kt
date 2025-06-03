package com.pilltip.pilltip.model.search

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pilltip.pilltip.model.AuthInterceptor
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
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@HiltViewModel
class SearchHiltViewModel @Inject constructor(
    private val repository: AutoCompleteRepository,
    private val drugSearchRepo: DrugSearchRepository
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
                try {
                    val newResults = repository.getAutoComplete(currentQuery, currentPage)
                    _autoComplete.value = _autoComplete.value + newResults
                    currentPage++
                } catch (e: Exception) {
                    Log.e("AutoComplete", "자동완성 API 호출 실패: ${e.message}")
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
            .addInterceptor(AuthInterceptor(context)) // AccessToken 자동 주입
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
}
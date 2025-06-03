package com.pilltip.pilltip.model.search

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Inject

@HiltViewModel
class AutoCompleteHiltViewModel @Inject constructor(
    private val repository: AutoCompleteRepository
) : ViewModel() {

    private val _autoComplete = MutableStateFlow<List<SearchData>>(emptyList())
    val autoCompleted: StateFlow<List<SearchData>> = _autoComplete.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private var currentPage = 0
    private var currentQuery = ""

    fun fetchAutoComplete(query: String, reset: Boolean = false) {
        if (_isLoading.value) return
        viewModelScope.launch {
            _isLoading.value = true
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
                _isLoading.value = false
            }
        }
    }
}

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {
    @Provides
    fun provideAutoCompleteApi(): AutoCompleteApi {
        return Retrofit.Builder()
            .baseUrl("http://164.125.253.20:20022")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(AutoCompleteApi::class.java)
    }

    @Provides
    fun provideAutoCompleteRepository(api: AutoCompleteApi): AutoCompleteRepository {
        return AutoCompleteRepositoryImpl(api)
    }
}
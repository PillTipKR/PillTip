package com.pilltip.pilltip.model.signUp

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class SignUpViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _signUpData = mutableStateOf(SignUpData())
    val signUpData: State<SignUpData> = _signUpData

    fun updateLoginType(type: LoginType) {
        _signUpData.value = _signUpData.value.copy(loginType = type)
    }

    fun updateUserId(id: String) {
        _signUpData.value = _signUpData.value.copy(userId = id)
    }

    fun updatePassword(password: String) {
        _signUpData.value = _signUpData.value.copy(password = password)
    }

    fun updateTermsOfServices(agreed: Boolean){
        _signUpData.value = _signUpData.value.copy(term = agreed)
    }

    fun updateNickname(nickname: String) {
        _signUpData.value = _signUpData.value.copy(nickname = nickname)
    }

    fun updateGender(gender: String) {
        _signUpData.value = _signUpData.value.copy(gender = gender)
    }

    fun updateBirthDate(year: Int, month: Int, day: Int) {
        val dateStr = "%04d-%02d-%02d".format(year, month, day)
        val calculatedAge = calculateAge(year, month, day)

        _signUpData.value = _signUpData.value.copy(
            birthDate = dateStr,
            age = calculatedAge
        )
    }

    fun updateHeight(height: Int) {
        _signUpData.value = _signUpData.value.copy(height = height)
    }

    fun updateWeight(weight: Int) {
        _signUpData.value = _signUpData.value.copy(weight = weight)
    }

    fun updateInterest(interest: String) {
        _signUpData.value = _signUpData.value.copy(interest = interest)
    }

    fun updatePhone(phone: String) {
        _signUpData.value = _signUpData.value.copy(phone = phone)
    }

    fun updateToken(token: String) {
        _signUpData.value = _signUpData.value.copy(token = token)
    }

    // 최종 회원가입 요청
    fun completeSignUp(
        onSuccess: () -> Unit,
        onFailure: (Throwable?) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val result = authRepository.sendSignUp(_signUpData.value)
                if (result) {
                    onSuccess()
                } else {
                    onFailure(null)
                }
            } catch (e: Exception) {
                onFailure(e)
            }
        }
    }

    private fun calculateAge(year: Int, month: Int, day: Int): Int {
        val today = LocalDate.now()
        val birthDate = LocalDate.of(year, month, day)
        var age = today.year - birthDate.year

        if (today < birthDate.plusYears(age.toLong()))
            age--

        return age
    }
}
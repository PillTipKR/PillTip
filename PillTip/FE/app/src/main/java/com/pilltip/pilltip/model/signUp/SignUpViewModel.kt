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

    /*값 업데이트*/
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

    /*값 읽기*/

    // 로그인 타입
    fun getLoginType(): LoginType = _signUpData.value.loginType

    // 사용자 ID
    fun getUserId(): String = _signUpData.value.userId

    // 비밀번호
    fun getPassword(): String = _signUpData.value.password

    // 닉네임
    fun getNickname(): String = _signUpData.value.nickname

    // 성별
    fun getGender(): String = _signUpData.value.gender

    // 생년월일
    fun getBirthDate(): String = _signUpData.value.birthDate

    // 나이
    fun getAge(): Int = _signUpData.value.age

    // 키
    fun getHeight(): Int = _signUpData.value.height

    // 몸무게
    fun getWeight(): Int = _signUpData.value.weight

    // 관심사
    fun getInterest(): String = _signUpData.value.interest

    // 전화번호
    fun getPhone(): String = _signUpData.value.phone

    // 토큰
    fun getToken(): String = _signUpData.value.token

    // 약관 동의 여부
    fun isAgreedToTerms(): Boolean = _signUpData.value.term

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
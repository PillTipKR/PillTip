package com.pilltip.pilltip.model.signUp

import android.app.Activity
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class SignUpViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _signUpData = mutableStateOf(SignUpData())
    val signUpData: State<SignUpData> = _signUpData


    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    private val _accessToken = mutableStateOf("")
    val accessToken: State<String> = _accessToken

    private val _refreshToken = mutableStateOf("")
    val refreshToken: State<String> = _refreshToken

    /*값 업데이트*/
    fun updateLoginType(type: LoginType) {
        _signUpData.value = _signUpData.value.copy(loginType = type)
    }

    fun updateloginId(id: String) {
        _signUpData.value = _signUpData.value.copy(loginId = id)
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
    fun getloginId(): String = _signUpData.value.loginId

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

    fun logSignUpData(tag: String = "SignUpData") {
        val data = _signUpData.value
        Log.d(tag, """
        - loginType: ${data.loginType}
        - userId: ${data.loginId}
        - password: ${data.password}
        - term: ${data.term}
        - nickname: ${data.nickname}
        - gender: ${data.gender}
        - birthDate: ${data.birthDate}
        - age: ${data.age}
        - height: ${data.height}
        - weight: ${data.weight}
        - interest: ${data.interest}
        - phone: ${data.phone}
        - token: ${data.token}
    """.trimIndent())
    }

    fun completeSignUp(
        onSuccess: (accessToken: String, refreshToken: String) -> Unit,
        onFailure: (Throwable?) -> Unit
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val (result, tokenData) = authRepository.sendSignUp(_signUpData.value)
                if (result && tokenData != null) {
                    _accessToken.value = tokenData.accessToken
                    _refreshToken.value = tokenData.refreshToken
                    onSuccess(tokenData.accessToken, tokenData.refreshToken)
                } else {
                    onFailure(null)
                }
            } catch (e: Exception) {
                onFailure(e)
            } finally {
                _isLoading.value = false
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

    fun submitTerms(
        token: String,
        onSuccess: () -> Unit,
        onFailure: (Throwable?) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val success = authRepository.submitTerms(token, _signUpData.value.term)
                if (success) {
                    onSuccess()
                } else {
                    onFailure(null)
                }
            } catch (e: Exception) {
                onFailure(e)
            }
        }
    }
}

@HiltViewModel
class PhoneAuthViewModel @Inject constructor(
    private val phoneAuthManager: PhoneAuthManager
) : ViewModel() {

    private val _timeRemaining = MutableStateFlow(180) // 3분
    val timeRemaining: StateFlow<Int> = _timeRemaining
    private var timerJob: Job? = null

    private val _rawPhone = MutableStateFlow("")
    val rawPhone: StateFlow<String> = _rawPhone

    private val _formattedPhone = MutableStateFlow("")
    val formattedPhone: StateFlow<String> = _formattedPhone

    private val _code = MutableStateFlow("")
    val code: StateFlow<String> = _code

    private val _verificationId = MutableStateFlow<String?>(null)
    val verificationId: StateFlow<String?> = _verificationId

    private val _status = MutableStateFlow("")
    val status: StateFlow<String> = _status

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val _isAutoVerified = MutableStateFlow(false)
    val isAutoVerified: StateFlow<Boolean> = _isAutoVerified

    fun updatePhoneNumber(input: String) {
        val digits = input.filter { it.isDigit() }.take(11)
        _rawPhone.value = when {
            digits.length <= 3 -> digits
            digits.length <= 7 -> "${digits.substring(0, 3)}-${digits.substring(3)}"
            else -> "${digits.substring(0, 3)}-${digits.substring(3, 7)}-${digits.substring(7)}"
        }

        _formattedPhone.value = if (digits.startsWith("0")) "+82${digits.drop(1)}" else digits
    }

    fun updateCode(value: String) {
        _code.value = value
    }

    fun requestVerification(
        activity: Activity,
        onSent: () -> Unit,
        onFailed: (String) -> Unit,
        onAutoVerified: () -> Unit = {}
    ) {
        _status.value = "인증 요청 중..."
        _errorMessage.value = null

        phoneAuthManager.startPhoneNumberVerification(
            activity = activity,
            phoneNumber = _formattedPhone.value,
            onCodeSent = { id, _ ->
                _verificationId.value = id
                _status.value = "코드 전송됨"
                startTimer()
                onSent()
            },
            onVerificationCompleted = {credential, code ->
                val autoCode = code
                if (!autoCode.isNullOrEmpty()) {
                    _code.value = autoCode
                }
                _status.value = "자동 인증 중..."
                FirebaseAuth.getInstance().signInWithCredential(credential)
                    .addOnSuccessListener {
                        _status.value = "자동 인증 성공"
                        _isAutoVerified.value = true
                        onAutoVerified()
                    }
                    .addOnFailureListener { e ->
                        val msg = e.message ?: "자동 인증 실패"
                        _status.value = "실패: $msg"
                        _errorMessage.value = msg
                        onFailed(msg)
                    }
            },
            onVerificationFailed = { e ->
                val msg = e.message ?: "인증 실패"
                _status.value = "실패: $msg"
                _errorMessage.value = msg
                onFailed(msg)
            }
        )
    }

    fun verifyCodeInput(
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        val id = _verificationId.value
        val inputCode = _code.value

        if (id != null && inputCode.isNotEmpty()) {
            _status.value = "코드 인증 중..."
            _errorMessage.value = null

            phoneAuthManager.verifyCode(
                verificationId = id,
                code = inputCode,
                onSuccess = {
                    _status.value = "성공: ${it.phoneNumber}"
                    onSuccess()
                },
                onFailure = { e ->
                    val msg = e.message ?: "코드 인증 실패"
                    _status.value = "실패: $msg"
                    _errorMessage.value = msg
                    onFailure(msg)
                }
            )
        } else {
            _errorMessage.value = "인증 코드 또는 인증 ID가 없습니다."
        }
    }

    fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            for (i in 180 downTo 0) {
                _timeRemaining.value = i
                delay(1000)
            }
        }
    }
}


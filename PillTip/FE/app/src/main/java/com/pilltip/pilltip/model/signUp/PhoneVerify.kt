package com.pilltip.pilltip.model.signUp

import android.app.Activity
import android.util.Log
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class PhoneAuthManager @Inject constructor(
    private val auth: FirebaseAuth
) {
    fun startPhoneNumberVerification(
        activity: Activity,
        phoneNumber: String,
        onCodeSent: (String, PhoneAuthProvider.ForceResendingToken) -> Unit,
        onVerificationCompleted: (PhoneAuthCredential, String?) -> Unit,
        onVerificationFailed: (FirebaseException) -> Unit
    ) {
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(activity)
            .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                    val code = credential.smsCode

                    if (code != null) {
                        Log.d("PhoneAuth", "자동 감지된 인증 코드: $code")
                    }
                    onVerificationCompleted(credential, code)
                }

                override fun onVerificationFailed(e: FirebaseException) {
                    onVerificationFailed(e)
                }

                override fun onCodeSent(
                    verificationId: String,
                    token: PhoneAuthProvider.ForceResendingToken
                ) {
                    onCodeSent(verificationId, token)
                }
            })
            .build()

        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    fun verifyCode(
        verificationId: String,
        code: String,
        onSuccess: (FirebaseUser) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val credential = PhoneAuthProvider.getCredential(verificationId, code)
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = task.result?.user
                    user?.let { onSuccess(it) }
                } else {
                    onFailure(task.exception ?: Exception("인증 실패"))
                }
            }
    }
}

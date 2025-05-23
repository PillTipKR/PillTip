package com.pilltip.pilltip

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.ui.platform.LocalContext
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.google.firebase.FirebaseApp
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import com.google.firebase.auth.FirebaseAuth
import com.kakao.sdk.common.KakaoSdk
import com.kakao.sdk.common.util.Utility
import com.kakao.vectormap.KakaoMapSdk
import com.pilltip.pilltip.nav.NavGraph
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val context = LocalContext.current
            Log.d("KeyHash", "${Utility.getKeyHash(this)}")
            FirebaseApp.initializeApp(context)
            FirebaseAppCheck.getInstance().installAppCheckProviderFactory(
                DebugAppCheckProviderFactory.getInstance()
            )
            FirebaseAuth.getInstance().firebaseAuthSettings // 디버그 모드에서, PlayIntegrity 통과 못할 시 강제 리캡챠로 진행하도록 수정.
                .forceRecaptchaFlowForTesting(true)

            val kakaoKey = BuildConfig.KAKAO_KEY
            Log.d("KakaoKey", kakaoKey)
            KakaoSdk.init(this, kakaoKey)
            KakaoMapSdk.init(this, kakaoKey);
            NavGraph(startPage = "SplashPage")
        }
    }
}


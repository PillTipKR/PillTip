package com.pilltip.pilltip

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
            Log.d("KeyHash", "${Utility.getKeyHash(this)}")

            val kakaoKey = BuildConfig.KAKAO_KEY
            Log.d("KakaoKey", kakaoKey)
            KakaoSdk.init(this, kakaoKey)
            KakaoMapSdk.init(this, kakaoKey);
            NavGraph()
        }
    }
}

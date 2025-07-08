package com.pilltip.pilltip.model.signUp

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.GsonBuilder
import com.pilltip.pilltip.R
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import java.security.KeyStore
import java.security.cert.CertificateFactory
import javax.inject.Named
import javax.inject.Singleton
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager

interface ServerAuthAPI {
    @POST("api/auth/signup")
    suspend fun signUp(@Body request: SignUpRequest): Response<SignUpResponse>

    @POST("api/auth/login")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<LoginResponse>

    @POST("api/auth/social-login")
    suspend fun socialLogin(
        @Body request: SocialLoginRequest
    ): Response<LoginResponse>

    @POST("api/auth/terms")
    suspend fun submitTerms(
        @Header("Authorization") token: String,
    ): Response<AuthMeResponse>

    @GET("api/auth/me")
    suspend fun getMyInfo(
        @Header("Authorization") token: String
    ): Response<AuthMeResponse>
}

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    fun createTrustedOkHttpClient(context: Context): OkHttpClient {
        val certificateFactory = CertificateFactory.getInstance("X.509")
        val input = context.resources.openRawResource(R.raw.server)
        val certificate = certificateFactory.generateCertificate(input)
        input.close()

        val keyStore = KeyStore.getInstance(KeyStore.getDefaultType())
        keyStore.load(null)
        keyStore.setCertificateEntry("server", certificate)

        val trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
        trustManagerFactory.init(keyStore)

        val sslContext = SSLContext.getInstance("TLS")
        sslContext.init(null, trustManagerFactory.trustManagers, null)

        val trustManager = trustManagerFactory.trustManagers[0] as X509TrustManager

        return OkHttpClient.Builder()
            .sslSocketFactory(sslContext.socketFactory, trustManager)
            .hostnameVerifier { _, _ -> true } // 필수: IP 주소 사용 시 hostname mismatch 회피
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .build()
    }

    @Provides
    @Singleton
    @Named("CertClient")
    fun provideOkHttpClient(@ApplicationContext context: Context): OkHttpClient {
        return createTrustedOkHttpClient(context)
    }

    @Provides
    @Singleton
    @Named("AuthRetrofit")
    fun provideRetrofit(@Named("CertClient") client: OkHttpClient): Retrofit {
        val gson = GsonBuilder().setLenient().serializeNulls().create()
        return Retrofit.Builder()
            .baseUrl("https://164.125.253.20:20022/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    @Provides
    fun provideServerAuthAPI(@Named("AuthRetrofit") retrofit: Retrofit): ServerAuthAPI {
        return retrofit.create(ServerAuthAPI::class.java)
    }
}

@Module
@InstallIn(SingletonComponent::class)
object FirebaseModule {

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth {
        return FirebaseAuth.getInstance()
    }
}
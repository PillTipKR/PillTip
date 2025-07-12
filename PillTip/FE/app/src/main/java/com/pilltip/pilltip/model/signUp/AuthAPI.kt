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


    @Provides
    @Singleton
    @Named("AuthRetrofit")
    fun provideRetrofit(client: OkHttpClient): Retrofit {
        val gson = GsonBuilder().setLenient().serializeNulls().create()
        return Retrofit.Builder()
            .baseUrl("https://pilltip.com:20022/")
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
package com.pilltip.pilltip.model.signUp

import com.google.firebase.auth.FirebaseAuth
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
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
import javax.inject.Named
import javax.inject.Singleton

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
    val client = OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .build()

    @Provides
    @Singleton
    @Named("AuthRetrofit")
    fun provideRetrofit(): Retrofit {
        val gson = GsonBuilder()
            .setLenient()
            .serializeNulls()
            .create()

        return Retrofit.Builder()
            .baseUrl("http://164.125.253.20:20022/")
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
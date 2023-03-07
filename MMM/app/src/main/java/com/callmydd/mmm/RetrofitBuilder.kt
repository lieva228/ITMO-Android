package com.callmydd.mmm

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

object RetrofitBuilder {

    private val client = OkHttpClient.Builder().build()
    private const val BASE_URL = "http://213.189.221.170:8008/"

    private fun getRetrofit(): ApiService {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create())
            .build().create(ApiService::class.java)
    }

    val apiService: ApiService = getRetrofit()
}
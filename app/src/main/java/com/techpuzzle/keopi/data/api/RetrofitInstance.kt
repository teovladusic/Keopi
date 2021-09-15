package com.techpuzzle.keopi.data.api

import com.techpuzzle.keopi.utils.Constants.Companion.API_BASE_URL
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {

    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(API_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val api: KeopiApi by lazy {
        retrofit.create(KeopiApi::class.java)
    }
}
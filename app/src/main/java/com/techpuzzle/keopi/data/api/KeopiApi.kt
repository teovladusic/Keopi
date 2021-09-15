package com.techpuzzle.keopi.data.api

import com.techpuzzle.keopi.data.entities.*
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.QueryMap

interface KeopiApi {

    @GET("api/cafes")
    suspend fun getCafes(@QueryMap queryParams: Map<String, String>): Response<CafeListModel>

    @GET("api/cafes/{cafeId}")
    suspend fun getCafeById(
        @Path(
            "cafeId",
            encoded = true
        ) cafeId: String
    ): Response<CafeBar>

    @GET("api/PromoCafes")
    suspend fun getPromoBars(): Response<List<CafeBar>>

    @GET("api/Images")
    suspend fun getCafeImagesById(
        @Query("cafeId") cafeId: String
    ): Response<List<String>>

    @GET("api/Cities")
    suspend fun getCities(): Response<List<City>>

    @GET("api/Areas")
    suspend fun getAreasByCityId(
        @Query("cityId") cityId: String
    ): Response<List<Area>>

    @GET("/api/events/getEventDates2MonthsBackAndUp")
    suspend fun getEventDates2MonthBackAndUp(
        @Query("year") year: Int,
        @Query("month") month: Int,
        @Query("cafeId") cafeId: String? = null
    ): Response<List<String>>

    @GET("api/events")
    suspend fun getEventsForDate(
        @Query("dateTime") date: String,
        @Query("cafeId") cafeId: String? = null
    ): Response<List<Event>>
}
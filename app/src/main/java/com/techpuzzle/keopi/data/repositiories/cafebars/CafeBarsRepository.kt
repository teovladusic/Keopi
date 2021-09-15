package com.techpuzzle.keopi.data.repositiories.cafebars

import androidx.paging.PagingSource
import com.techpuzzle.keopi.data.entities.Area
import com.techpuzzle.keopi.data.entities.CafeBar
import com.techpuzzle.keopi.data.entities.City
import com.techpuzzle.keopi.utils.Resource

interface CafeBarsRepository {

    suspend fun getPromoBars(): Resource<MutableList<CafeBar>>

    fun getCachedCafeBars(): PagingSource<Int, CafeBar>?

    suspend fun getAllCities(): Resource<MutableList<City>>

    suspend fun getAreasByCityId(cityId: String): Resource<MutableList<Area>>

    suspend fun insertCafeBar(cafeBar: CafeBar)

    suspend fun deleteCafeBar(cafeBar: CafeBar)

}
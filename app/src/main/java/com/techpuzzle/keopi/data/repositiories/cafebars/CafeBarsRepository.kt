package com.techpuzzle.keopi.data.repositiories.cafebars

import androidx.paging.PagingSource
import com.techpuzzle.keopi.data.entities.CafeBar
import com.techpuzzle.keopi.data.entities.City
import com.techpuzzle.keopi.data.entities.Area

interface CafeBarsRepository {

    suspend fun getPromoBars(): MutableList<CafeBar>?

    fun getCachedCafeBars(): PagingSource<Int, CafeBar>?

    suspend fun getAllCities(): MutableList<City>?

    suspend fun getAreasByCityId(cityId: String): MutableList<Area>?

    suspend fun insertCafeBar(cafeBar: CafeBar)

    suspend fun deleteCafeBar(cafeBar: CafeBar)

}
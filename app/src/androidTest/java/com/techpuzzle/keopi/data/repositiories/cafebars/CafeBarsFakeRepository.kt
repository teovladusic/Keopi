package com.techpuzzle.keopi.data.repositiories.cafebars

import androidx.paging.PagingSource
import com.techpuzzle.keopi.data.entities.Area
import com.techpuzzle.keopi.data.entities.CafeBar
import com.techpuzzle.keopi.data.entities.City
import com.techpuzzle.keopi.utils.Resource

class CafeBarsFakeRepository : CafeBarsRepository {

    private val cafeBars = mutableListOf<CafeBar>()

    override suspend fun getPromoBars(): Resource<MutableList<CafeBar>> =
        Resource.success(mutableListOf(CafeBar(), CafeBar(), CafeBar()))

    override fun getCachedCafeBars(): PagingSource<Int, CafeBar>? {
        return null
    }

    override suspend fun getAllCities(): Resource<MutableList<City>> =
        Resource.success(mutableListOf(City(), City(), City()))

    override suspend fun getAreasByCityId(cityId: String): Resource<MutableList<Area>> =
        Resource.success(mutableListOf(Area(), Area(), Area()))

    override suspend fun insertCafeBar(cafeBar: CafeBar) {
        cafeBars.add(cafeBar)
    }

    override suspend fun deleteCafeBar(cafeBar: CafeBar) {
        cafeBars.remove(cafeBar)
    }
}
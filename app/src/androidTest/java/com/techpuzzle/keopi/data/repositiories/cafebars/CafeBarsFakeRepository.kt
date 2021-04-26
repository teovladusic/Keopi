package com.techpuzzle.keopi.data.repositiories.cafebars

import androidx.paging.PagingSource
import com.techpuzzle.keopi.data.entities.CafeBar
import com.techpuzzle.keopi.data.entities.City
import com.techpuzzle.keopi.data.entities.Area

class CafeBarsFakeRepository : CafeBarsRepository {

    private val cafeBars = mutableListOf<CafeBar>()

    override suspend fun getPromoBars(): MutableList<CafeBar> =
        mutableListOf(CafeBar(), CafeBar(), CafeBar())

    override fun getCachedCafeBars(): PagingSource<Int, CafeBar>? {
        return null
    }

    override suspend fun getAllCities(): MutableList<City> = mutableListOf(City(), City(), City())

    override suspend fun getAreasByCityId(cityId: String): MutableList<Area>? =
        mutableListOf(Area(), Area(), Area())

    override suspend fun insertCafeBar(cafeBar: CafeBar) {
        cafeBars.add(cafeBar)
    }

    override suspend fun deleteCafeBar(cafeBar: CafeBar) {
        cafeBars.remove(cafeBar)
    }
}
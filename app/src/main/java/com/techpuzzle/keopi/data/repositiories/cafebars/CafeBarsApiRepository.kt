package com.techpuzzle.keopi.data.repositiories.cafebars

import androidx.paging.PagingSource
import com.techpuzzle.keopi.data.api.RetrofitInstance
import com.techpuzzle.keopi.data.entities.Area
import com.techpuzzle.keopi.data.entities.CafeBar
import com.techpuzzle.keopi.data.entities.City
import com.techpuzzle.keopi.data.room.KeopiDatabase
import com.techpuzzle.keopi.utils.Resource
import javax.inject.Inject

class CafeBarsApiRepository @Inject constructor(
    private val db: KeopiDatabase
) : CafeBarsRepository {
    override suspend fun getPromoBars(): Resource<MutableList<CafeBar>> {
        return try {
            val response = RetrofitInstance.api.getPromoBars()
            if (response.isSuccessful && response.body() != null) {
                Resource.success(response.body() as MutableList<CafeBar>)
            } else {
                Resource.error(response.message(), null)
            }
        } catch (e: Exception) {
            Resource.error(e.message.toString(), null)
        }

    }

    override fun getCachedCafeBars(): PagingSource<Int, CafeBar> {
        return db.getDao().getPagedCafeBars()
    }

    override suspend fun getAllCities(): Resource<MutableList<City>> {
        return try {
            val response = RetrofitInstance.api.getCities()
            if (response.isSuccessful && response.body() != null) {
                Resource.success(response.body() as MutableList<City>)
            } else {
                Resource.error(response.message(), null)
            }
        } catch (e: Exception) {
            Resource.error(e.message.toString(), null)
        }

    }

    override suspend fun getAreasByCityId(cityId: String): Resource<MutableList<Area>> {
        return try {
            val response = RetrofitInstance.api.getAreasByCityId(cityId)
            if (response.isSuccessful && response.body() != null) {
                Resource.success(response.body() as MutableList<Area>)
            } else {
                Resource.error(response.message(), null)
            }
        }catch (e: Exception) {
            Resource.error(e.message.toString(), null)
        }

    }

    override suspend fun insertCafeBar(cafeBar: CafeBar) {
        db.getDao().insertCafeBar(cafeBar)
    }

    override suspend fun deleteCafeBar(cafeBar: CafeBar) {
        db.getDao().deleteCafeBar(cafeBar)
    }
}
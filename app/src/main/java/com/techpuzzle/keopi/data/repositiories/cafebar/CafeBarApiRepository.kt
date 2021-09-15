package com.techpuzzle.keopi.data.repositiories.cafebar

import com.techpuzzle.keopi.data.api.RetrofitInstance
import com.techpuzzle.keopi.data.entities.CafeBar
import com.techpuzzle.keopi.data.room.KeopiDatabase
import com.techpuzzle.keopi.utils.Resource
import javax.inject.Inject

class CafeBarApiRepository @Inject constructor(
    private val db: KeopiDatabase
) : CafeBarRepository {
    override suspend fun getImageUrlsByCafeId(cafeBarId: String): Resource<List<String>> {
        return try {
            val response = RetrofitInstance.api.getCafeImagesById(cafeBarId)
            return if (response.isSuccessful) {
                Resource.success(response.body() ?: emptyList())
            } else {
                Resource.error(response.message(), null)
            }
        } catch (e: Exception) {
            Resource.error(e.message.toString(), null)
        }
    }

    override suspend fun insertCafeBar(cafeBar: CafeBar) = db.getDao().insertCafeBar(cafeBar)

}
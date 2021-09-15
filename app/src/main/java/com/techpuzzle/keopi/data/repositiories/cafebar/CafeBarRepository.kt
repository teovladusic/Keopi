package com.techpuzzle.keopi.data.repositiories.cafebar

import com.techpuzzle.keopi.data.entities.CafeBar
import com.techpuzzle.keopi.utils.Resource

interface CafeBarRepository {

    //suspend fun getCafeBar(cafeBarId: String) : CafeBar

    suspend fun getImageUrlsByCafeId(cafeBarId: String): Resource<List<String>>

    suspend fun insertCafeBar(cafeBar: CafeBar)
}
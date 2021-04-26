package com.techpuzzle.keopi.data.repositiories.cafebar

import com.techpuzzle.keopi.data.entities.CafeBar

interface CafeBarRepository {

    suspend fun getImageUrlsByCafeId(cafeBarId: String): List<String>?

    suspend fun insertCafeBar(cafeBar: CafeBar)
}
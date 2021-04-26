package com.techpuzzle.keopi.data.repositories

import com.techpuzzle.keopi.data.entities.CafeBar
import com.techpuzzle.keopi.data.repositiories.cafebar.CafeBarRepository

class FakeRepository : CafeBarRepository {

    private val cafeBars = mutableListOf<CafeBar>()

    private val urls = listOf("1.com", "2.com", "3.com")

    override suspend fun getImageUrlsByCafeId(cafeBarId: String): List<String> {
        return urls
    }

    override suspend fun insertCafeBar(cafeBar: CafeBar) {
        cafeBars.add(cafeBar)
    }
}

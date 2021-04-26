package com.techpuzzle.keopi.data.repositiories.order

import com.techpuzzle.keopi.data.entities.Drink

interface OrderRepository {

    suspend fun loadPriceList(priceListId: String) : List<Drink>
}
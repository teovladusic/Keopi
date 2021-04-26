package com.techpuzzle.keopi.data.repositiories.order

import com.google.firebase.firestore.FirebaseFirestore
import com.techpuzzle.keopi.data.entities.Drink
import kotlinx.coroutines.tasks.await

class OrderDefaultRepository : OrderRepository {
    private val firebaseFirestore = FirebaseFirestore.getInstance()

    override suspend fun loadPriceList(priceListId: String): List<Drink> =
        firebaseFirestore.collection("cjenik").document(priceListId).collection("pica").get()
            .await().toObjects(Drink::class.java)
}
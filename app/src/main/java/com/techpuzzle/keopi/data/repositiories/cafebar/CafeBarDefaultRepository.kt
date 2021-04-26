package com.techpuzzle.keopi.data.repositiories.cafebar

import com.techpuzzle.keopi.data.entities.CafeBar
import com.techpuzzle.keopi.data.room.KeopiDatabase
import com.techpuzzle.keopi.keopiApp
import io.realm.mongodb.Credentials
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.bson.Document
import java.lang.Exception
import javax.inject.Inject

class CafeBarDefaultRepository @Inject constructor(
    private val db: KeopiDatabase
) : CafeBarRepository {

    override suspend fun getImageUrlsByCafeId(cafeBarId: String): List<String>? {
        val user = keopiApp.currentUser() ?: return null

        val imageUrlsCollection =
            user.getMongoClient("mongodb-atlas").getDatabase("KeopiDatabase")
                .getCollection("images")
        var imageUrls: List<String>? = null
        val pipeLine = listOf(
            Document("\$match", Document("cafeId", cafeBarId)),
            Document(
                "\$project", Document("urls", 1).append("_id", 0)
            )
        )

        CoroutineScope(Dispatchers.IO).launch {
            try {
                imageUrlsCollection.aggregate(pipeLine).iterator().get().forEach {
                    imageUrls = it["urls"] as List<String>
                }
            } catch (e: Exception) {
                imageUrls = null
            }
        }.join()
        return imageUrls
    }

    override suspend fun insertCafeBar(cafeBar: CafeBar) = db.getDao().insertCafeBar(cafeBar)
}
package com.techpuzzle.keopi.data.repositiories.cafebars

import androidx.paging.PagingData
import com.techpuzzle.keopi.data.entities.CafeBar
import com.techpuzzle.keopi.data.entities.City
import com.techpuzzle.keopi.data.entities.Area
import com.techpuzzle.keopi.data.room.KeopiDatabase
import com.techpuzzle.keopi.keopiApp
import com.techpuzzle.keopi.utils.bsonToCafeBar
import io.realm.mongodb.Credentials
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.bson.Document
import org.bson.types.ObjectId
import java.lang.Exception
import javax.inject.Inject

class CafeBarsDefaultRepository @Inject constructor(
    private val db: KeopiDatabase
) : CafeBarsRepository {


    private val TAG = "CafeBarsDefaultRepository"

    override suspend fun getPromoBars(): MutableList<CafeBar>? {
        var user = keopiApp.currentUser()
        if (user == null) {
            login()
            user = keopiApp.currentUser()!!
        }

        val database = user.getMongoClient("mongodb-atlas").getDatabase("KeopiDatabase")
        val promoBarsCollection = database.getCollection("promoBars")
        val cafesCollection = database.getCollection("cafes")

        val promoBarIds = mutableListOf<ObjectId>()
        val pipeline = listOf(
            Document(
                "\$project", Document("cafeId", 1).append("_id", 0)
            )
        )
        CoroutineScope(Dispatchers.IO).launch {
            try {
                promoBarsCollection.aggregate(pipeline).iterator().get().forEach { document ->
                    promoBarIds.add(ObjectId(document["cafeId"].toString()))
                }
            } catch (e: Exception) {
                return@launch
            }
        }.join()

        var promoBars: MutableList<CafeBar>? = mutableListOf()
        val filterQuery = Document(
            "_id", Document("\$in", promoBarIds)
        )
        CoroutineScope(Dispatchers.IO).launch {
            try {
                cafesCollection.find(filterQuery).iterator().get().forEach { document ->
                    promoBars?.add(bsonToCafeBar(document)!!)
                }
            } catch (e: Exception) {
                promoBars = null
            }
        }.join()
        return promoBars
    }

    override fun getCachedCafeBars() = db.getDao().getPagedCafeBars()

    override suspend fun getAllCities(): MutableList<City>? {
        var user = keopiApp.currentUser()
        if (user == null) {
            login()
            user = keopiApp.currentUser()!!
        }

        val citiesCollection = user.getMongoClient("mongodb-atlas").getDatabase("KeopiDatabase")
            .getCollection("cities")

        var cities: MutableList<City>? = mutableListOf()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                citiesCollection.find().iterator().get().forEach { document ->
                    val city = City(document["_id"].toString(), document["name"].toString())
                    cities?.add(city)
                }
            } catch (e: Exception) {
                cities = null
            }
        }.join()
        return cities
    }

    override suspend fun getAreasByCityId(cityId: String): MutableList<Area>? {
        val user = keopiApp.currentUser() ?: return null
        val areasCollection =
            user.getMongoClient("mongodb-atlas").getDatabase("KeopiDatabase").getCollection("areas")
        var areas: MutableList<Area>? = mutableListOf()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                areasCollection.find(Document("cityId", cityId)).iterator().get()
                    .forEach { document ->
                        val area = Area(
                            _id = document["_id"].toString(),
                            cityId = document["cityId"].toString(),
                            name = document["name"].toString()
                        )
                        areas?.add(area)
                    }
            } catch (e: Exception) {
                areas = null
            }
        }.join()
        return areas
    }


    override suspend fun insertCafeBar(cafeBar: CafeBar) {
        db.getDao().insertCafeBar(cafeBar)
    }

    override suspend fun deleteCafeBar(cafeBar: CafeBar) {
        db.getDao().deleteCafeBar(cafeBar)
    }

    private suspend fun login() {
        CoroutineScope(Dispatchers.IO).launch {
            val credentials = Credentials.anonymous()
            keopiApp.login(credentials)
        }.join()
    }
}
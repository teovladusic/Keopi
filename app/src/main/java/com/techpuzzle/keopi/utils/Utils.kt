package com.techpuzzle.keopi.utils

import com.techpuzzle.keopi.data.entities.CafeBar
import com.techpuzzle.keopi.data.entities.Event
import org.bson.Document
import java.lang.Exception
import kotlin.random.Random


val <T> T.exhaustive: T
    get() = this

fun bsonToCafeBar(document: Document): CafeBar? {
    return try {
        CafeBar(
            _id = document["_id"].toString(),
            address = document["address"].toString(),
            bio = document["bio"].toString(),
            cjenikId = document["cjenikId"].toString(),
            name = document["name"].toString(),
            capacity = document["capacity"].toString(),
            betting = document["betting"].toString().toBoolean(),
            location = document["location"].toString(),
            cityId = document["cityId"].toString(),
            areaId = document["areaId"].toString(),
            latitude = document["latitude"].toString(),
            longitude = document["longitude"].toString(),
            music = document["music"].toString(),
            age = document["age"].toString(),
            smoking = document["smoking"].toString().toBoolean(),
            startingWorkTime = document["startingWorkTime"].toString().toInt(),
            endingWorkTime = document["endingWorkTime"].toString().toInt(),
            picture = document["picture"].toString(),
            terrace = document["terrace"].toString().toBoolean(),
            dart = document["dart"].toString().toBoolean(),
            billiards = document["billiards"].toString().toBoolean(),
            hookah = document["hookah"].toString().toBoolean(),
            playground = document["playground"].toString().toBoolean(),
            instagram = document["instagram"].toString(),
            facebook = document["facebook"].toString()
            )
    } catch (e: Exception) {
        null
    }
}

fun bsonToEvent(document: Document) : Event? {
    return try {
        Event(
            _id = document["_id"].toString(),
            time = document["time"].toString(),
            price = document["price"].toString(),
            date = document["date"].toString(),
            performer = document["performer"].toString(),
            cafeBarId = document["cafeBarId"].toString(),
            description = document["description"].toString(),
            type = document["type"].toString().toInt()
        )
    } catch (e: Exception) {
        null
    }
}

val seed = Random.nextInt(5, 21)


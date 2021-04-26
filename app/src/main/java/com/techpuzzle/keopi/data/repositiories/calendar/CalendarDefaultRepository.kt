package com.techpuzzle.keopi.data.repositiories.calendar

import com.google.firebase.firestore.FirebaseFirestore
import com.techpuzzle.keopi.data.entities.CafeBar
import com.techpuzzle.keopi.data.entities.Event
import com.techpuzzle.keopi.keopiApp
import com.techpuzzle.keopi.utils.bsonToCafeBar
import com.techpuzzle.keopi.utils.bsonToEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import org.bson.Document
import org.bson.types.ObjectId
import java.lang.Exception

class CalendarDefaultRepository : CalendarRepository {

    private val firebaseFirestore = FirebaseFirestore.getInstance()

    override suspend fun getEvents(cafeBarId: String): MutableList<Event> {
        return if (cafeBarId.isEmpty()) {
            firebaseFirestore.collection("events").get().await().toObjects(Event::class.java)
        } else {
            firebaseFirestore.collection("events").whereEqualTo("cafeBarId", cafeBarId).get()
                .await().toObjects(Event::class.java)
        }
    }

    override suspend fun getCafeBarById(id: String): CafeBar? =
        firebaseFirestore.collection("kafici").document(id).get().await()
            .toObject(CafeBar::class.java)

    override suspend fun getEventDates(cafeBarId: String): List<String>? {
        val user = keopiApp.currentUser() ?: return null
        val eventsCollection = user.getMongoClient("mongodb-atlas").getDatabase("KeopiDatabase")
            .getCollection("events")
        val pipeLine = if (cafeBarId.isEmpty()) {
            listOf(
                Document(
                    "\$project", Document("date", 1).append("_id", 0)
                )
            )
        } else {
            listOf(
                Document("\$match", Document("cafeBarId", cafeBarId)),
                Document(
                    "\$project", Document("date", 1).append("_id", 0)
                )
            )
        }
        var eventDates: MutableList<String>? = mutableListOf()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                eventsCollection.aggregate(pipeLine).iterator().get().forEach { document ->
                    eventDates?.add(document["date"].toString())
                }
            } catch (e: Exception) {
                eventDates = null
                return@launch
            }

        }.join()
        return eventDates
    }

    override suspend fun getEventsForDate(date: String, cafeBarId: String): List<Event>? {
        val user = keopiApp.currentUser() ?: return null
        val eventsCollection = user.getMongoClient("mongodb-atlas").getDatabase("KeopiDatabase")
            .getCollection("events")
        var events: MutableList<Event>? = mutableListOf()

        val pipeLine = if (cafeBarId.isEmpty()) {
            listOf(
                Document("\$match", Document("date", date))
                )
        } else {
            listOf(
                Document("\$match", Document("cafeBarId", cafeBarId)),
                Document("\$match", Document("date", date)),
            )
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                eventsCollection.aggregate(pipeLine).iterator().get().forEach { document ->
                    events?.add(bsonToEvent(document)!!)
                }
            } catch (e: Exception) {
                events = null
                return@launch
            }
        }.join()
        return events
    }

    override suspend fun getCafesById(cafeIds: List<String>): List<CafeBar>? {
        val user = keopiApp.currentUser() ?: return null
        val cafesCollection = user.getMongoClient("mongodb-atlas").getDatabase("KeopiDatabase")
            .getCollection("cafes")

        val cafeObjectIds = mutableListOf<ObjectId>()
        cafeIds.forEach { cafeId ->
            cafeObjectIds.add(ObjectId(cafeId))
        }
        var cafeBars: MutableList<CafeBar>? = mutableListOf()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                cafesCollection.find(Document("_id", Document("\$in", cafeObjectIds))).iterator()
                    .get()
                    .forEach { document ->
                        val cafeBar = bsonToCafeBar(document)!!
                        cafeBars?.add(cafeBar.copy(_id = document["_id"].toString()))
                    }
            } catch (e: Exception) {
                cafeBars = null
                return@launch
            }
        }.join()
        return cafeBars
    }

}
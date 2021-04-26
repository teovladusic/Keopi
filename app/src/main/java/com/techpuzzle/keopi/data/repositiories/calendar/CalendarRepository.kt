package com.techpuzzle.keopi.data.repositiories.calendar

import com.techpuzzle.keopi.data.entities.CafeBar
import com.techpuzzle.keopi.data.entities.Event

interface CalendarRepository {

    suspend fun getEvents(cafeBarId: String = ""): MutableList<Event>

    suspend fun getCafeBarById(id: String): CafeBar?

    suspend fun getEventDates(cafeBarId: String = ""): List<String>?

    suspend fun getEventsForDate(date: String, cafeBarId: String = ""): List<Event>?

    suspend fun getCafesById(cafeIds: List<String>) : List<CafeBar>?
}
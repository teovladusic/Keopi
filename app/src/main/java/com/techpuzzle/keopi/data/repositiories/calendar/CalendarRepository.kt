package com.techpuzzle.keopi.data.repositiories.calendar

import com.techpuzzle.keopi.data.entities.CafeBar
import com.techpuzzle.keopi.data.entities.Event

interface CalendarRepository {

    suspend fun getEventDates(cafeBarId: String = "", year: Int, month: Int): List<String>?

    suspend fun getEventsForDate(date: String, cafeBarId: String = ""): List<Event>?

    suspend fun getCafesById(cafeIds: List<String>): List<CafeBar>?
}
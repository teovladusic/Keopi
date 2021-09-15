package com.techpuzzle.keopi.data.repositiories.calendar

import com.techpuzzle.keopi.data.api.RetrofitInstance
import com.techpuzzle.keopi.data.entities.CafeBar
import com.techpuzzle.keopi.data.entities.Event

class CalendarApiRepository : CalendarRepository {

    override suspend fun getEventDates(cafeBarId: String, year: Int, month: Int): List<String>? {
        return try {
            if (cafeBarId.isEmpty()) {
                val response = RetrofitInstance.api.getEventDates2MonthBackAndUp(year, month)
                if (response.isSuccessful) {
                    response.body()
                } else {
                    emptyList()
                }
            } else {
                val response = RetrofitInstance.api.getEventDates2MonthBackAndUp(year, month, cafeId = cafeBarId)
                if (response.isSuccessful) {
                    response.body()
                } else {
                    emptyList()
                }
            }

        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun getEventsForDate(date: String, cafeBarId: String): List<Event>? {
        return try {
            if (cafeBarId.isEmpty()) {
                val response = RetrofitInstance.api.getEventsForDate(date)
                if (response.isSuccessful) {
                    response.body()
                } else {
                    emptyList()
                }
            } else {
                val response = RetrofitInstance.api.getEventsForDate(date, cafeBarId)
                if (response.isSuccessful) {
                    response.body()
                } else {
                    emptyList()
                }
            }

        } catch (e: Exception) {
            return null
        }

    }

    override suspend fun getCafesById(cafeIds: List<String>): List<CafeBar> {
        return try {
            val cafeBars = mutableListOf<CafeBar>()
            for (cafeId in cafeIds) {
                val response = RetrofitInstance.api.getCafeById(cafeId)
                if (response.isSuccessful && response.body() != null) {
                    cafeBars.add(response.body()!!)
                }
            }
            cafeBars
        } catch (e: Exception) {
            emptyList()
        }
    }
}
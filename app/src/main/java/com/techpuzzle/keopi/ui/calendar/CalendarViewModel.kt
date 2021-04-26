package com.techpuzzle.keopi.ui.calendar

import android.util.Log
import androidx.lifecycle.*
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.techpuzzle.keopi.data.entities.CafeBar
import com.techpuzzle.keopi.data.entities.Event
import com.techpuzzle.keopi.data.repositiories.calendar.CalendarRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject

@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val repository: CalendarRepository,
    state: SavedStateHandle
) : ViewModel() {

    val cafeBar = state.get<CafeBar>("cafeBar")

    private val calendarEventsChannel = Channel<CalendarEvents>()
    val calendarEvents = calendarEventsChannel.receiveAsFlow()

    val selectedDateStringLiveData = state.getLiveData("selectedDate", LocalDate.now())

    val daysOfWeek = arrayOf(
        DayOfWeek.MONDAY,
        DayOfWeek.TUESDAY,
        DayOfWeek.WEDNESDAY,
        DayOfWeek.THURSDAY,
        DayOfWeek.FRIDAY,
        DayOfWeek.SATURDAY,
        DayOfWeek.SUNDAY
    )

    val currentMonth: YearMonth = YearMonth.now()
    val currentMonthMinus2: YearMonth = currentMonth.minusMonths(2)
    val currentMonthMinus1: YearMonth = currentMonth.minusMonths(1)
    val currentMonthPlus1: YearMonth = currentMonth.plusMonths(1)
    val currentMonthPlus2: YearMonth = currentMonth.plusMonths(2)

    private var allEventsWithCafeBars = mutableListOf<EventWithCafeBar>()

    private val eventDates = MutableStateFlow(listOf<String>())
    val eventDatesLiveData = eventDates.asLiveData()

    private val eventsTombola = MutableStateFlow(listOf(EventWithCafeBar()))
    val eventsTombolaLiveData = eventsTombola.asLiveData()

    private val eventsParties = MutableStateFlow(listOf(EventWithCafeBar()))
    val eventsPartiesLiveData = eventsParties.asLiveData()

    private val eventsConcerts = MutableStateFlow(listOf(EventWithCafeBar()))
    val eventsConcertsLiveData = eventsConcerts.asLiveData()

    var bottomSheetState = BottomSheetBehavior.STATE_HIDDEN
    var lastSelectedEvent: EventWithCafeBar? = null


    fun loadEvents() = viewModelScope.launch(Dispatchers.IO) {
        if (allEventsWithCafeBars.isEmpty()) {
            calendarEventsChannel.send(CalendarEvents.Loading)
            eventDates.value = if (cafeBar == null) {
                repository.getEventDates()
            } else {
                repository.getEventDates(cafeBar._id)
            } ?: emptyList()
            calendarEventsChannel.send(CalendarEvents.Loaded)
            onDaySelected(selectedDateStringLiveData.value!!)
        } else {
            calendarEventsChannel.send(CalendarEvents.Loaded)
        }
    }


    fun onDaySelected(localDate: LocalDate) = viewModelScope.launch {
        calendarEventsChannel.send(CalendarEvents.EventLoading)
        val localDateString = getDateString(localDate)
        val eventsOnDate = if (cafeBar == null) {
            repository.getEventsForDate(localDateString)
        } else {
            repository.getEventsForDate(localDateString, cafeBar._id)
        } ?: emptyList()

        val cafeIds = eventsOnDate.map { it.cafeBarId }.distinct().toList()
        val cafeBars = repository.getCafesById(cafeIds) ?: emptyList()


        val concerts = async { setupEvents(0, eventsOnDate, cafeBars) }
        val tombola = async { setupEvents(1, eventsOnDate, cafeBars) }
        val parties = async { setupEvents(2, eventsOnDate, cafeBars) }

        eventsTombola.value = tombola.await()
        eventsConcerts.value = concerts.await()
        eventsParties.value = parties.await()
        calendarEventsChannel.send(CalendarEvents.EventLoaded)

        if (eventsTombola.value.isEmpty() && eventsParties.value.isEmpty() && eventsConcerts.value.isEmpty()) {
            viewModelScope.launch {
                calendarEventsChannel.send(CalendarEvents.ShowNoEventsMessage)
            }
        }
    }

    private suspend fun setupEvents(type: Int, eventsOnDate: List<Event>, cafeBars: List<CafeBar>) =
        withContext(Dispatchers.Default) {
            val events = eventsOnDate.filter { it.type == type }
            val eventsWithCafeBars = mutableListOf<EventWithCafeBar>()
            for (event in events) {
                eventsWithCafeBars.add(
                    EventWithCafeBar(
                        event = event,
                        cafeBar = cafeBars.filter { it._id == event.cafeBarId }[0]
                    )
                )
            }
            return@withContext eventsWithCafeBars
        }


    fun onBackPressed() = viewModelScope.launch {
        if (cafeBar == null) {
            calendarEventsChannel.send(CalendarEvents.NavigateToMainScreen)
        } else {
            calendarEventsChannel.send(CalendarEvents.NavigateToCafeBarScreen(cafeBar))
        }
    }


    private fun getDateString(date: LocalDate): String {
        val dateSting = date.toString()
        val day = dateSting.substring(8, 10)
        val month = dateSting.substring(5, 7)
        val year = dateSting.substring(0, 4)
        return "$day.$month.$year."
    }


    sealed class CalendarEvents {
        object Loading : CalendarEvents()
        object Loaded : CalendarEvents()
        object ShowNoEventsMessage : CalendarEvents()
        object NavigateToMainScreen : CalendarEvents()
        data class NavigateToCafeBarScreen(val cafeBar: CafeBar) : CalendarEvents()
        object EventLoading : CalendarEvents()
        object EventLoaded : CalendarEvents()
    }

    data class EventWithCafeBar(
        val event: Event = Event(),
        val cafeBar: CafeBar = CafeBar()
    )
}
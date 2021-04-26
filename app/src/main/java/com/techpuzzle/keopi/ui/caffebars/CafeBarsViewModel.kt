package com.techpuzzle.keopi.ui.caffebars

import android.util.Log
import androidx.lifecycle.*
import androidx.paging.*
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.firebase.messaging.FirebaseMessaging
import com.techpuzzle.keopi.data.entities.CafeBar
import com.techpuzzle.keopi.data.entities.City
import com.techpuzzle.keopi.data.entities.Area
import com.techpuzzle.keopi.data.paging.CafesPagingSource
import com.techpuzzle.keopi.data.repositiories.cafebars.CafeBarsRepository
import com.techpuzzle.keopi.utils.Event
import com.techpuzzle.keopi.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import org.bson.Document
import java.io.IOException
import java.lang.Exception
import javax.inject.Inject

@HiltViewModel
class CafeBarsViewModel @Inject constructor(
    private val repository: CafeBarsRepository,
    private val state: SavedStateHandle
) : ViewModel() {

    private val TAG = "CafeBarsViewModel"

    private val cafeBarEventsChannel = Channel<CafeBarsEvents>()
    val cafeBarsEvents = cafeBarEventsChannel.receiveAsFlow()

    val searchQueryLiveData = state.getLiveData("searchQuery", "")
    private val searchQuery = searchQueryLiveData.asFlow()

   private val matchQueriesLiveData = state.getLiveData("matchQueries", emptyList<Document>())
   private val matchQueries = matchQueriesLiveData.asFlow()

    val promoCafeBarsFlow = MutableStateFlow(listOf<CafeBar>())

    private var _getPromoBarsStatus = MutableLiveData<Event<Resource<List<CafeBar>>>>()
    val getPromoBarsStatus: LiveData<Event<Resource<List<CafeBar>>>> = _getPromoBarsStatus




    fun loadPromoBars() = viewModelScope.launch {
        val promoBars = repository.getPromoBars() ?: emptyList()
        if (promoBars.size == 3) {
            promoCafeBarsFlow.value = promoBars
            _getPromoBarsStatus.postValue(Event(Resource.success(promoBars)))
        } else {
            _getPromoBarsStatus.postValue(
                Event(
                    Resource.error(
                        "Didnt received 3 cafe bars",
                        promoBars
                    )
                )
            )
        }
    }

    var lastCafeBars: PagingData<CafeBar>? = null
    private var lastMatchQuery = emptyList<Document>()
    private var lastSearchQuery = ""

    @ExperimentalCoroutinesApi
    val cafeBarsFlow = combine(
        matchQueries,
        searchQuery
    ) { matchQueries, searchQuery ->
        Pair(matchQueries, searchQuery)
    }.flatMapLatest { (matchQueries, searchQuery) ->
        if (lastCafeBars != null && lastMatchQuery == matchQueries && lastSearchQuery == searchQuery) {
            flowOf(lastCafeBars)
        } else {
            lastSearchQuery = searchQuery
            lastMatchQuery = matchQueries
            withContext(Dispatchers.IO) {
                getSearchResults(matchQueries, searchQuery)
                    .cachedIn(viewModelScope)
            }
        }
    }

    fun loadCafeBars() = viewModelScope.launch {
        //refresh search query
        val realQuery = searchQueryLiveData.value ?: ""
        searchQueryLiveData.postValue("a")
        delay(5)
        searchQueryLiveData.postValue(realQuery)
    }

    private fun getSearchResults(
        matchQueries: List<Document>,
        searchQuery: String
    ): Flow<PagingData<CafeBar>> {
        return Pager(
            config = PagingConfig(
                pageSize = 5,
            ),
            pagingSourceFactory = { CafesPagingSource(searchQuery, matchQueries) }
        ).flow
    }


    val cachedCafeBarsFlow = Pager(
        PagingConfig(pageSize = 10)
    ) {
        repository.getCachedCafeBars()!!
    }.flow.cachedIn(viewModelScope)


    fun onAddRemoveBarClick(
        hostingFragment: CafeBarsFragment.HostingFragments,
        cafeBar: CafeBar
    ) {
        if (hostingFragment == CafeBarsFragment.HostingFragments.CachedCafeBars) {
            removeCafeBar(cafeBar)
        } else {
            addCafeBar(cafeBar)
        }
    }

    private val _addCafeBarStatus = MutableLiveData<Event<Resource<CafeBar>>>()
    val addCafeBarStatus: LiveData<Event<Resource<CafeBar>>> = _addCafeBarStatus

    fun addCafeBar(cafeBar: CafeBar) = viewModelScope.launch {
        if (cafeBar._id.isEmpty()) {
            _addCafeBarStatus.postValue(Event(Resource.error("Cafe bar id is not valid", null)))
            return@launch
        }
        try {
            FirebaseMessaging.getInstance().subscribeToTopic("/topics/${cafeBar._id}")
            repository.insertCafeBar(cafeBar)
            _addCafeBarStatus.postValue(Event(Resource.success(cafeBar)))
            cafeBarEventsChannel.send(CafeBarsEvents.ShowMessage("Uspjesno spremljeno"))
        } catch (e: IOException) {
            _addCafeBarStatus.postValue(Event(Resource.error("Item has not been added", null)))
            cafeBarEventsChannel.send(CafeBarsEvents.ShowMessage("Dogodila se greska"))
        }
    }

    private val _removeCafeBarStatus = MutableLiveData<Event<Resource<CafeBar>>>()
    val removeCafeBarStatus: LiveData<Event<Resource<CafeBar>>> = _removeCafeBarStatus

    fun removeCafeBar(cafeBar: CafeBar) = viewModelScope.launch {
        if (cafeBar._id.isEmpty()) {
            _removeCafeBarStatus.postValue(Event(Resource.error("Cafe bar id is not valid", null)))
            return@launch
        }
        try {
            repository.deleteCafeBar(cafeBar)
            FirebaseMessaging.getInstance().unsubscribeFromTopic("/topics/${cafeBar._id}")
            _removeCafeBarStatus.postValue(Event(Resource.success(null)))
        } catch (e: IOException) {
            _removeCafeBarStatus.postValue(Event(Resource.error("Item has not been deleted", null)))
            cafeBarEventsChannel.send(CafeBarsEvents.ShowMessage("Dogodila se greska"))
        }
    }

    val cities = MutableStateFlow<List<City>>(emptyList())

    private val _loadCitiesStatus = MutableLiveData<Event<Resource<List<City>>>>()
    val loadCitiesStatus: LiveData<Event<Resource<List<City>>>> = _loadCitiesStatus

    fun loadCities() = viewModelScope.launch {
        if (cities.value.isNotEmpty()) {
            return@launch
        }
        val loadedCities = repository.getAllCities() ?: mutableListOf()
        cities.value = loadedCities
        if (loadedCities.isEmpty()) {
            _loadCitiesStatus.postValue(Event(Resource.error("cities cant be empty", null)))
        } else {
            _loadCitiesStatus.postValue(Event(Resource.success(loadedCities)))
        }
    }

    var bottomSheetState = BottomSheetBehavior.STATE_HIDDEN

    val arrayOdDo = arrayOf("All", "From", "Until")

    var mSongType = "All"
    var mCanSmoke = false
    var mHasDart = false
    var mHasSlotMachine = false
    var mHasBilliard = false

    var spinnerSongsPosition = 0
    var spinnerLocationPosition = 0
    var fromUntilPosition = 0
    var timePosition = 0
    var spinnerPeopleCapacityPosition = 0
    var spinnerAgePosition = 0
    var spinnerAreaPosition = 0


    private val _setFilterQueryStatus = MutableLiveData<Event<Resource<Nothing>>>()
    val setFilterQueryStatus: LiveData<Event<Resource<Nothing>>> = _setFilterQueryStatus

    private val _isQueriedByCity = MutableLiveData<Boolean>()
    val isQueriedByCity: LiveData<Boolean> = _isQueriedByCity

    private val _isQueriedByArea = MutableLiveData<Boolean>()
    val isQueriedByArea: LiveData<Boolean> = _isQueriedByArea

    private val _isQueriedByWorkingTime = MutableLiveData<Boolean>()
    val isQueriedByWorkingTime: LiveData<Boolean> = _isQueriedByWorkingTime

    private val _isQueriedByMusic = MutableLiveData<Boolean>()
    val isQueriedByMusic: LiveData<Boolean> = _isQueriedByMusic

    private val _isQueriedByPeopleCapacity = MutableLiveData<Boolean>()
    val isQueriedByPeopleCapacity: LiveData<Boolean> = _isQueriedByPeopleCapacity

    private val _isQueriedByAge = MutableLiveData<Boolean>()
    val isQueriedByAge: LiveData<Boolean> = _isQueriedByAge

    private val _isQueriedBySmoking = MutableLiveData<Boolean>()
    val isQueriedBySmoking: LiveData<Boolean> = _isQueriedBySmoking

    private val _isQueriedByDart = MutableLiveData<Boolean>()
    val isQueriedByDart: LiveData<Boolean> = _isQueriedByDart

    private val _isQueriedBySlotMachine = MutableLiveData<Boolean>()
    val isQueriedBySlotMachine: LiveData<Boolean> = _isQueriedBySlotMachine

    private val _isQueriedByBilliard = MutableLiveData<Boolean>()
    val isQueriedByBilliard: LiveData<Boolean> = _isQueriedByBilliard

    fun setFilerQuery(
        songType: String,
        fromUntil: String,
        time: String,
        canSmoke: Boolean,
        hasDart: Boolean,
        hasSlotMachine: Boolean,
        hasBilliard: Boolean,
        cityPosition: Int,
        kvartPosition: Int,
        age: String,
        capacity: String
    ) = viewModelScope.launch {
        try {
            mSongType = songType
            mCanSmoke = canSmoke
            mHasDart = hasDart
            mHasSlotMachine = hasSlotMachine
            mHasBilliard = hasBilliard

            val matchDocuments = mutableListOf<Document>()

            if (kvartPosition != -1 && kvartPosition != 0 && cityPosition != 0) {
                _isQueriedByArea.postValue(true)
                val areaId = areas[kvartPosition - 1]._id
                matchDocuments.add(Document("\$match", Document("areaId", areaId)))
            } else _isQueriedByArea.postValue(false)
            if (cityPosition != 0 && (kvartPosition == 0 || kvartPosition == -1)) {
                _isQueriedByCity.postValue(true)
                val cityId = cities.value[cityPosition - 1]._id
                matchDocuments.add(Document("\$match", Document("cityId", cityId)))
            } else _isQueriedByCity.postValue(false)
            if (fromUntil != "All" && time != "All") {
                val workingHours = getWorkingHours(fromUntil, time)
                if (workingHours != mutableListOf(-1)) {
                    _isQueriedByWorkingTime.postValue(true)
                    if (fromUntil == "Od") {
                        matchDocuments.add(
                            Document(
                                "\$match",
                                Document(
                                    "startingWorkTime",
                                    Document("\$in", workingHours)
                                )
                            )
                        )
                    } else {
                        matchDocuments.add(
                            Document(
                                "\$match",
                                Document(
                                    "endingWorkTime",
                                    Document("\$in", workingHours)
                                )
                            )
                        )
                    }
                } else _isQueriedByWorkingTime.postValue(false)
            } else _isQueriedByWorkingTime.postValue(false)
            if (age != "All") {
                _isQueriedByAge.postValue(true)
                matchDocuments.add(Document("\$match", Document("age", age)))
            } else _isQueriedByAge.postValue(false)
            if (capacity != "All") {
                _isQueriedByPeopleCapacity.postValue(true)
                matchDocuments.add(Document("\$match", Document("capacity", capacity)))
            } else _isQueriedByPeopleCapacity.postValue(false)
            if (songType != "All") {
                _isQueriedByMusic.postValue(true)
                matchDocuments.add(Document("\$match", Document("music", songType)))
            } else _isQueriedByMusic.postValue(false)
            if (canSmoke) {
                _isQueriedBySmoking.postValue(true)
                matchDocuments.add(Document("\$match", Document("smoking", true)))
            } else _isQueriedBySmoking.postValue(false)
            if (hasDart) {
                _isQueriedByDart.postValue(true)
                matchDocuments.add(Document("\$match", Document("dart", true)))
            } else _isQueriedByDart.postValue(false)
            if (hasSlotMachine) {
                _isQueriedBySlotMachine.postValue(true)
                matchDocuments.add(Document("\$match", Document("betting", true)))
            } else _isQueriedBySlotMachine.postValue(false)
            if (hasBilliard) {
                _isQueriedByBilliard.postValue(true)
                matchDocuments.add(Document("\$match", Document("billiards", true)))
            } else _isQueriedByBilliard.postValue(false)
            _setFilterQueryStatus.postValue(Event(Resource.success(null)))

            matchQueriesLiveData.postValue(matchDocuments)


        } catch (e: Exception) {
            Log.d(TAG, e.message.toString())
            _setFilterQueryStatus.postValue(Event(Resource.error("${e.message}", null)))
        }
    }

    private var areas = listOf<Area>()

    private val _loadAreasStatus = MutableLiveData<Event<Resource<List<Area>>>>()
    val loadAreaStatus: LiveData<Event<Resource<List<Area>>>> = _loadAreasStatus

    fun loadAreas(cityPosition: Int) = viewModelScope.launch {
        try {
            val cityId = cities.value[cityPosition - 1]._id
            areas = repository.getAreasByCityId(cityId)?.toList() ?: emptyList()
            _loadAreasStatus.postValue(Event(Resource.success(areas)))
            val _areas = mutableListOf("All")
            _areas.addAll(areas.map { it.name })
            cafeBarEventsChannel.send(CafeBarsEvents.SetKvartoviAdapter(_areas))
        } catch (e: Exception) {
            _loadAreasStatus.postValue(Event(Resource.error("An error occurred", null)))
        }
    }

    fun getWorkingHours(fromUntil: String, time: String): MutableList<Int> {
        val workingHours = mutableListOf<Int>()
        if (fromUntil == "Od") {
            val timeInt = transformHours(time)
            if (timeInt < 10) {
                for (hour in 0..timeInt) {
                    workingHours.add(hour)
                }
            }
        } else {
            val timeInt = transformHours(time)
            //ako je time veci od 4 a manji od 19 onda izbaci sve kafice
            if (timeInt < 8 || timeInt > 19) {
                //ako je time manji od 8 onda izbaci kafice koji rade od time do 4
                if (timeInt < 8) {
                    for (hour in timeInt..8) {
                        workingHours.add(hour)
                    }

                } else if (timeInt > 19) {
                    //ako je time veci od 19 izbaci sve koji rade od 19 do 23 i od 0 do 4
                    for (hour in timeInt..23) {
                        workingHours.add(hour)
                    }
                    for (hour in 0..4) {
                        workingHours.add(hour)
                    }
                }
            }
        }
        return if (workingHours.isNotEmpty()) {
            workingHours
        } else {
            mutableListOf(-1)
        }
    }

    fun transformHours(time: String) = time.substring(0, 2).toInt()


    fun getHours(): MutableList<String> {
        val hours = mutableListOf("All")
        for (i in 0..23) {
            hours.add(if (i < 10) "0$i:00" else "$i:00")
        }
        return hours
    }

    private val _onCafeBarClickStatus = MutableLiveData<Event<Resource<CafeBar>>>()
    val onCafeBarClickStatus: LiveData<Event<Resource<CafeBar>>> = _onCafeBarClickStatus

    fun onCafeBarClick(cafeBar: CafeBar) = viewModelScope.launch {
        if (cafeBar._id.isEmpty()) {
            _onCafeBarClickStatus.postValue(Event(Resource.error("Invalid id", null)))
            return@launch
        }
        _onCafeBarClickStatus.postValue(Event(Resource.success(cafeBar)))
        cafeBarEventsChannel.send(CafeBarsEvents.NavigateToCafeBarFragment(cafeBar))
    }


    sealed class CafeBarsEvents {
        data class ShowMessage(val message: String) : CafeBarsEvents()
        data class SetKvartoviAdapter(val kvartovi: List<String>) : CafeBarsEvents()
        data class NavigateToCafeBarFragment(val cafeBar: CafeBar) : CafeBarsEvents()
    }

}

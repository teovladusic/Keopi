package com.techpuzzle.keopi.ui.caffebars

import androidx.lifecycle.*
import androidx.paging.*
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.firebase.messaging.FirebaseMessaging
import com.techpuzzle.keopi.data.entities.Area
import com.techpuzzle.keopi.data.entities.CafeBar
import com.techpuzzle.keopi.data.entities.CafeParams
import com.techpuzzle.keopi.data.entities.City
import com.techpuzzle.keopi.data.paging.CafesPagingSource
import com.techpuzzle.keopi.data.repositiories.cafebars.CafeBarsRepository
import com.techpuzzle.keopi.utils.Event
import com.techpuzzle.keopi.utils.Resource
import com.techpuzzle.keopi.utils.Status
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import java.io.IOException
import javax.inject.Inject
import kotlin.random.Random

@HiltViewModel
class CafeBarsViewModel @Inject constructor(
    private val repository: CafeBarsRepository,
    state: SavedStateHandle
) : ViewModel() {

    private val TAG = "CafeBarsViewModel"

    private val cafeBarEventsChannel = Channel<CafeBarsEvents>()
    val cafeBarsEvents = cafeBarEventsChannel.receiveAsFlow()

    val searchQueryLiveData = state.getLiveData("searchQuery", "")
    private val searchQuery = searchQueryLiveData.asFlow()

    private val cafeParamsLiveData = state.getLiveData("cafeParams", CafeParams())
    private val cafeParams = cafeParamsLiveData.asFlow()

    val promoCafeBarsFlow = MutableStateFlow(listOf<CafeBar>())

    private var _getPromoBarsStatus = MutableLiveData<Event<Resource<List<CafeBar>>>>()
    val getPromoBarsStatus: LiveData<Event<Resource<List<CafeBar>>>> = _getPromoBarsStatus

    fun loadPromoBars() = viewModelScope.launch {
        val promoBars = repository.getPromoBars()
        if (promoBars.status == Status.SUCCESS) {
            promoCafeBarsFlow.value = promoBars.data!!
        } else {
            promoCafeBarsFlow.value = emptyList()
        }
        _getPromoBarsStatus.postValue(Event(promoBars))
    }

    var lastCafeBars: PagingData<CafeBar>? = null

    @ExperimentalCoroutinesApi
    val cafeBarsFlow = combine(
        cafeParams,
        searchQuery
    ) { cafeParams, searchQuery ->
        Pair(cafeParams, searchQuery)
    }.flatMapLatest { (cafeParams, searchQuery) ->
        withContext(Dispatchers.IO) {
            cafeParams.name = searchQuery
            getSearchResults(cafeParams)
                .cachedIn(viewModelScope)
        }
    }

    fun loadCafeBars() = viewModelScope.launch {
        //refresh search query
        val realQuery = searchQueryLiveData.value ?: ""
        searchQueryLiveData.postValue("a")
        delay(5)
        searchQueryLiveData.postValue(realQuery)
    }

    private fun getSearchResults(cafeParams: CafeParams): Flow<PagingData<CafeBar>> {
        return Pager(
            config = PagingConfig(
                pageSize = 5,
            ),
            pagingSourceFactory = { CafesPagingSource(createCafeParamsMap(cafeParams)) }
        ).flow
    }

    private val seed = Random.nextInt(0, 1000).toString()
    private fun createCafeParamsMap(cafeParams: CafeParams): MutableMap<String, String> {
        val mapToReturn = mutableMapOf<String, String>()

        if (cafeParams.billiards != null) {
            mapToReturn["billiards"] = cafeParams.billiards!!.toString()
        }
        if (cafeParams.cityId != null) {
            mapToReturn["cityId"] = cafeParams.cityId!!
        }
        if (cafeParams.name != null) {
            mapToReturn["name"] = cafeParams.name!!
        }
        if (cafeParams.capacity != null) {
            mapToReturn["capacity"] = cafeParams.capacity!!
        }
        if (cafeParams.betting != null) {
            mapToReturn["betting"] = cafeParams.betting!!.toString()
        }
        if (cafeParams.areaId != null) {
            mapToReturn["areaId"] = cafeParams.areaId!!
        }
        if (cafeParams.location != null) {
            mapToReturn["location"] = cafeParams.location!!
        }
        if (cafeParams.music != null) {
            mapToReturn["music"] = cafeParams.music!!
        }
        if (cafeParams.dart != null) {
            mapToReturn["dart"] = cafeParams.dart!!.toString()
        }
        if (cafeParams.startingWorkTime != null) {
            mapToReturn["startingWorkTime"] = cafeParams.startingWorkTime!!.toString()
        }
        if (cafeParams.age != null) {
            mapToReturn["age"] = cafeParams.age!!
        }
        if (cafeParams.smoking != null) {
            mapToReturn["smoking"] = cafeParams.smoking!!.toString()
        }
        if (cafeParams.endingWorkTime != null) {
            mapToReturn["endingWorkTime"] = cafeParams.endingWorkTime!!.toString()
        }
        if (cafeParams.terrace != null) {
            mapToReturn["terrace"] = cafeParams.terrace!!.toString()
        }
        if (cafeParams.hookah != null) {
            mapToReturn["hookah"] = cafeParams.hookah!!.toString()
        }
        if (cafeParams.playground != null) {
            mapToReturn["playground"] = cafeParams.playground!!.toString()
        }
        if (cafeParams.pageSize != null) {
            mapToReturn["pageSize"] = cafeParams.pageSize!!.toString()
        }
        mapToReturn["seed"] = seed

        return mapToReturn
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
        if (cafeBar.id.isEmpty()) {
            _addCafeBarStatus.postValue(Event(Resource.error("Cafe bar id is not valid", null)))
            return@launch
        }
        try {
            FirebaseMessaging.getInstance().subscribeToTopic("/topics/${cafeBar.id}")
            repository.insertCafeBar(cafeBar)
            _addCafeBarStatus.postValue(Event(Resource.success(cafeBar)))
            cafeBarEventsChannel.send(CafeBarsEvents.ShowMessage("Saved"))
        } catch (e: IOException) {
            _addCafeBarStatus.postValue(Event(Resource.error("Item has not been added", null)))
            cafeBarEventsChannel.send(CafeBarsEvents.ShowMessage("An error occurred"))
        }
    }

    private val _removeCafeBarStatus = MutableLiveData<Event<Resource<CafeBar>>>()
    val removeCafeBarStatus: LiveData<Event<Resource<CafeBar>>> = _removeCafeBarStatus

    fun removeCafeBar(cafeBar: CafeBar) = viewModelScope.launch {
        if (cafeBar.id.isEmpty()) {
            _removeCafeBarStatus.postValue(Event(Resource.error("Cafe bar id is not valid", null)))
            return@launch
        }
        try {
            repository.deleteCafeBar(cafeBar)
            FirebaseMessaging.getInstance().unsubscribeFromTopic("/topics/${cafeBar.id}")
            _removeCafeBarStatus.postValue(Event(Resource.success(null)))
        } catch (e: IOException) {
            _removeCafeBarStatus.postValue(Event(Resource.error("Item has not been deleted", null)))
            cafeBarEventsChannel.send(CafeBarsEvents.ShowMessage("An error occurred"))
        }
    }

    val cities = MutableStateFlow<List<City>>(emptyList())

    private val _loadCitiesStatus = MutableLiveData<Event<Resource<List<City>>>>()
    val loadCitiesStatus: LiveData<Event<Resource<List<City>>>> = _loadCitiesStatus

    fun loadCities() = viewModelScope.launch {
        if (cities.value.isNotEmpty()) {
            return@launch
        }
        val loadedCities = repository.getAllCities().data!!
        cities.value = loadedCities
        if (loadedCities.isEmpty()) {
            _loadCitiesStatus.postValue(Event(Resource.error("cities cant be empty", null)))
        } else {
            _loadCitiesStatus.postValue(Event(Resource.success(loadedCities)))
        }
    }

    var bottomSheetState = BottomSheetBehavior.STATE_HIDDEN

    val arrayOdDo = arrayOf("All", "From", "Until")

    private var mMusicType = "All"
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
        musicType: String,
        fromUntil: String,
        time: String,
        canSmoke: Boolean,
        hasDart: Boolean,
        hasSlotMachine: Boolean,
        hasBilliard: Boolean,
        cityPosition: Int,
        areaPosition: Int,
        age: String,
        capacity: String
    ) = viewModelScope.launch {
        try {
            mMusicType = musicType
            mCanSmoke = canSmoke
            mHasDart = hasDart
            mHasSlotMachine = hasSlotMachine
            mHasBilliard = hasBilliard

            val newCafeParams = CafeParams()

            if (areaPosition != -1 && areaPosition != 0 && cityPosition != 0) {
                _isQueriedByArea.postValue(true)
                val areaId = areas[areaPosition - 1].id
                newCafeParams.areaId = areaId
            } else _isQueriedByArea.postValue(false)
            if (cityPosition != 0 && (areaPosition == 0 || areaPosition == -1)) {
                _isQueriedByCity.postValue(true)
                val cityId = cities.value[cityPosition - 1].id
                newCafeParams.cityId = cityId
            } else _isQueriedByCity.postValue(false)
            if (fromUntil != "All" && time != "All") {
                _isQueriedByWorkingTime.postValue(true)
                if (fromUntil == "From") {
                    newCafeParams.startingWorkTime = transformHours(time)
                    newCafeParams.endingWorkTime = null
                } else {
                    newCafeParams.endingWorkTime = transformHours(time)
                    newCafeParams.startingWorkTime = null
                }
            } else _isQueriedByWorkingTime.postValue(false)
            if (age != "All") {
                _isQueriedByAge.postValue(true)
                newCafeParams.age = age
            } else _isQueriedByAge.postValue(false)
            if (capacity != "All") {
                _isQueriedByPeopleCapacity.postValue(true)
                newCafeParams.capacity = capacity
            } else _isQueriedByPeopleCapacity.postValue(false)
            if (musicType != "All") {
                _isQueriedByMusic.postValue(true)
                newCafeParams.music = musicType
            } else _isQueriedByMusic.postValue(false)
            if (canSmoke) {
                _isQueriedBySmoking.postValue(true)
                newCafeParams.smoking = true
            } else _isQueriedBySmoking.postValue(false)
            if (hasDart) {
                _isQueriedByDart.postValue(true)
                newCafeParams.dart = true
            } else _isQueriedByDart.postValue(false)
            if (hasSlotMachine) {
                _isQueriedBySlotMachine.postValue(true)
                newCafeParams.betting = true
            } else _isQueriedBySlotMachine.postValue(false)
            if (hasBilliard) {
                _isQueriedByBilliard.postValue(true)
                newCafeParams.billiards = true
            } else _isQueriedByBilliard.postValue(false)
            _setFilterQueryStatus.postValue(Event(Resource.success(null)))

            cafeParamsLiveData.postValue(newCafeParams)

        } catch (e: Exception) {
            _setFilterQueryStatus.postValue(Event(Resource.error("${e.message}", null)))
        }
    }

    private var areas = listOf<Area>()

    private val _loadAreasStatus = MutableLiveData<Event<Resource<List<Area>>>>()
    val loadAreaStatus: LiveData<Event<Resource<List<Area>>>> = _loadAreasStatus

    fun loadAreas(cityPosition: Int) = viewModelScope.launch {
        try {
            val cityId = cities.value[cityPosition - 1].id
            areas = repository.getAreasByCityId(cityId).data ?: emptyList()
            _loadAreasStatus.postValue(Event(Resource.success(areas)))
            val mAreas = mutableListOf("All")
            mAreas.addAll(areas.map { it.name })
            cafeBarEventsChannel.send(CafeBarsEvents.SetAreasAdapter(mAreas))
        } catch (e: Exception) {
            _loadAreasStatus.postValue(Event(Resource.error("An error occurred", null)))
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
        if (cafeBar.id.isEmpty()) {
            _onCafeBarClickStatus.postValue(Event(Resource.error("Invalid id", null)))
            return@launch
        }
        _onCafeBarClickStatus.postValue(Event(Resource.success(cafeBar)))
        cafeBarEventsChannel.send(CafeBarsEvents.NavigateToCafeBarFragment(cafeBar))
    }


    sealed class CafeBarsEvents {
        data class ShowMessage(val message: String) : CafeBarsEvents()
        data class SetAreasAdapter(val areas: List<String>) : CafeBarsEvents()
        data class NavigateToCafeBarFragment(val cafeBar: CafeBar) : CafeBarsEvents()
    }
}

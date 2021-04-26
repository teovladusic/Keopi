package com.techpuzzle.keopi.ui.cafebar

import android.util.Log
import androidx.lifecycle.*
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.SphericalUtil
import com.techpuzzle.keopi.data.entities.CafeBar
import com.techpuzzle.keopi.data.repositiories.cafebar.CafeBarRepository
import com.techpuzzle.keopi.utils.Event
import com.techpuzzle.keopi.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CafeBarViewModel @Inject constructor(
    private val state: SavedStateHandle,
    private val repository: CafeBarRepository
) : ViewModel() {

    private val TAG = "CafeBarViewModel"

    val cafeBar = state.get<CafeBar>("cafeBar")!!

    val imageUrls = state.get<Array<String>>("imageUrls")?.toList() ?: emptyList()

    var isStartPositionUsed = false
    val startPosition = state.get<Int>("position") ?: 0
    var position = startPosition

    private val cafeBarEventsChannel = Channel<CafeBarEvents>()
    val cafeBarEvents = cafeBarEventsChannel.receiveAsFlow()

    val imageUrlsFlow = MutableStateFlow(imageUrls)

    private val _loadImageUrlsStatus = MutableLiveData<Event<Resource<List<String>>>>()
    val loadImageUrlsStatus: LiveData<Event<Resource<List<String>>>> = _loadImageUrlsStatus

    fun loadImageUrls(cafeBarId: String) = viewModelScope.launch {
        val mImageUrls = if (imageUrls.isEmpty()) {
            repository.getImageUrlsByCafeId(cafeBarId)
        } else {
            imageUrls
        }
        mImageUrls?.let {
            imageUrlsFlow.value = it
        }
        if (mImageUrls == null) {
            _loadImageUrlsStatus.postValue(Event(Resource.error("no such image urls found", null)))
        } else {
            _loadImageUrlsStatus.postValue(Event(Resource.success(mImageUrls)))
        }
    }

    private val _insertCafeBarStatus = MutableLiveData<Event<Resource<CafeBar>>>()
    val insertCafeBarStatus: LiveData<Event<Resource<CafeBar>>> = _insertCafeBarStatus

    /** TESTIRAJ **/
    fun verifyLocation(latitude: Double, longitude: Double) = viewModelScope.launch {
        Log.d(TAG, "user $latitude $longitude ")
        val userLatLng = LatLng(latitude, longitude)
        val cafeBarLatLng = LatLng(cafeBar.latitude.toDouble(), cafeBar.longitude.toDouble())
        val distance = SphericalUtil.computeDistanceBetween(userLatLng, cafeBarLatLng)
        Log.d(TAG, "$distance")
        //TODO: OKRENI ZNAK JEDNAKOSTI OVO SAN STAVIA SAMO DA MOGU UC U KAFIC
        if (distance > 30) {
            Log.d(TAG, "pusti ga da naruci")
            cafeBarEventsChannel.send(CafeBarEvents.NavigateToOrderScreen(cafeBar))
        } else {
            cafeBarEventsChannel.send(CafeBarEvents.SendMessage("Trebate biti u kaficu (ukoliko jeste, pokusajte ponovno)"))
        }
    }

    fun onAddClicked(cafeBar: CafeBar) = viewModelScope.launch {
        if (cafeBar._id.isEmpty()) {
            _insertCafeBarStatus.postValue(
                Event(
                    Resource.error(
                        "caffe bar id can't be empty",
                        cafeBar
                    )
                )
            )
            return@launch
        }
        repository.insertCafeBar(cafeBar)
        _insertCafeBarStatus.postValue(Event(Resource.success(cafeBar)))
        cafeBarEventsChannel.send(CafeBarEvents.SendMessage("Uspješno spremljeno"))
    }

    sealed class CafeBarEvents {
        data class SendMessage(val message: String) : CafeBarEvents()
        data class NavigateToOrderScreen(val cafeBar: CafeBar) : CafeBarEvents()
    }
}
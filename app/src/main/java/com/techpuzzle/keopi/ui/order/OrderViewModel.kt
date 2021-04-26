package com.techpuzzle.keopi.ui.order

import androidx.lifecycle.*
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.techpuzzle.keopi.data.entities.CafeBar
import com.techpuzzle.keopi.data.entities.Drink
import com.techpuzzle.keopi.data.repositiories.order.OrderRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OrderViewModel @Inject constructor(
    private val state: SavedStateHandle,
    private val repository: OrderRepository
) : ViewModel() {

    private val TAG = "OrderViewModel"

    private val cafeBar = state.get<CafeBar>("cafeBar")!!

    private val orderEventsChannel = Channel<OrderEvents>()
    val orderEventsFlow = orderEventsChannel.receiveAsFlow()

    val selectedDrinks = MutableLiveData(mutableListOf<Drink>())

    private var priceList = emptyList<Drink>()

    private val priceListHotDrinks = MutableStateFlow<List<Drink>>(emptyList())
    val priceListHotDrinksLiveData = priceListHotDrinks.asLiveData()

    private val priceListSoftDrinks = MutableStateFlow<List<Drink>>(emptyList())
    val priceListSoftDrinksLiveData = priceListSoftDrinks.asLiveData()

    private val priceListAlcohol = MutableStateFlow<List<Drink>>(emptyList())
    val priceListAlcoholLiveData = priceListAlcohol.asLiveData()

    private val priceListDeserts = MutableStateFlow<List<Drink>>(emptyList())
    val priceListDesertsLiveData = priceListDeserts.asLiveData()

    fun loadPriceList() = viewModelScope.launch {
        if (priceList.isEmpty()) {
            orderEventsChannel.send(OrderEvents.Loading)
            val mPriceList = repository.loadPriceList(cafeBar.cjenikId)
            priceListHotDrinks.value = mPriceList.filter { it.vrsta == 0 }
            priceListSoftDrinks.value = mPriceList.filter { it.vrsta == 1 }
            priceListAlcohol.value = mPriceList.filter { it.vrsta == 2 }
            priceListDeserts.value = mPriceList.filter { it.vrsta == 3 }
            priceList = mPriceList
        }
        orderEventsChannel.send(OrderEvents.Loaded)
    }

    fun onValueChanged(drink: Drink, amount: Int, position: Int) {
        val newList = selectedDrinks.value ?: mutableListOf()
        newList.removeAll { drink.id == it.id }
        for (i in 0 until amount) {
            newList.add(drink)
        }
        selectedDrinks.postValue(newList)
        viewModelScope.launch {
            orderEventsChannel.send(OrderEvents.RefreshAdapter(drink.vrsta, position))
        }
    }

    fun onConfirmClick() {
        if (selectedDrinks.value == null) {
            return
        }
        if (selectedDrinks.value!!.isEmpty()) {
            return
        }
    }

    var flavourPickDialogState = BottomSheetBehavior.STATE_HIDDEN
    var flavourPosition = 0
    var flavourDialogDrink: Drink? = null
    var flavourItemInRecViewPosition = 0

    sealed class OrderEvents {
        data class RefreshAdapter(val drinkType: Int, val position: Int) : OrderEvents()
        object Loading : OrderEvents()
        object Loaded : OrderEvents()
    }
}
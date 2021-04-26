package com.techpuzzle.keopi.ui.checkout

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.techpuzzle.keopi.data.repositiories.checkout.CheckoutRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class CheckoutViewModel @Inject constructor(
    private val state: SavedStateHandle,
    private val repository: CheckoutRepository
): ViewModel() {

    //private val selectedDrinksArray = state.get<Array<Drink>>("selectedDrinks")
}
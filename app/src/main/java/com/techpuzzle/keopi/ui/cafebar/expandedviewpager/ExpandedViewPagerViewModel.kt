package com.techpuzzle.keopi.ui.cafebar.expandedviewpager

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.techpuzzle.keopi.data.entities.CafeBar
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ExpandedViewPagerViewModel @Inject constructor(
    private val state: SavedStateHandle
) : ViewModel() {

    val imageUrls = state.get<Array<String>>("imageUrls")!!.toList()

    private val startPosition = state.get<Int>("position") ?: 0
    var position = startPosition

    val cafeBar = state.get<CafeBar>("cafeBar")!!
}
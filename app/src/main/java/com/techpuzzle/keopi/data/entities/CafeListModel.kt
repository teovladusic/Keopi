package com.techpuzzle.keopi.data.entities

data class CafeListModel(
    val cafes: List<CafeBar>,
    val currentPage: Int,
    val totalPages: Int,
    val pageSize: Int,
    val hasPrevious: Boolean,
    val hasNext: Boolean
)
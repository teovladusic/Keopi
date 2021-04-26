package com.techpuzzle.keopi.data.room

import androidx.lifecycle.LiveData
import androidx.paging.PagingSource
import androidx.room.*
import com.techpuzzle.keopi.data.entities.CafeBar

@Dao
interface KeopiDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCafeBar(cafeBar: CafeBar)

    @Delete
    suspend fun deleteCafeBar(cafeBar: CafeBar)

    @Query("SELECT * FROM CafeBar")
    fun getPagedCafeBars() : PagingSource<Int, CafeBar>

    @Query("SELECT * FROM CafeBar")
    fun getCafeBarsFlow() : LiveData<List<CafeBar>>
}
package com.techpuzzle.keopi.data.paging

import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.techpuzzle.keopi.data.api.RetrofitInstance
import com.techpuzzle.keopi.data.entities.CafeBar

class CafesPagingSource(
    private val cafeParams: MutableMap<String, String>
) : PagingSource<Int, CafeBar>() {


    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, CafeBar> {
        return try {
            val nextPage = params.key ?: 1
            cafeParams["currentPage"] = nextPage.toString()
            cafeParams["pageSize"] = "10"
            val response = RetrofitInstance.api.getCafes(cafeParams)

            Log.d("TAG", "$response")
            Log.d("TAG", "${response.body()} ")

            if (!response.isSuccessful) {
                throw Exception(response.message())
            }
            LoadResult.Page(
                data = response.body()!!.cafes,
                prevKey = if (response.body()!!.hasPrevious) (response.body()!!.currentPage - 1) else null,
                nextKey = if (response.body()!!.hasNext) (response.body()!!.currentPage + 1) else null
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, CafeBar>): Int? = null
}
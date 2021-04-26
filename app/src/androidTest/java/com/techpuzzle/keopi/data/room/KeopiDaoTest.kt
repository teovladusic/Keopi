package com.techpuzzle.keopi.data.room

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.google.common.truth.Truth.assertThat
import com.techpuzzle.keopi.data.entities.CafeBar
import com.techpuzzle.keopi.getOrAwaitValue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@SmallTest
class KeopiDaoTest {

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: KeopiDatabase
    private lateinit var dao: KeopiDao

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            KeopiDatabase::class.java
        ).allowMainThreadQueries().build()
        dao = database.getDao()
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun insertCafeBarTest() = runBlockingTest {
        val cafeBar = CafeBar(name = "aa", _id = "1")
        dao.insertCafeBar(cafeBar)

        val allCafeBars = dao.getCafeBarsFlow().getOrAwaitValue()

        assertThat(allCafeBars.contains(cafeBar))
    }

    @Test
    fun deleteCafeBarTest() = runBlockingTest {
        val cafeBar = CafeBar(name = "aa", _id = "1")
        dao.insertCafeBar(cafeBar)

        dao.deleteCafeBar(cafeBar)

        val allCafeBars = dao.getCafeBarsFlow().getOrAwaitValue()
        assertThat(!allCafeBars.contains(cafeBar))
    }
}
package com.techpuzzle.keopi.ui.cafebar

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.SavedStateHandle
import com.google.common.truth.Truth.assertThat
import com.techpuzzle.keopi.MainCoroutineRule
import com.techpuzzle.keopi.data.entities.CafeBar
import com.techpuzzle.keopi.data.repositories.FakeRepository
import com.techpuzzle.keopi.getOrAwaitValueTest
import com.techpuzzle.keopi.utils.Status
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class CafeBarViewModelTest {

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    private lateinit var viewModel: CafeBarViewModel

    @Before
    fun setup() {
        val savedStateHandle = SavedStateHandle()

        val cafeBar = CafeBar(
            address = "neka nmp",
            billiards = false,
            bio = "Mos slusat misu",
            cjenikId = "vpwCzBBjGsKXEcQIzRn0",
            cityId = "i1HArtxiMAT11rchC74Y",
            _id = "3M6XwZ6rOrLDCnw3KZUb",
            name = "Faraon",
            capacity = "< 50",
            betting = true,
            latitude = "",
            longitude = "",
            areaId = "Twq7rmLSK1ggqK4y1jKM",
            location = "K.Luksic",
            music = "house",
            dart = true,
            startingWorkTime = 8,
            age = "< 22",
            smoking = true,
            picture = "gs://keopi-8467e.appspot.com/images/3M6XwZ6rOrLDCnw3KZUb/faraon.jfif",
            terrace = true,
            endingWorkTime = 23,
            facebook = "",
            instagram = "",
            hookah = false,
            playground = true
        )
        savedStateHandle.set("cafeBar", cafeBar)
        viewModel = CafeBarViewModel(savedStateHandle, FakeRepository())
    }

    @Test
    fun `on add clicked, returns success`() {
        viewModel.onAddClicked(viewModel.cafeBar)
        val value = viewModel.insertCafeBarStatus.getOrAwaitValueTest()
        assertThat(value.getContentIfNotHandled()?.status).isEqualTo(Status.SUCCESS)
    }

    @Test
    fun `on add clicked, cafeBarId = empty string, returns error`() {
        viewModel.onAddClicked(CafeBar())
        val value = viewModel.insertCafeBarStatus.getOrAwaitValueTest()
        assertThat(value.getContentIfNotHandled()?.status).isEqualTo(Status.ERROR)
    }

    @Test
    fun `load image urls test, there is cafeBar with such id, returns success`() {
        val cafeBarId = "1"
        viewModel.loadImageUrls(cafeBarId)
        val value = viewModel.loadImageUrlsStatus.getOrAwaitValueTest()
        assertThat(value.getContentIfNotHandled()?.status).isEqualTo(Status.SUCCESS)
    }

    @Test
    fun `load image urls test, there is no cafeBar with such id, returns error`() {
        val cafeBarId = "noSuchId"
        viewModel.loadImageUrls(cafeBarId)
        val value = viewModel.loadImageUrlsStatus.getOrAwaitValueTest()
        assertThat(value.getContentIfNotHandled()?.status).isEqualTo(Status.ERROR)
    }
}
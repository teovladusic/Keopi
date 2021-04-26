package com.techpuzzle.keopi.ui.caffebars

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.google.common.truth.Truth.assertThat
import com.techpuzzle.keopi.data.entities.CafeBar
import com.techpuzzle.keopi.data.repositiories.cafebars.CafeBarsFakeRepository
import com.techpuzzle.keopi.getOrAwaitValue
import com.techpuzzle.keopi.utils.Status
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@SmallTest
class CafeBarsViewModelTest {

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var viewModel: CafeBarsViewModel

    @Before
    fun setup() {
        viewModel = CafeBarsViewModel(CafeBarsFakeRepository())
    }


    @Test
    fun getPromoBarsReturnsSuccess() {
        viewModel.loadPromoBars()
        val value = viewModel.getPromoBarsStatus.getOrAwaitValue()
        assertThat(value.getContentIfNotHandled()?.status).isEqualTo(Status.SUCCESS)
    }

    @Test
    fun addCafeBarReturnsSuccess() {
        val caffeBar = CafeBar(_id = "valid_id")
        viewModel.addCafeBar(caffeBar)
        val value = viewModel.addCafeBarStatus.getOrAwaitValue()
        assertThat(value.getContentIfNotHandled()?.status).isEqualTo(Status.SUCCESS)
    }

    @Test
    fun addCafeBarWithInvalidIdReturnsError() {
        val caffeBar = CafeBar()
        viewModel.addCafeBar(caffeBar)
        val value = viewModel.addCafeBarStatus.getOrAwaitValue()
        assertThat(value.getContentIfNotHandled()?.status).isEqualTo(Status.ERROR)
    }

    @Test
    fun removeCafeBarReturnsSuccess() {
        val caffeBar = CafeBar(_id = "valid_id")
        viewModel.addCafeBar(caffeBar)
        viewModel.removeCafeBar(caffeBar)
        val value = viewModel.removeCafeBarStatus.getOrAwaitValue()
        assertThat(value.getContentIfNotHandled()?.status).isEqualTo(Status.SUCCESS)
    }

    @Test
    fun removeCafeBarWithInvalidIdReturnsError() {
        val caffeBar = CafeBar()
        viewModel.addCafeBar(caffeBar)
        viewModel.removeCafeBar(caffeBar)
        val value = viewModel.removeCafeBarStatus.getOrAwaitValue()
        assertThat(value.getContentIfNotHandled()?.status).isEqualTo(Status.ERROR)
    }

    @Test
    fun addRemoveCafeBarShouldAddReturnsSuccess() {
        val caffeBar = CafeBar(_id = "valid_id")
        viewModel.onAddRemoveBarClick(CafeBarsFragment.HostingFragments.FirestoreCafeBars, caffeBar)
        val value = viewModel.addCafeBarStatus.getOrAwaitValue()
        assertThat(value.getContentIfNotHandled()?.status).isEqualTo(Status.SUCCESS)
    }

    @Test
    fun addRemoveCafeBarWithInvalidIdShouldAddReturnsError() {
        val caffeBar = CafeBar()
        viewModel.onAddRemoveBarClick(CafeBarsFragment.HostingFragments.FirestoreCafeBars, caffeBar)
        val value = viewModel.addCafeBarStatus.getOrAwaitValue()
        assertThat(value.getContentIfNotHandled()?.status).isEqualTo(Status.ERROR)
    }

    @Test
    fun addRemoveCafeBarShouldRemoveReturnsSuccess() {
        val caffeBar = CafeBar(_id = "valid_id")
        viewModel.addCafeBar(caffeBar)
        viewModel.onAddRemoveBarClick(CafeBarsFragment.HostingFragments.CachedCafeBars, caffeBar)
        val value = viewModel.removeCafeBarStatus.getOrAwaitValue()
        assertThat(value.getContentIfNotHandled()?.status).isEqualTo(Status.SUCCESS)
    }

    @Test
    fun addRemoveCafeBarWithInvalidIdShouldRemoveReturnsError() {
        val caffeBar = CafeBar()
        viewModel.addCafeBar(caffeBar)
        viewModel.onAddRemoveBarClick(CafeBarsFragment.HostingFragments.CachedCafeBars, caffeBar)
        val value = viewModel.removeCafeBarStatus.getOrAwaitValue()
        assertThat(value.getContentIfNotHandled()?.status).isEqualTo(Status.ERROR)
    }

    @Test
    fun loadCitiesReturnsSuccess() {
        viewModel.loadCities()
        val value = viewModel.loadCitiesStatus.getOrAwaitValue()
        assertThat(value.getContentIfNotHandled()?.status).isEqualTo(Status.SUCCESS)
    }

    @Test
    fun setKvartQueryKvartPos1ReturnsTrue() {
        val cityPosition = 1
        val kvartPosition = 1
        viewModel.loadCities()
        viewModel.loadAreas(cityPosition)
        viewModel.setFilerQuery("", "Od", "Sve", false, false, false, false, cityPosition, kvartPosition, "", "")
        val value = viewModel.isQueriedByArea.getOrAwaitValue()
        assertThat(value).isTrue()
    }
    @Test
    fun setKvartQueryKvartPos0ReturnsFalse() {
        val cityPosition = 1
        val kvartPosition = 0
        viewModel.loadCities()
        viewModel.setFilerQuery("", "Od", "Sve", false, false, false, false, cityPosition, kvartPosition, "", "")
        val value = viewModel.isQueriedByArea.getOrAwaitValue()
        assertThat(value).isFalse()
    }

    @Test
    fun setKvartQueryKvartPosMinus1ReturnsFalse() {
        val cityPosition = 1
        val kvartPosition = -1
        viewModel.loadCities()
        viewModel.setFilerQuery("", "Od", "Sve", false, false, false, false, cityPosition, kvartPosition, "", "")
        val value = viewModel.isQueriedByArea.getOrAwaitValue()
        assertThat(value).isFalse()
    }

    @Test
    fun setCityQueryCityPos1KvartPos0ReturnsTrue() {
        val cityPosition = 1
        val kvartPosition = 0
        //function from fake repository returns 3 cities
        viewModel.loadCities()
        viewModel.setFilerQuery("Sve", "Od", "Sve", false, false, false, false, cityPosition, kvartPosition, "Sve", "Sve")
        val value = viewModel.isQueriedByCity.getOrAwaitValue()
        assertThat(value).isTrue()
    }
    @Test
    fun setCityQueryCityPos1KvartPos1ReturnsFalse() {
        val cityPosition = 1
        val kvartPosition = 1
        //function from fake repository returns 3 cities
        viewModel.loadCities()
        viewModel.loadAreas(cityPosition)
        viewModel.setFilerQuery("", "Od", "Sve", false, false, false, false, cityPosition, kvartPosition, "", "")
        val value = viewModel.isQueriedByCity.getOrAwaitValue()
        assertThat(value).isFalse()
    }
    @Test
    fun setCityQueryCityPos0KvartPos0ReturnsFalse() {
        val cityPosition = 0
        val kvartPosition = 0
        //function from fake repository returns 3 cities
        viewModel.loadCities()
        viewModel.setFilerQuery("", "Od", "Sve", false, false, false, false, cityPosition, kvartPosition, "", "")
        val value = viewModel.isQueriedByCity.getOrAwaitValue()
        assertThat(value).isFalse()
    }

    @Test
    fun setFromUntilQueryFromUntilTrueTimeTrueReturnsTrue() {
        viewModel.setFilerQuery("", "Od", "08:00", false, false, false, false, 0, 0, "", "")
        val value = viewModel.isQueriedByWorkingTime.getOrAwaitValue()
        assertThat(value).isTrue()
    }

    @Test
    fun setFromUntilQueryFromUntilTrueTimeTrueTimeNotInScopeWorkingFromReturnsFalse() {
        viewModel.setFilerQuery("", "Od", "15:00", false, false, false, false, 0, 0, "", "")
        val value = viewModel.isQueriedByWorkingTime.getOrAwaitValue()
        assertThat(value).isFalse()
    }
    @Test
    fun setFromUntilQueryFromUntilTrueTimeTrueTimeNotInScopeWorkingUntilReturnsFalse() {
        viewModel.setFilerQuery("", "Do", "16:00", false, false, false, false, 0, 0, "", "")
        val value = viewModel.isQueriedByWorkingTime.getOrAwaitValue()
        assertThat(value).isFalse()
    }

    @Test
    fun setFromUntilQueryFromUntilTrueTimeFalseReturnsFalse() {
        viewModel.setFilerQuery("", "Od", "Sve", false, false, false, false, 0, 0, "", "")
        val value = viewModel.isQueriedByWorkingTime.getOrAwaitValue()
        assertThat(value).isFalse()
    }
    @Test
    fun setFromUntilQueryFromUntilFalseTimeTrueReturnsFalse() {
        viewModel.setFilerQuery("", "Sve", "15:00", false, false, false, false, 0, 0, "", "")
        val value = viewModel.isQueriedByWorkingTime.getOrAwaitValue()
        assertThat(value).isFalse()
    }
    @Test
    fun setFromUntilQueryFromUntilFalseTimeFalseReturnsFalse() {
        viewModel.setFilerQuery("Sve", "Sve", "Sve", false, false, false, false, 0, 0, "Sve", "Sve")
        val value = viewModel.isQueriedByWorkingTime.getOrAwaitValue()
        assertThat(value).isFalse()
    }

    @Test
    fun setAgeQueryReturnsTrue() {
        viewModel.setFilerQuery("Sve", "Sve", "Sve", false, false, false, false, 0, 0, "< 22", "")
        val value = viewModel.isQueriedByAge.getOrAwaitValue()
        assertThat(value).isTrue()
    }
    @Test
    fun setAgeQueryReturnsFalse() {
        viewModel.setFilerQuery("Sve", "Sve", "Sve", false, false, false, false, 0, 0, "Sve", "")
        val value = viewModel.isQueriedByAge.getOrAwaitValue()
        assertThat(value).isFalse()
    }

    @Test
    fun setCapacityQueryReturnsTrue() {
        viewModel.setFilerQuery("Sve", "Sve", "Sve", false, false, false, false, 0, 0, "Sve", "< 50")
        val value = viewModel.isQueriedByPeopleCapacity.getOrAwaitValue()
        assertThat(value).isTrue()
    }
    @Test
    fun setCapacityQueryReturnsFalse() {
        viewModel.setFilerQuery("Sve", "Sve", "Sve", false, false, false, false, 0, 0, "Sve", "Sve")
        val value = viewModel.isQueriedByPeopleCapacity.getOrAwaitValue()
        assertThat(value).isFalse()
    }

    @Test
    fun setMusicQueryReturnsTrue() {
        viewModel.setFilerQuery("Cajke", "Sve", "Sve", false, false, false, false, 0, 0, "Sve", "Sve")
        val value = viewModel.isQueriedByMusic.getOrAwaitValue()
        assertThat(value).isTrue()
    }
    @Test
    fun setMusicQueryReturnsFalse() {
        viewModel.setFilerQuery("Sve", "Sve", "Sve", false, false, false, false, 0, 0, "Sve", "Sve")
        val value = viewModel.isQueriedByMusic.getOrAwaitValue()
        assertThat(value).isFalse()
    }

    @Test
    fun setSmokingQueryReturnsTrue() {
        viewModel.setFilerQuery("Sve", "Sve", "Sve", true, false, false, false, 0, 0, "Sve", "Sve")
        val value = viewModel.isQueriedBySmoking.getOrAwaitValue()
        assertThat(value).isTrue()
    }
    @Test
    fun setSmokingQueryReturnsFalse() {
        viewModel.setFilerQuery("Sve", "Sve", "Sve", false, false, false, false, 0, 0, "Sve", "Sve")
        val value = viewModel.isQueriedBySmoking.getOrAwaitValue()
        assertThat(value).isFalse()
    }

    @Test
    fun setDartQueryReturnsTrue() {
        viewModel.setFilerQuery("Sve", "Sve", "Sve", false, true, false, false, 0, 0, "Sve", "Sve")
        val value = viewModel.isQueriedByDart.getOrAwaitValue()
        assertThat(value).isTrue()
    }
    @Test
    fun setDartQueryReturnsFalse() {
        viewModel.setFilerQuery("Sve", "Sve", "Sve", false, false, false, false, 0, 0, "Sve", "Sve")
        val value = viewModel.isQueriedByDart.getOrAwaitValue()
        assertThat(value).isFalse()
    }

    @Test
    fun setSlotMachineQueryReturnsTrue() {
        viewModel.setFilerQuery("Sve", "Sve", "Sve", false, false, true, false, 0, 0, "Sve", "Sve")
        val value = viewModel.isQueriedBySlotMachine.getOrAwaitValue()
        assertThat(value).isTrue()
    }
    @Test
    fun setSlotMachineQueryReturnsFalse() {
        viewModel.setFilerQuery("Sve", "Sve", "Sve", false, false, false, false, 0, 0, "Sve", "Sve")
        val value = viewModel.isQueriedBySlotMachine.getOrAwaitValue()
        assertThat(value).isFalse()
    }

    @Test
    fun setBilliardQueryReturnsTrue() {
        viewModel.setFilerQuery("Sve", "Sve", "Sve", false, false, false, true, 0, 0, "Sve", "Sve")
        val value = viewModel.isQueriedByBilliard.getOrAwaitValue()
        assertThat(value).isTrue()
    }
    @Test
    fun setBilliardQueryReturnsFalse() {
        viewModel.setFilerQuery("Sve", "Sve", "Sve", false, false, false, false, 0, 0, "Sve", "Sve")
        val value = viewModel.isQueriedByBilliard.getOrAwaitValue()
        assertThat(value).isFalse()
    }

    @Test
    fun setFilterQueryReturnsSuccess() {
        viewModel.setFilerQuery("Sve", "Sve", "Sve", false, false, false, false, 0, 0, "Sve", "Sve")
        val value = viewModel.setFilterQueryStatus.getOrAwaitValue()
        assertThat(value.getContentIfNotHandled()?.status).isEqualTo(Status.SUCCESS)
    }

    @Test
    fun loadKvartoviReturnsSuccess() {
        viewModel.loadCities()
        viewModel.loadAreas(1)
        val value = viewModel.loadAreaStatus.getOrAwaitValue()
        assertThat(value.getContentIfNotHandled()?.status).isEqualTo(Status.SUCCESS)
    }
    @Test
    fun loadKvartoviNoCitiesReturnsError() {
        viewModel.loadAreas(1)
        val value = viewModel.loadAreaStatus.getOrAwaitValue()
        assertThat(value.getContentIfNotHandled()?.status).isEqualTo(Status.ERROR)
    }
    @Test
    fun loadKvartoviCitiesPositionLessThan0ReturnsError() {
        viewModel.loadCities()
        viewModel.loadAreas(0)
        val value = viewModel.loadAreaStatus.getOrAwaitValue()
        assertThat(value.getContentIfNotHandled()?.status).isEqualTo(Status.ERROR)
    }

    @Test
    fun getWorkingHoursFromReturnsTrue() {
        val fromUntil = "Od"
        val time = "08:00"
        val workingHours = viewModel.getWorkingHours(fromUntil, time)
        val shouldReturn = mutableListOf(0, 1, 2, 3, 4, 5, 6, 7, 8)
        assertThat(workingHours).isEqualTo(shouldReturn)
    }
    @Test
    fun getWorkingHoursFromTimeNotInScopeReturnsFalse() {
        val fromUntil = "Od"
        val time = "11:00"
        val workingHours = viewModel.getWorkingHours(fromUntil, time)
        val shouldReturn = mutableListOf(-1)
        assertThat(workingHours).isEqualTo(shouldReturn)
    }

    @Test
    fun getWorkingHoursUntilReturnsTrue() {
        val fromUntil = "Do"
        val time = "22:00"
        val workingHours = viewModel.getWorkingHours(fromUntil, time)
        val shouldReturn = mutableListOf(22, 23, 0, 1, 2, 3, 4)
        assertThat(workingHours).isEqualTo(shouldReturn)
    }
    @Test
    fun getWorkingHoursUntilTimeNotInScopeReturnsListOf() {
        val fromUntil = "Do"
        val time = "18:00"
        val workingHours = viewModel.getWorkingHours(fromUntil, time)
        val shouldReturn = mutableListOf(-1)
        assertThat(workingHours).isEqualTo(shouldReturn)
    }

    @Test
    fun transformHours() {
        val time = "09:00"
        val timeInt = viewModel.transformHours(time)
        assertThat(timeInt).isEqualTo(9)
    }

    @Test
    fun getHours() {
        val hours = viewModel.getHours()
        val shouldReturn = mutableListOf("Sve", "00:00", "01:00", "02:00", "03:00", "04:00", "05:00", "06:00", "07:00", "08:00", "09:00",
            "10:00", "11:00", "12:00", "13:00", "14:00", "15:00", "16:00", "17:00", "18:00", "19:00", "20:00", "21:00", "22:00", "23:00")
        assertThat(hours).isEqualTo(shouldReturn)
    }

    @Test
    fun onCafeBarClickWithValidIdReturnsSuccess() {
        val caffeBar = CafeBar(_id = "valid_id")
        viewModel.onCafeBarClick(caffeBar)
        val value = viewModel.onCafeBarClickStatus.getOrAwaitValue()
        assertThat(value.getContentIfNotHandled()?.status).isEqualTo(Status.SUCCESS)
    }
    @Test
    fun onCafeBarClickWithInvalidIdReturnsError() {
        val caffeBar = CafeBar()
        viewModel.onCafeBarClick(caffeBar)
        val value = viewModel.onCafeBarClickStatus.getOrAwaitValue()
        assertThat(value.getContentIfNotHandled()?.status).isEqualTo(Status.ERROR)
    }
}
package com.techpuzzle.keopi.ui.caffebars

import android.Manifest
import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayoutMediator
import com.techpuzzle.keopi.R
import com.techpuzzle.keopi.data.entities.CafeBar
import com.techpuzzle.keopi.databinding.DialogFilterBinding
import com.techpuzzle.keopi.databinding.FragmentCafebarsBinding
import com.techpuzzle.keopi.ui.caffebars.allcafebars.AllCafeBarsFragment
import com.techpuzzle.keopi.ui.caffebars.cachedcafebars.CachedCafeBarsFragment
import com.techpuzzle.keopi.ui.caffebars.promocafebars.PromoCafesAdapter
import com.techpuzzle.keopi.utils.Permissions
import com.techpuzzle.keopi.utils.connection.ConnectivityManager
import com.techpuzzle.keopi.utils.exhaustive
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import pub.devrel.easypermissions.EasyPermissions
import javax.inject.Inject

@AndroidEntryPoint
class CafeBarsFragment : Fragment(R.layout.fragment_cafebars), SearchView.OnQueryTextListener {

    private val TAG = "CaffeBarsFragment"

    private var _binding: FragmentCafebarsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: CafeBarsViewModel by activityViewModels()

    @ExperimentalCoroutinesApi
    @Inject
    lateinit var connectivityManager: ConnectivityManager

    private lateinit var dialog: BottomSheetDialog
    private var _dialogFilterBinding: DialogFilterBinding? = null
    private val dialogFilterBinding get() = _dialogFilterBinding!!

    private var hasInternetConnection = true

    @ExperimentalCoroutinesApi
    @SuppressLint("NotifyDataSetChanged")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentCafebarsBinding.bind(view)
        requestPermissions()

        val layoutInflater = LayoutInflater.from(requireContext())
        _dialogFilterBinding = DialogFilterBinding.inflate(layoutInflater)

        dialog = BottomSheetDialog(requireContext())
        dialog.setContentView(dialogFilterBinding.root)

        if (viewModel.bottomSheetState == BottomSheetBehavior.STATE_EXPANDED) {
            dialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED
            setUpDialogPositions()
            dialog.show()
        }

        val fragmentList = arrayListOf(
            AllCafeBarsFragment(),
            CachedCafeBarsFragment()
        )

        val viewPagerAdapter = ViewPagerAdapter(
            fragmentList,
            childFragmentManager,
            lifecycle
        )

        binding.viewPager.adapter = viewPagerAdapter

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            when (position) {
                0 -> tab.text = "All cafes"
                1 -> tab.text = "Saved"
            }
        }.attach()

        val listener = object : PromoCafesAdapter.OnItemClickListener {
            override fun onAddCaffeBarClick(cafeBar: CafeBar) {
                viewModel.addCafeBar(cafeBar)
            }

            override fun onBarClick(cafeBar: CafeBar) {
                viewModel.onCafeBarClick(cafeBar)
            }
        }

        val promoCafesAdapter = PromoCafesAdapter(listener)
        binding.recViewPromoCaffes.apply {
            adapter = promoCafesAdapter
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        }

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.promoCafeBarsFlow.collectLatest {
                if (it.isNotEmpty()) {
                    binding.recViewPromoCaffes.apply {
                        promoCafesAdapter.cafes = it
                        promoCafesAdapter.notifyDataSetChanged()
                    }
                }
            }
        }

        dialog.setOnCancelListener {
            viewModel.bottomSheetState = BottomSheetBehavior.STATE_HIDDEN
        }

        binding.apply {
            searchViewCaffeBars.setOnQueryTextListener(this@CafeBarsFragment)
            searchViewCaffeBars.setQuery(viewModel.searchQueryLiveData.value, false)

            cardViewFilter.setOnClickListener {
                viewModel.bottomSheetState = BottomSheetBehavior.STATE_EXPANDED
                dialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED
                setUpDialogPositions()
                dialog.show()
            }

            btnCalendar.setOnClickListener {
                val action = CafeBarsFragmentDirections.actionCaffesFragmentToCalendarFragment()
                findNavController().navigate(action)
            }
        }

        dialogFilterBinding.apply {
            spinnerLokacija.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {

                    if (viewModel.spinnerLocationPosition == spinnerLokacija.selectedItemPosition) {
                        //okret ekrana
                        spinnerKvart.setSelection(viewModel.spinnerAreaPosition)
                    } else {
                        //nije okret ekrana
                        viewModel.spinnerAreaPosition = 0
                    }
                    viewModel.spinnerLocationPosition = spinnerLokacija.selectedItemPosition

                    if (spinnerLokacija.selectedItemPosition == 0) {
                        spinnerKvart.adapter = null
                        viewModel.spinnerAreaPosition = 0
                        spinnerKvart.setBackgroundResource(R.drawable.bottom_sheet_elements_not_selected_elements)
                        spinnerKvart.isEnabled = false
                    } else {
                        spinnerKvart.isEnabled = true
                        spinnerKvart.setBackgroundResource(R.drawable.bottom_sheet_elements_background)

                        viewModel.loadAreas(spinnerLokacija.selectedItemPosition)
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                }
            }
            spinnerKvart.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    viewModel.spinnerAreaPosition = spinnerKvart.selectedItemPosition
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                }
            }

            spinnerSongs.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    viewModel.spinnerSongsPosition = spinnerSongs.selectedItemPosition
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                }
            }
            spinnerPeopleCapacity.onItemSelectedListener =
                object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(
                        parent: AdapterView<*>?,
                        view: View?,
                        position: Int,
                        id: Long
                    ) {
                        viewModel.spinnerPeopleCapacityPosition =
                            spinnerPeopleCapacity.selectedItemPosition
                    }

                    override fun onNothingSelected(parent: AdapterView<*>?) {
                    }
                }
            spinnerAge.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    viewModel.spinnerAgePosition = spinnerAge.selectedItemPosition
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                }
            }
            numberPickerFromUntil.setOnValueChangedListener { _, _, newVal ->
                viewModel.fromUntilPosition = newVal
            }
            numberPickerTime.setOnValueChangedListener { _, _, newVal ->
                viewModel.timePosition = newVal
            }

            checkboxSmoking.setOnClickListener {
                viewModel.mCanSmoke = checkboxSmoking.isChecked
            }
            checkboxPikado.setOnClickListener {
                viewModel.mHasDart = checkboxPikado.isChecked
            }
            checkboxSlotMachine.setOnClickListener {
                viewModel.mHasSlotMachine = checkboxSlotMachine.isChecked
            }
            checkboxBiljar.setOnClickListener {
                viewModel.mHasBilliard = checkboxBiljar.isChecked
            }
            btnTryConnect.setOnClickListener {
                CoroutineScope(Dispatchers.IO).launch {
                    connectivityManager.ping()
                    withContext(Dispatchers.Main) {
                        dialogFilterBinding.progressBarConnecting.visibility = View.VISIBLE
                        delay(1500)
                        dialogFilterBinding.progressBarConnecting.visibility = View.GONE
                    }
                }
            }

            btnSubmitFilters.setOnClickListener {
                val songType = spinnerSongs.selectedItem.toString()
                val canSmoke = checkboxSmoking.isChecked
                val hasDart = checkboxPikado.isChecked
                val hasSlotMachine = checkboxSlotMachine.isChecked
                val hasBilliard = checkboxBiljar.isChecked
                val fromUntil = viewModel.arrayOdDo[numberPickerFromUntil.value]
                val time = viewModel.getHours()[numberPickerTime.value]
                val cityPosition = spinnerLokacija.selectedItemPosition
                val kvartPosition = spinnerKvart.selectedItemPosition
                val age = spinnerAge.selectedItem.toString()
                val capacity = spinnerPeopleCapacity.selectedItem.toString()

                viewModel.setFilerQuery(
                    songType,
                    fromUntil,
                    time,
                    canSmoke,
                    hasDart,
                    hasSlotMachine,
                    hasBilliard,
                    cityPosition,
                    kvartPosition,
                    age,
                    capacity
                )

                dialog.cancel()
            }
        }

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.cafeBarsEvents.collect { event ->
                when (event) {
                    is CafeBarsViewModel.CafeBarsEvents.ShowMessage -> {
                        Snackbar.make(binding.root, event.message, Snackbar.LENGTH_SHORT).show()
                    }
                    is CafeBarsViewModel.CafeBarsEvents.SetAreasAdapter -> {
                        val adapter = ArrayAdapter(
                            requireContext(),
                            android.R.layout.simple_spinner_dropdown_item,
                            event.areas
                        )
                        dialogFilterBinding.spinnerKvart.adapter = adapter
                        dialogFilterBinding.spinnerKvart.setSelection(viewModel.spinnerAreaPosition)
                    }
                    is CafeBarsViewModel.CafeBarsEvents.NavigateToCafeBarFragment -> {
                        val action =
                            CafeBarsFragmentDirections.actionCafesFragmentToCafeBarFragment(event.cafeBar)
                        findNavController().navigate(action)
                    }
                }
            }.exhaustive
        }

        connectivityManager.isNetworkAvailableLiveData.observe(viewLifecycleOwner) {
            hasInternetConnection = it
            if (it) {
                loadData()
            }
        }

    }

    private fun loadData() {
        viewModel.loadCities()
        viewModel.loadPromoBars()
        viewModel.loadCafeBars()
    }

    private fun setUpDialogPositions() {
        dialogFilterBinding.apply {

            if (!hasInternetConnection) {
                dialogFilterBinding.constraintLayoutNoConnection.visibility = View.VISIBLE
                dialogFilterBinding.constraintLayoutConnected.visibility = View.GONE
            } else {
                dialogFilterBinding.constraintLayoutNoConnection.visibility = View.GONE
                dialogFilterBinding.constraintLayoutConnected.visibility = View.VISIBLE
            }

            numberPickerFromUntil.maxValue = 2
            numberPickerFromUntil.minValue = 0
            numberPickerFromUntil.displayedValues = viewModel.arrayOdDo

            numberPickerTime.maxValue = 24
            numberPickerTime.minValue = 0
            numberPickerTime.displayedValues = viewModel.getHours().toTypedArray()

            val cities = mutableListOf("All")
            cities.addAll(viewModel.cities.value.map { it.name })

            val citiesAdapter = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_spinner_dropdown_item,
                cities
            )
            spinnerLokacija.adapter = citiesAdapter

            numberPickerTime.value = viewModel.timePosition
            numberPickerFromUntil.value = viewModel.fromUntilPosition

            checkboxSmoking.isChecked = viewModel.mCanSmoke
            checkboxPikado.isChecked = viewModel.mHasDart
            checkboxSlotMachine.isChecked = viewModel.mHasSlotMachine
            checkboxBiljar.isChecked = viewModel.mHasBilliard

            spinnerSongs.setSelection(viewModel.spinnerSongsPosition)
            spinnerLokacija.setSelection(viewModel.spinnerLocationPosition)
            spinnerPeopleCapacity.setSelection(viewModel.spinnerPeopleCapacityPosition)
            spinnerAge.setSelection(viewModel.spinnerAgePosition)

        }
    }

    private fun requestPermissions() {
        if (Permissions.hasLocationPermission(requireContext())) {
            return
        }
        EasyPermissions.requestPermissions(
            this,
            "Morate dopustiti pristup lokaciji kako bi mogli nastaviti koristi aplikaciju.",
            Permissions.REQUEST_CODE_LOCATION_PERMISSION,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    }

    override fun onQueryTextSubmit(query: String?) = false

    override fun onQueryTextChange(newText: String?): Boolean {
        newText?.let {
            viewModel.searchQueryLiveData.postValue(it)
        }
        return true
    }

    override fun onDestroyView() {
        super.onDestroyView()
        dialog.dismiss()
        _binding = null
        _dialogFilterBinding = null
    }

    sealed class HostingFragments {
        object CachedCafeBars : HostingFragments()
        object FirestoreCafeBars : HostingFragments()
    }

}
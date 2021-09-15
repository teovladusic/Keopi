package com.techpuzzle.keopi.ui.calendar

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.kizitonwose.calendarview.model.CalendarDay
import com.kizitonwose.calendarview.model.CalendarMonth
import com.kizitonwose.calendarview.model.DayOwner
import com.kizitonwose.calendarview.ui.DayBinder
import com.kizitonwose.calendarview.ui.MonthHeaderFooterBinder
import com.kizitonwose.calendarview.ui.MonthScrollListener
import com.kizitonwose.calendarview.ui.ViewContainer
import com.kizitonwose.calendarview.utils.yearMonth
import com.techpuzzle.keopi.R
import com.techpuzzle.keopi.databinding.CalendarDayLayoutBinding
import com.techpuzzle.keopi.databinding.CalendarMonthHeaderLayoutBinding
import com.techpuzzle.keopi.databinding.DialogEventBinding
import com.techpuzzle.keopi.databinding.FragmentCalendarBinding
import com.techpuzzle.keopi.utils.connection.ConnectivityManager
import com.techpuzzle.keopi.utils.exhaustive
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class CalendarFragment : Fragment(R.layout.fragment_calendar), EventsAdapter.OnItemClickListener {

    private val TAG = "CalendarFragment"

    private var _binding: FragmentCalendarBinding? = null
    val binding get() = _binding!!

    private var _dialogEventBinding: DialogEventBinding? = null
    private val dialogEventBinding get() = _dialogEventBinding!!

    private lateinit var dialog: BottomSheetDialog

    private val viewModel: CalendarViewModel by viewModels()

    private lateinit var currentTextView: TextView
    private lateinit var currentImgView: ImageView

    @ExperimentalCoroutinesApi
    @Inject
    lateinit var connectivityManager: ConnectivityManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requireActivity().onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    viewModel.onBackPressed()
                }
            })
    }

    @ExperimentalCoroutinesApi
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentCalendarBinding.bind(view)

        val layoutInflater = LayoutInflater.from(requireContext())
        _dialogEventBinding = DialogEventBinding.inflate(layoutInflater)

        dialog = BottomSheetDialog(requireContext())
        dialog.setContentView(dialogEventBinding.root)

        if (viewModel.cafeBar == null) {
            binding.tvCafeBarName.visibility = View.INVISIBLE
        } else {
            binding.tvCafeBarName.visibility = View.VISIBLE
            binding.tvCafeBarName.text = viewModel.cafeBar?.name
        }

        if (viewModel.bottomSheetState == BottomSheetBehavior.STATE_EXPANDED) {
            viewModel.lastSelectedEvent?.let {
                populateDialog(it)
                dialog.show()
            }
        }

        currentTextView = binding.tvCurrentMonth
        currentImgView = binding.imgCurrentMonth
        setMonthsText()

        viewModel.eventDatesLiveData.observe(viewLifecycleOwner) { eventDates ->
            setupCalendar(eventDates)
        }

        binding.imgBack.setOnClickListener {
            viewModel.onBackPressed()
        }

        dialogEventBinding.btnGoToCafe.setOnClickListener {
            viewModel.lastSelectedEvent?.let {
                val action =
                    CalendarFragmentDirections.actionCalendarFragmentToCafeBarFragment(it.cafeBar)
                findNavController().navigate(action)
            }
        }


        viewModel.eventsTombolaLiveData.observe(viewLifecycleOwner) {
            if (it.isNotEmpty() && it[0].event.id.isNotEmpty()) {
                binding.recViewTombola.visibility = View.VISIBLE
                binding.tvTombula.visibility = View.VISIBLE
                binding.tvNoEvents.visibility = View.GONE
                val tombolaAdapter = EventsAdapter(it, this)
                binding.recViewTombola.apply {
                    adapter = tombolaAdapter
                    layoutManager =
                        LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
                }
            } else {
                binding.recViewTombola.visibility = View.GONE
                binding.tvTombula.visibility = View.GONE
            }
        }

        viewModel.eventsConcertsLiveData.observe(viewLifecycleOwner) {
            if (it.isNotEmpty() && it[0].event.id.isNotEmpty()) {
                binding.recViewConcert.visibility = View.VISIBLE
                binding.tvConcert.visibility = View.VISIBLE
                binding.tvNoEvents.visibility = View.GONE
                val concertsAdapter = EventsAdapter(it, this)
                binding.recViewConcert.apply {
                    adapter = concertsAdapter
                    layoutManager =
                        LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
                }
            } else {
                binding.recViewConcert.visibility = View.GONE
                binding.tvConcert.visibility = View.GONE
            }
        }

        viewModel.eventsPartiesLiveData.observe(viewLifecycleOwner) {
            if (it.isNotEmpty() && it[0].event.id.isNotEmpty()) {
                binding.recViewParty.visibility = View.VISIBLE
                binding.tvParty.visibility = View.VISIBLE
                binding.tvNoEvents.visibility = View.GONE
                val partiesAdapter = EventsAdapter(it, this)
                binding.recViewParty.apply {
                    adapter = partiesAdapter
                    layoutManager =
                        LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
                }
            } else {
                binding.recViewParty.visibility = View.GONE
                binding.tvParty.visibility = View.GONE
            }
        }

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.calendarEvents.collect { event ->
                when (event) {
                    is CalendarViewModel.CalendarEvents.Loading -> {
                        binding.constraintLayoutLoading.visibility = View.VISIBLE
                    }
                    is CalendarViewModel.CalendarEvents.Loaded -> {
                        binding.constraintLayoutLoading.visibility = View.GONE
                    }
                    is CalendarViewModel.CalendarEvents.ShowNoEventsMessage -> {
                        binding.tvNoEvents.visibility = View.VISIBLE
                    }
                    is CalendarViewModel.CalendarEvents.NavigateToCafeBarScreen -> {
                        val action =
                            CalendarFragmentDirections.actionCalendarFragmentToCafeBarFragment(event.cafeBar)
                        findNavController().navigate(action)
                    }
                    is CalendarViewModel.CalendarEvents.NavigateToMainScreen -> {
                        val action =
                            CalendarFragmentDirections.actionCalendarFragmentToCaffesFragment()
                        findNavController().navigate(action)
                    }
                    CalendarViewModel.CalendarEvents.EventLoaded -> {
                        binding.progressBarEvent.visibility = View.GONE
                    }
                    CalendarViewModel.CalendarEvents.EventLoading -> {
                        binding.progressBarEvent.visibility = View.VISIBLE
                    }
                }.exhaustive
            }
        }

        dialog.setOnCancelListener {
            viewModel.bottomSheetState = BottomSheetBehavior.STATE_HIDDEN
        }

        connectivityManager.isNetworkAvailableLiveData.observe(viewLifecycleOwner) {
            if (it) {
                loadData()
            }
        }
    }

    private fun loadData() {
        viewModel.loadEvents()
    }

    override fun onItemClick(eventWithCafeBar: CalendarViewModel.EventWithCafeBar) {
        populateDialog(eventWithCafeBar)
        viewModel.bottomSheetState = BottomSheetBehavior.STATE_EXPANDED
        viewModel.lastSelectedEvent = eventWithCafeBar
        dialog.show()
    }

    private fun populateDialog(eventWithCafeBar: CalendarViewModel.EventWithCafeBar) {
        dialogEventBinding.apply {
            tvCafeName.text = eventWithCafeBar.cafeBar.name
            tvEventTime.text = eventWithCafeBar.event.date.takeLast(2)
            tvMoney.text = eventWithCafeBar.event.price
            if (eventWithCafeBar.event.type == 0) {
                imgMicrophone.visibility = View.VISIBLE
                tvPerformer.visibility = View.VISIBLE
                tvPerformer.text = eventWithCafeBar.event.performer
            } else {
                imgMicrophone.visibility = View.INVISIBLE
                tvPerformer.visibility = View.INVISIBLE
            }
            tvDescription.text = eventWithCafeBar.event.description
        }
    }

    fun setMonthStyle(month: CalendarMonth) {
        binding.apply {
            val textViews = listOf(
                tvCurrentMonth,
                tvMonthMinusOne,
                tvMonthMinusTwo,
                tvMonthPlusOne,
                tvMonthPlusTwo
            )
            when (month.yearMonth) {
                viewModel.currentMonth -> {
                    currentTextView = tvCurrentMonth
                    currentImgView = imgCurrentMonth
                }
                viewModel.currentMonthMinus2 -> {
                    currentTextView = tvMonthMinusTwo
                    currentImgView = imgMonthMinusTwo
                }
                viewModel.currentMonthMinus1 -> {
                    currentTextView = tvMonthMinusOne
                    currentImgView = imgMonthMinusOne
                }
                viewModel.currentMonthPlus2 -> {
                    currentTextView = tvMonthPlusTwo
                    currentImgView = imgMonthPlusTwo
                }
                viewModel.currentMonthPlus1 -> {
                    currentTextView = tvMonthPlusOne
                    currentImgView = imgMonthPlusOne
                }
            }
            for (tv in textViews) {
                if (tv == currentTextView) {
                    tv.setTypeface(tv.typeface, Typeface.BOLD)
                } else {
                    tv.setTypeface(tv.typeface, Typeface.NORMAL)
                }
            }

            val imgViews = listOf(
                imgCurrentMonth,
                imgMonthPlusOne,
                imgMonthMinusOne,
                imgMonthMinusTwo,
                imgMonthPlusTwo
            )
            for (imgView in imgViews) {
                if (imgView == currentImgView) {
                    imgView.visibility = View.VISIBLE
                } else {
                    imgView.visibility = View.INVISIBLE
                }
            }
        }
    }

    private fun setupCalendar(eventDates: List<String>) {
        binding.calendarView.apply {
            setup(
                viewModel.currentMonthMinus2,
                viewModel.currentMonthPlus2,
                viewModel.daysOfWeek.first()
            )
            scrollToMonth(viewModel.currentMonth)
        }

        class DayViewContainer(view: View) : ViewContainer(view) {
            private val calendarDayLayoutBinding = CalendarDayLayoutBinding.bind(view)
            lateinit var day: CalendarDay

            init {
                calendarDayLayoutBinding.root.setOnClickListener {
                    val currentSelection = viewModel.selectedDate

                    if (currentSelection == day.date) {
                        return@setOnClickListener
                    }

                    viewModel.selectedDate = day.date
                    // Reload the newly selected date so the dayBinder is
                    // called and we can ADD the selection background.
                    binding.calendarView.notifyDateChanged(day.date)

                    // We need to also reload the previously selected
                    // date so we can REMOVE the selection background.
                    binding.calendarView.notifyDateChanged(currentSelection)

                    if (day.owner != DayOwner.THIS_MONTH) {
                        binding.calendarView.smoothScrollToMonth(day.date.yearMonth)
                    }

                    //load events
                    CoroutineScope(Dispatchers.IO).launch {
                        viewModel.onDaySelected(day.date)
                    }
                }
            }

            val root = calendarDayLayoutBinding.root
            val tvDay = calendarDayLayoutBinding.tvDay
            val imgViewEvent = calendarDayLayoutBinding.imgViewEvent
        }

        binding.calendarView.dayBinder = object : DayBinder<DayViewContainer> {

            override fun bind(container: DayViewContainer, day: CalendarDay) {
                container.day = day
                container.tvDay.text = day.date.dayOfMonth.toString()

                //check if event is on that day
                //and put dash below it
                val yearInt = day.date.year
                val monthInt = day.date.monthValue
                val dayInt = day.day
                val dayString = if (dayInt < 10) "0${dayInt}" else dayInt.toString()
                val monthString = if (monthInt < 10) "0${monthInt}" else monthInt.toString()
                val dateString = "$dayString.$monthString.$yearInt."
                if (eventDates.contains(dateString)) {
                    container.imgViewEvent.visibility = View.VISIBLE
                }

                if (day.date == viewModel.selectedDate && day.owner == DayOwner.THIS_MONTH) {
                    //set yellow background on selected date
                    container.root.setBackgroundResource(R.drawable.calendar_selected_day_background)
                    container.tvDay.setTextColor(Color.WHITE)

                } else {
                    //remove yellow background from last selected date
                    container.root.background = null
                    if (day.owner == DayOwner.THIS_MONTH) {
                        container.tvDay.setTextColor(
                            ContextCompat.getColor(
                                requireContext(),
                                R.color.gray_text
                            )
                        )
                        if (viewModel.selectedDate == day.date) {
                            container.tvDay.setTextColor(Color.BLACK)
                        }
                    } else {
                        container.tvDay.setTextColor(
                            ContextCompat.getColor(
                                requireContext(),
                                R.color.light_gray_text
                            )
                        )
                    }
                }
            }

            override fun create(view: View): DayViewContainer = DayViewContainer(view)
        }

        class MonthViewContainer(view: View) : ViewContainer(view) {
            private val binding = CalendarMonthHeaderLayoutBinding.bind(view)
            val tvMonth = binding.tvMonth
            val tvYear = binding.tvYear
        }

        binding.calendarView.monthHeaderBinder =
            object : MonthHeaderFooterBinder<MonthViewContainer> {
                override fun bind(container: MonthViewContainer, month: CalendarMonth) {
                    container.apply {
                        tvMonth.text =
                            month.yearMonth.month.name.lowercase(Locale.ROOT)
                                .replaceFirstChar { x -> x.uppercase() }
                        tvYear.text = month.year.toString()
                    }
                }

                override fun create(view: View): MonthViewContainer = MonthViewContainer(view)

            }


        binding.calendarView.monthScrollListener = object : MonthScrollListener {
            override fun invoke(p1: CalendarMonth) {
                setMonthStyle(p1)
            }
        }
    }

    private fun setMonthsText() {
        binding.apply {
            tvMonthMinusTwo.text =
                viewModel.currentMonthMinus2.month.name.substring(0, 3)
                    .lowercase(Locale.ROOT).replaceFirstChar { x -> x.uppercase() }
            tvMonthMinusOne.text =
                viewModel.currentMonthMinus1.month.name.substring(0, 3)
                    .lowercase(Locale.ROOT).replaceFirstChar { x -> x.uppercase() }
            tvCurrentMonth.text = viewModel.currentMonth.month.name.substring(0, 3)
                .lowercase(Locale.ROOT).replaceFirstChar { x -> x.uppercase() }
            tvMonthPlusOne.text = viewModel.currentMonthPlus1.month.name.substring(0, 3)
                .lowercase(Locale.ROOT).replaceFirstChar { x -> x.uppercase() }
            tvMonthPlusTwo.text = viewModel.currentMonthPlus2.month.name.substring(0, 3)
                .lowercase(Locale.ROOT).replaceFirstChar { x -> x.uppercase() }

            tvMonthMinusTwo.setOnClickListener {
                calendarView.smoothScrollToMonth(viewModel.currentMonthMinus2)
            }
            tvMonthMinusOne.setOnClickListener {
                calendarView.smoothScrollToMonth(viewModel.currentMonthMinus1)
            }
            tvCurrentMonth.setOnClickListener {
                calendarView.smoothScrollToMonth(viewModel.currentMonth)
            }
            tvMonthPlusOne.setOnClickListener {
                calendarView.smoothScrollToMonth(viewModel.currentMonthPlus1)
            }
            tvMonthPlusTwo.setOnClickListener {
                calendarView.smoothScrollToMonth(viewModel.currentMonthPlus2)
            }
        }
    }

    override fun onDestroyView() {
        dialog.dismiss()
        _binding = null
        _dialogEventBinding = null
        super.onDestroyView()
    }
}
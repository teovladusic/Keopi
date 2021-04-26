package com.techpuzzle.keopi.ui.order

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.techpuzzle.keopi.R
import com.techpuzzle.keopi.data.entities.Drink
import com.techpuzzle.keopi.databinding.DialogFlavourPickBinding
import com.techpuzzle.keopi.databinding.FragmentOrderBinding
import com.techpuzzle.keopi.utils.exhaustive
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import java.lang.Exception

@AndroidEntryPoint
class OrderFragment : Fragment(R.layout.fragment_order) {

    private var _binding: FragmentOrderBinding? = null
    private val binding get() = _binding!!

    private val viewModel: OrderViewModel by viewModels()

    private lateinit var flavourPickDialog: BottomSheetDialog

    private var hotDrinksAdapter: DrinkItemAdapter? = null
    private var softDrinksAdapter: DrinkItemAdapter? = null
    private var alcoholItemAdapter: DrinkItemAdapter? = null
    private var desertsItemAdapter: DrinkItemAdapter? = null

    private val TAG = "OrderFragment"

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentOrderBinding.bind(view)
        viewModel.loadPriceList()
        flavourPickDialog = BottomSheetDialog(requireContext())

        if (viewModel.flavourPickDialogState != BottomSheetBehavior.STATE_HIDDEN) {
            viewModel.flavourDialogDrink?.let {
                setUpFlavourDialog(it, viewModel.flavourItemInRecViewPosition)
            }
        }

        val listener = object : DrinkItemAdapter.OnItemClickListener {
            override fun onValueChanged(drink: Drink, amount: Int, position: Int) {
                viewModel.onValueChanged(drink, amount, position)
            }

            override fun onAddItemWithMultipleFlavours(drink: Drink, position: Int) {
                setUpFlavourDialog(drink, position)
            }
        }

        viewModel.priceListHotDrinksLiveData.observe(viewLifecycleOwner) {
            if (it.isNotEmpty()) {
                hotDrinksAdapter = DrinkItemAdapter(it, listener)
                binding.recViewHotDrinks.apply {
                    adapter = hotDrinksAdapter
                    layoutManager = LinearLayoutManager(requireContext())
                    setHasFixedSize(true)
                }
            }
        }

        viewModel.priceListSoftDrinksLiveData.observe(viewLifecycleOwner) {
            if (it.isNotEmpty()) {
                softDrinksAdapter = DrinkItemAdapter(it, listener)
                binding.recViewSoftDrinks.apply {
                    adapter = softDrinksAdapter
                    layoutManager = LinearLayoutManager(requireContext())
                    setHasFixedSize(true)
                }
            }
        }

        viewModel.priceListAlcoholLiveData.observe(viewLifecycleOwner) {
            if (it.isNotEmpty()) {
                alcoholItemAdapter = DrinkItemAdapter(it, listener)
                binding.recViewAlcohol.apply {
                    adapter = alcoholItemAdapter
                    layoutManager = LinearLayoutManager(requireContext())
                    setHasFixedSize(true)
                }
            }
        }

        viewModel.priceListDesertsLiveData.observe(viewLifecycleOwner) {
            if (it.isNotEmpty()) {
                desertsItemAdapter = DrinkItemAdapter(it, listener)
                binding.recViewDeserts.apply {
                    adapter = desertsItemAdapter
                    layoutManager = LinearLayoutManager(requireContext())
                    setHasFixedSize(true)
                }
            }
        }

        viewModel.selectedDrinks.observe(viewLifecycleOwner) { selectedDrinks ->
            val totalPrice = selectedDrinks.map { it.cijena.dropLast(2).toInt() }.sum()
            binding.btnPrice.text = "${selectedDrinks.size} pica za $totalPrice,00 Kn"
            if (selectedDrinks.isEmpty()) {
                binding.btnPrice.visibility = View.INVISIBLE
            } else {
                binding.btnPrice.visibility = View.VISIBLE
            }
            hotDrinksAdapter?.selectedDrinks = selectedDrinks
            softDrinksAdapter?.selectedDrinks = selectedDrinks
            alcoholItemAdapter?.selectedDrinks = selectedDrinks
            desertsItemAdapter?.selectedDrinks = selectedDrinks
        }

        binding.btnPrice.setOnClickListener {
            viewModel.onConfirmClick()
        }

        @Suppress("IMPLICIT_CAST_TO_ANY")
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.orderEventsFlow.collect { event ->
                when (event) {
                    is OrderViewModel.OrderEvents.RefreshAdapter -> {
                        try {
                            notifyAdapter(event.drinkType, event.position)
                        } catch (e: Exception) {
                        }
                    }
                    is OrderViewModel.OrderEvents.Loaded -> {
                        binding.apply {
                            progressBar.visibility = View.GONE
                            scrollView.visibility = View.VISIBLE
                        }
                    }
                    is OrderViewModel.OrderEvents.Loading -> {
                        binding.apply {
                            progressBar.visibility = View.VISIBLE
                            scrollView.visibility = View.GONE
                        }
                    }
                }.exhaustive
            }
        }

        flavourPickDialog.setOnCancelListener {
            viewModel.flavourPickDialogState = BottomSheetBehavior.STATE_HIDDEN
        }

        binding.btnPrice.setOnClickListener {
            val action =
                OrderFragmentDirections.actionOrderFragmentToCheckoutFragment(viewModel.selectedDrinks.value!!.toTypedArray())
            findNavController().navigate(action)
        }

        binding.imgBack.setOnClickListener {
            requireActivity().onBackPressed()
        }
    }

    private fun setUpFlavourDialog(drink: Drink, position: Int) {
        val dialogFlavourPickItemBinding = DialogFlavourPickBinding.inflate(layoutInflater)
        flavourPickDialog.setContentView(dialogFlavourPickItemBinding.root)
        lateinit var flavourItemAdapter: FlavourItemAdapter

        viewModel.flavourDialogDrink = drink
        viewModel.flavourItemInRecViewPosition = position

        val flavourItemListener = object : FlavourItemAdapter.OnItemClickListener {
            override fun onCheckboxClick(position: Int) {
                flavourItemAdapter.checkedPosition = position
                viewModel.flavourPosition = position
                flavourItemAdapter.notifyDataSetChanged()
            }
        }

        dialogFlavourPickItemBinding.apply {
            tvDrinkName.text = drink.ime
            tvDrinkPrice.text = "${drink.cijena.dropLast(2)},00 Kn"
            flavourItemAdapter = FlavourItemAdapter(flavourItemListener, drink.okusi)
            flavourItemAdapter.checkedPosition = viewModel.flavourPosition
            recViewFlavours.adapter = flavourItemAdapter
            recViewFlavours.layoutManager = GridLayoutManager(requireContext(), 2)
        }
        viewModel.flavourPickDialogState = BottomSheetBehavior.STATE_EXPANDED
        flavourPickDialog.show()

        dialogFlavourPickItemBinding.btnSubmit.setOnClickListener {
            val newDrink = drink.copy(okus = viewModel.flavourPosition)
            viewModel.selectedDrinks.value?.add(newDrink)
            notifyAdapter(drink.vrsta, position)
            flavourPickDialog.cancel()
        }
    }

    private fun notifyAdapter(drinkType: Int, position: Int) {
        when (drinkType) {
            0 -> hotDrinksAdapter?.notifyItemChanged(position)
            1 -> softDrinksAdapter?.notifyItemChanged(position)
            2 -> alcoholItemAdapter?.notifyItemChanged(position)
            3 -> desertsItemAdapter?.notifyItemChanged(position)
            else -> {
            }
        }
    }

    override fun onDestroyView() {
        flavourPickDialog.dismiss()
        _binding = null
        super.onDestroyView()
    }
}

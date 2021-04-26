package com.techpuzzle.keopi.ui.order

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.RecyclerView
import com.techpuzzle.keopi.data.entities.Drink
import com.techpuzzle.keopi.databinding.DrinkItemBinding

class DrinkItemAdapter(
    private val drinks: List<Drink>,
    private val listener: OnItemClickListener
) : RecyclerView.Adapter<DrinkItemAdapter.DrinkItemViewHolder>() {

    var selectedDrinks = emptyList<Drink>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DrinkItemViewHolder {
        val binding =
            DrinkItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DrinkItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DrinkItemViewHolder, position: Int) {
        val drink = drinks[position]
        holder.bind(drink)
    }

    override fun getItemCount(): Int = drinks.size

    inner class DrinkItemViewHolder(private val binding: DrinkItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.cardViewAdd.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    if (drinks[position].okusi.isEmpty()) {
                        if (binding.editTextAmount.text.toString() != "99") {
                            if (binding.editTextAmount.text.toString().isEmpty()) {
                                listener.onValueChanged(drinks[position], 0, bindingAdapterPosition)
                            } else {
                                val newValue = binding.editTextAmount.text.toString().toInt() + 1
                                listener.onValueChanged(drinks[position], newValue, bindingAdapterPosition)
                            }
                        }
                    } else if (drinks[position].okusi.isNotEmpty()) {
                        listener.onAddItemWithMultipleFlavours(drinks[position], bindingAdapterPosition)
                    } else if (drinks[position].prviArtikli .isNotEmpty() && drinks[position].drugiArtikli.isNotEmpty()) {
                        //TODO napravi nacin da dodes do odabira vise artikala
                    }
                }
            }

            binding.cardViewSubtract.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    if (binding.editTextAmount.text.toString() != "0") {
                        if (binding.editTextAmount.text.toString().isEmpty()) {
                            binding.editTextAmount.setText("0")
                        } else {
                            val newValue = binding.editTextAmount.text.toString().toInt() - 1
                            binding.editTextAmount.setText(newValue.toString())
                        }
                    }
                }
            }

            binding.editTextAmount.doAfterTextChanged {
                if (it.toString().isNotEmpty()) {
                    val newValue = it.toString().toInt()
                    listener.onValueChanged(drinks[bindingAdapterPosition], newValue, bindingAdapterPosition)
                } else {
                    listener.onValueChanged(drinks[bindingAdapterPosition], 0, bindingAdapterPosition)
                }
            }
        }


        fun bind(drink: Drink) {
            binding.apply {
                tvDrinkName.text = drink.ime
                tvDrinkDescription.text = drink.opis
                tvDrinkPrice.text = "${drink.cijena.dropLast(2)},00kn"
                val amount = selectedDrinks.filter { drink.id  == it.id}.size
                editTextAmount.setText(amount.toString())
                if (editTextAmount.text.toString().isNotEmpty()) {
                    editTextAmount.setSelection(editTextAmount.text.length)
                }
                if (drink.okusi.isNotEmpty()) {
                    binding.editTextAmount.isEnabled = false
                }
            }
        }
    }

    interface OnItemClickListener {
        fun onValueChanged(drink: Drink, amount: Int, position: Int)
        fun onAddItemWithMultipleFlavours(drink: Drink, position: Int)
    }
}
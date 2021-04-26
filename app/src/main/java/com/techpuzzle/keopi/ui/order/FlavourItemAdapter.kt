package com.techpuzzle.keopi.ui.order

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.techpuzzle.keopi.databinding.FlavourPickItemBinding

class FlavourItemAdapter(
    private val listener: OnItemClickListener,
    private val flavours: List<String>
) : RecyclerView.Adapter<FlavourItemAdapter.FlavourItemViewHolder>() {

    var checkedPosition: Int = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FlavourItemViewHolder {
        val binding =
            FlavourPickItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FlavourItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FlavourItemViewHolder, position: Int) {
        val flavour = flavours[position]
        holder.bind(flavour)
    }

    override fun getItemCount(): Int = flavours.size

    inner class FlavourItemViewHolder(private val binding: FlavourPickItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        init {
            binding.checkbox.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    listener.onCheckboxClick(position)
                }
            }
        }


        fun bind(flavour: String) {
            binding.tvFlavour.text = flavour
            binding.checkbox.isChecked = checkedPosition == bindingAdapterPosition
        }
    }

    interface OnItemClickListener {
        fun onCheckboxClick(position: Int)
    }
}
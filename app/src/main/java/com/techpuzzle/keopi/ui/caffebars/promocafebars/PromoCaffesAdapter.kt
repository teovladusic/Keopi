package com.techpuzzle.keopi.ui.caffebars.promocafebars

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.storage.FirebaseStorage
import com.techpuzzle.keopi.R
import com.techpuzzle.keopi.data.entities.CafeBar
import com.techpuzzle.keopi.databinding.PromoCafeItemBinding
import com.techpuzzle.keopi.glide.GlideApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PromoCaffesAdapter(
    val listener: OnItemClickListener
) : RecyclerView.Adapter<PromoCaffesAdapter.PromoCaffesViewHolder>() {

    private val firebaseStorage = FirebaseStorage.getInstance()

    private lateinit var context: Context

    var cafes: List<CafeBar> = listOf(CafeBar(), CafeBar(), CafeBar())

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PromoCaffesViewHolder {
        context = parent.context
        val binding =
            PromoCafeItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PromoCaffesViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PromoCaffesViewHolder, position: Int) {
        val cafe = cafes[position]
        holder.bind(cafe)
        holder.loadImage(cafe)
    }

    override fun getItemCount() = cafes.size

    inner class PromoCaffesViewHolder(
        private val binding: PromoCafeItemBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.btnAddBar.setOnClickListener {
                listener.onAddCaffeBarClick(cafes[bindingAdapterPosition])
            }

            binding.root.setOnClickListener {
                listener.onBarClick(cafes[bindingAdapterPosition])
            }
        }

        fun bind(cafeBar: CafeBar) {
            binding.tvCafeName.text = cafeBar.name
            binding.tvLocation.text = cafeBar.location
        }

        fun loadImage(cafeBar: CafeBar) {
            if (cafeBar.picture.isEmpty()) {
                GlideApp.with(context)
                    .load(R.drawable.default_bar_image)
                    .into(binding.imgViewCafe)
            } else {
                CoroutineScope(Dispatchers.IO).launch {
                    val storageReference = firebaseStorage.getReferenceFromUrl(cafeBar.picture)
                    withContext(Dispatchers.Main) {
                        GlideApp.with(context)
                            .load(storageReference)
                            .into(binding.imgViewCafe)
                    }
                }
            }
        }

    }

    interface OnItemClickListener {
        fun onAddCaffeBarClick(cafeBar: CafeBar)
        fun onBarClick(cafeBar: CafeBar)
    }
}
package com.techpuzzle.keopi.ui.cafebar

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.storage.FirebaseStorage
import com.techpuzzle.keopi.R
import com.techpuzzle.keopi.databinding.CafeBarImagePagerItemBinding
import com.techpuzzle.keopi.glide.GlideApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CafeBarImagePagerAdapter(
    private val listener: OnItemClickListener
) : RecyclerView.Adapter<CafeBarImagePagerAdapter.CafeBarImagePagerViewHolder>() {

    private val storage = FirebaseStorage.getInstance()

    private lateinit var context: Context

    var imageUrls = emptyList<String>()

    var isError = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CafeBarImagePagerViewHolder {
        context = parent.context
        val binding =
            CafeBarImagePagerItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CafeBarImagePagerViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CafeBarImagePagerViewHolder, position: Int) {
        if (isError) {
            holder.bindError()
        } else {
            if (imageUrls.isNotEmpty()) {
                val imageUrl = imageUrls[position]
                holder.bind(imageUrl)
            }
        }

    }

    override fun getItemCount() = if (imageUrls.isEmpty()) 1 else imageUrls.size

    inner class CafeBarImagePagerViewHolder(private val binding: CafeBarImagePagerItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        init {
            binding.root.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION && imageUrls.isNotEmpty()) {
                    binding.photoView.visibility = View.VISIBLE
                    binding.imgView.visibility = View.GONE
                    listener.onItemClick()
                }
            }
        }

        fun bind(imageUrl: String) {
            CoroutineScope(Dispatchers.Main).launch {
                val storageReference = storage.getReferenceFromUrl(imageUrl)

                GlideApp.with(context)
                    .load(storageReference)
                    .into(binding.imgView)

                GlideApp.with(context)
                    .load(storageReference)
                    .into(binding.photoView)
            }
        }

        fun bindError() {
            binding.photoView.setImageResource(R.drawable.ic_error)
            binding.imgView.setImageResource(R.drawable.ic_error)
        }
    }

    interface OnItemClickListener {
        fun onItemClick()
    }
}
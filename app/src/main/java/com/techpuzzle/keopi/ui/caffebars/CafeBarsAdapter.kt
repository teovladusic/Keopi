package com.techpuzzle.keopi.ui.caffebars

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.storage.FirebaseStorage
import com.techpuzzle.keopi.R
import com.techpuzzle.keopi.data.entities.CafeBar
import com.techpuzzle.keopi.databinding.CafeBarItemBinding
import com.techpuzzle.keopi.glide.GlideApp
import com.techpuzzle.keopi.utils.exhaustive
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CafeBarsAdapter(
    private val listener: OnItemClickListener
) : PagingDataAdapter<CafeBar, CafeBarsAdapter.CaffeBarViewHolder>(DiffCallback()) {

    lateinit var context: Context

    private val storage = FirebaseStorage.getInstance()

    var hostingFragment: CafeBarsFragment.HostingFragments =
        CafeBarsFragment.HostingFragments.FirestoreCafeBars


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CaffeBarViewHolder {
        context = parent.context
        val binding = CafeBarItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CaffeBarViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CaffeBarViewHolder, position: Int) {
        val cafeBar = getItem(position) ?: return
        holder.bind(cafeBar)
    }

    inner class CaffeBarViewHolder(
        private val binding: CafeBarItemBinding
    ) : RecyclerView.ViewHolder(binding.root) {


        init {
            binding.imgViewAddRemoveBar.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    getItem(position)?.let {
                        listener.onAddRemoveBarClick(hostingFragment, it)
                    }
                }
            }
            binding.root.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    getItem(position)?.let {
                        listener.onItemClick(it)
                    }
                }
            }
        }

        @SuppressLint("SetTextI18n")
        fun bind(cafeBar: CafeBar) {
            CoroutineScope(Dispatchers.Main).launch {
                val storageReference = storage.getReferenceFromUrl(cafeBar.picture)

                GlideApp.with(context)
                    .load(storageReference)
                    .into(binding.imageView)
            }

            binding.tvCaffeName.text = cafeBar.name
            val startTime =
                if (cafeBar.startingWorkTime < 10) "0${cafeBar.startingWorkTime}:00" else "${cafeBar.startingWorkTime}:00"
            val endTime =
                if (cafeBar.endingWorkTime < 10) "0${cafeBar.endingWorkTime}:00" else "${cafeBar.endingWorkTime}:00"
            binding.tvWorkingTime.text = "$startTime $endTime"
            binding.tvLocation.text = cafeBar.location
            binding.tvTables.text = cafeBar.capacity
            when (hostingFragment) {
                is CafeBarsFragment.HostingFragments.CachedCafeBars -> {
                    binding.imgViewAddRemoveBar.setImageResource(R.drawable.ic_minus)
                }
                is CafeBarsFragment.HostingFragments.FirestoreCafeBars -> {
                    binding.imgViewAddRemoveBar.setImageResource(R.drawable.ic_add)
                }
            }.exhaustive
        }
    }

    interface OnItemClickListener {
        fun onAddRemoveBarClick(
            hostingFragments: CafeBarsFragment.HostingFragments,
            cafeBar: CafeBar
        )

        fun onItemClick(cafeBar: CafeBar)
    }

    class DiffCallback : DiffUtil.ItemCallback<CafeBar>() {
        override fun areItemsTheSame(oldItem: CafeBar, newItem: CafeBar) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: CafeBar, newItem: CafeBar) = oldItem == newItem
    }
}
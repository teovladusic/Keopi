package com.techpuzzle.keopi.ui.calendar

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.techpuzzle.keopi.databinding.EventListItemBinding

class EventsAdapter(
    private val eventWithCafeBars: List<CalendarViewModel.EventWithCafeBar>,
    private val listener: OnItemClickListener
) : RecyclerView.Adapter<EventsAdapter.EventsViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventsViewHolder {
        val binding =
            EventListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return EventsViewHolder(binding)
    }

    override fun onBindViewHolder(holder: EventsViewHolder, position: Int) {
        val eventWithCafeBar = eventWithCafeBars[position]
        holder.bind(eventWithCafeBar)
    }

    override fun getItemCount(): Int = eventWithCafeBars.size

    inner class EventsViewHolder(private val binding: EventListItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    listener.onItemClick(eventWithCafeBars[position])
                }
            }
        }

        fun bind(eventWithCafeBar: CalendarViewModel.EventWithCafeBar) {
            binding.tvCafeName.text = eventWithCafeBar.cafeBar.name
            binding.tvTime.text = eventWithCafeBar.event.time
            binding.tvMoney.text = eventWithCafeBar.event.price
        }
    }

    interface OnItemClickListener {
        fun onItemClick(eventWithCafeBar: CalendarViewModel.EventWithCafeBar)
    }
}
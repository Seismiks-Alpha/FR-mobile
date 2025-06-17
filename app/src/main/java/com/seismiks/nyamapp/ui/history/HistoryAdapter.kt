package com.seismiks.nyamapp.ui.history

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.seismiks.nyamapp.R
import com.seismiks.nyamapp.data.remote.response.FoodHistory
import com.seismiks.nyamapp.data.remote.response.HistoryViewItem
import com.seismiks.nyamapp.databinding.RowHistoryBinding
import com.seismiks.nyamapp.utils.DateUtils.toFormattedDateTime

class HistoryAdapter(private val clickListener: (HistoryViewItem) -> Unit) :
    ListAdapter<HistoryViewItem, HistoryAdapter.HistoryViewHolder>(DIFF_CALLBACK) {

    override fun onBindViewHolder(holder: HistoryAdapter.HistoryViewHolder, position: Int) {
        getItem(position)?.let {
            holder.bind(it, clickListener)
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): HistoryViewHolder {
        val binding = RowHistoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return HistoryViewHolder(binding)
    }

    inner class HistoryViewHolder(private val binding: RowHistoryBinding) :
        RecyclerView.ViewHolder(binding.root) {

        private val tvName: TextView = binding.tvHistoryTitle
        private val tvKalori: TextView = binding.tvKalori
        private val tvDate: TextView = binding.tvDate
        private val ivFood: ImageView = binding.ivFood

        fun bind(data: HistoryViewItem, click: (HistoryViewItem) -> Unit) {

            tvName.text = data.foodName
            tvKalori.text = data.calories.toString()
            tvDate.text = data.date.toFormattedDateTime()
            Glide.with(binding.root.context)
                .load("https://nyam.seix.me" + data.imageUrl)
                .placeholder(R.drawable.intro)
                .into(ivFood)

            itemView.setOnClickListener {
                clickListener(data)
            }
        }
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<HistoryViewItem>() {
            override fun areItemsTheSame(oldItem: HistoryViewItem, newItem: HistoryViewItem): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: HistoryViewItem, newItem: HistoryViewItem): Boolean {
                return oldItem == newItem
            }
        }
    }

}
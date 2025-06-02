package com.reza.nyamapp.ui.history

import android.graphics.pdf.models.ListItem
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.reza.nyamapp.R
import com.reza.nyamapp.data.local.ScanResult
import com.reza.nyamapp.databinding.RowHistoryBinding
import com.reza.nyamapp.utils.Date
import com.reza.nyamapp.utils.convertLongToTime

class HistoryAdapter(private val clickListener: (ScanResult) -> Unit) :
    PagingDataAdapter<ScanResult, HistoryAdapter.HistoryViewHolder>(DIFF_CALLBACK) {

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

    inner class HistoryViewHolder(private val binding: RowHistoryBinding) : RecyclerView.ViewHolder(binding.root) {

        private lateinit var scanResult: ScanResult
        private val tvName: TextView = binding.tvHistoryTitle
        private val tvKalori: TextView = binding.tvKalori
        private val tvDate: TextView = binding.tvDate
        private val ivFood: ImageView = binding.ivFood

        fun bind(data: ScanResult, click: (ScanResult) -> Unit) {
            this.scanResult = data

            scanResult.apply {
                tvName.text = foodName
                tvKalori.text = kalori.toString()
                tvDate.text = resultAddedInMillis.convertLongToTime()
                Glide.with(binding.root.context)
                    .load(Uri.parse(image))
                    .placeholder(R.drawable.intro)
                    .into(binding.ivFood)
            }

            itemView.setOnClickListener {
                clickListener(scanResult)
            }
        }

        fun getScanResult(): ScanResult = scanResult
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<ScanResult>() {
            override fun areItemsTheSame(oldItem: ScanResult, newItem: ScanResult): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: ScanResult, newItem: ScanResult): Boolean {
                return oldItem == newItem
            }
        }
    }

}
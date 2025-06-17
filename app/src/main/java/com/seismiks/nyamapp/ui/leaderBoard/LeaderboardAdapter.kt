package com.seismiks.nyamapp.ui.leaderBoard

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Recycler
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.seismiks.nyamapp.R
import com.seismiks.nyamapp.data.local.ScanResult
import com.seismiks.nyamapp.data.remote.response.LeaderboardData
import com.seismiks.nyamapp.data.remote.response.LeaderboardResponse
import com.seismiks.nyamapp.databinding.RowLeaderboardBinding

class LeaderboardAdapter :
    ListAdapter<LeaderboardData, LeaderboardAdapter.LeaderboardViewHolder>(DIFF_CALLBACK) {
    override fun onBindViewHolder(holder: LeaderboardAdapter.LeaderboardViewHolder, position: Int) {
        getItem(position)?.let {
            holder.bind(it)
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): LeaderboardAdapter.LeaderboardViewHolder {
        val binding =
            RowLeaderboardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return LeaderboardViewHolder(binding)
    }

    inner class LeaderboardViewHolder(private val binding: RowLeaderboardBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(data: LeaderboardData) {
            binding.tvUsername.text = data.fullName
            binding.tvRank.text = data.rank.toString()
            binding.tvPoints.text = data.points.toString()
            if (data.photoUrl == null) {
                binding.ivUserProfile.setImageResource(R.drawable.user)
            } else {
                Glide.with(itemView.context)
                    .load(data.photoUrl)
                    .apply(RequestOptions.circleCropTransform())
                    .into(binding.ivUserProfile)
            }
        }
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<LeaderboardData>() {
            override fun areItemsTheSame(
                oldItem: LeaderboardData,
                newItem: LeaderboardData
            ): Boolean {
                return oldItem.fullName == newItem.fullName
            }

            override fun areContentsTheSame(
                oldItem: LeaderboardData,
                newItem: LeaderboardData
            ): Boolean {
                return oldItem == newItem
            }
        }
    }

}
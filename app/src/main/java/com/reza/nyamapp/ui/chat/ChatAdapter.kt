package com.reza.nyamapp.ui.chat

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.reza.nyamapp.R
import com.reza.nyamapp.data.local.Chat
import com.reza.nyamapp.databinding.RowMessageBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ChatAdapter(private val currentUserName: String?) :
    ListAdapter<Chat, ChatAdapter.ChatViewHolder>(DIFF_CALLBACK) {

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        getItem(position)?.let {
            holder.bind(it)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val binding = RowMessageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ChatViewHolder(binding)
    }

    inner class ChatViewHolder(private val binding: RowMessageBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(chat: Chat) {
            binding.tvMessage.text = chat.message
            binding.tvTime.text = chat.timestamp.convertLongToTime()
            binding.tvUser.text = chat.sender
            setChatBubble(chat.sender, binding.cardView)

        }
    }

    fun Long.convertLongToTime(): String {
        val date = Date(this)
        val format = SimpleDateFormat("HH:mm", Locale.getDefault())
        return format.format(date)
    }

    private fun setChatBubble(userName: String?, cardView: CardView) {
        if (currentUserName == userName && userName != null) {
            val layoutParams = cardView.layoutParams as ViewGroup.MarginLayoutParams
            layoutParams.setMargins(100, 0, 0, 20)
            cardView.layoutParams = layoutParams
            cardView.setCardBackgroundColor(ContextCompat.getColor(cardView.context, R.color.yellow))
        } else {
            val layoutParams = cardView.layoutParams as ViewGroup.MarginLayoutParams
            layoutParams.setMargins(0, 0, 100, 20)
            cardView.layoutParams = layoutParams
            cardView.setCardBackgroundColor(ContextCompat.getColor(cardView.context, R.color.white))
        }
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Chat>() {
            override fun areItemsTheSame(oldItem: Chat, newItem: Chat): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: Chat, newItem: Chat): Boolean {
                return oldItem == newItem
            }
        }
    }
}
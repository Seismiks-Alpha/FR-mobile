package com.seismiks.nyamapp.ui.chat

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.seismiks.nyamapp.R
import com.seismiks.nyamapp.ViewModelFactory
import com.seismiks.nyamapp.databinding.ActivityChatBinding
import com.seismiks.nyamapp.utils.AppPreferences.getUserIdFromPreferences

class ChatActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChatBinding
    private lateinit var rvChat: RecyclerView
    private lateinit var adapter: ChatAdapter
    private lateinit var auth: FirebaseAuth

    private lateinit var chatViewModel: ChatViewModel
    private var progressBar: ProgressBar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        val userId = getUserIdFromPreferences(applicationContext)

        setSupportActionBar(binding.myToolbar)
        supportActionBar?.title = getString(R.string.chatbot_text)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        progressBar = findViewById(R.id.progress_bar)

        rvChat = binding.rvChatList
        rvChat.layoutManager = LinearLayoutManager(this)

        adapter = ChatAdapter("User")
        rvChat.adapter = adapter

        val factory = ViewModelFactory.getInstance(this)
        chatViewModel = ViewModelProvider(this, factory).get(ChatViewModel::class.java)

        binding.messageEditText.addTextChangedListener {
            binding.sendButton.isEnabled =
                it?.isNotEmpty() == true && chatViewModel.isLoading.value == false
        }

        chatViewModel.chatMessages.observe(this) { messages ->
            adapter.submitList(messages)
            if (messages.isNotEmpty()) {
                rvChat.scrollToPosition(messages.size - 1)
            }
        }

        chatViewModel.isLoading.observe(this) { isLoading ->
            progressBar?.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.sendButton.isEnabled =
                !isLoading && binding.messageEditText.text?.isNotEmpty() == true
        }

        chatViewModel.errorMessage.observe(this) { errorMessage ->
            errorMessage?.let {
                Toast.makeText(this, it, Toast.LENGTH_LONG).show()
            }
        }

        binding.sendButton.setOnClickListener {
            val messageText = binding.messageEditText.text.toString().trim()
            if (messageText.isNotEmpty()) {
                val currentUser = auth.currentUser
                if (currentUser != null && userId != null) {
                    currentUser.getIdToken(true)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val idToken = task.result.token
                                if (idToken != null) {
                                    chatViewModel.sendMessageGemini(idToken, userId, messageText)
                                    binding.messageEditText.text.clear()
                                } else {
                                    Toast.makeText(
                                        this,
                                        "Gagal mendapatkan token",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    Log.e("ChatActivity", "Gagal mendapatkan token")
                                }
                            } else {
                                Toast.makeText(this, "Gagal mendapatkan token", Toast.LENGTH_SHORT)
                                    .show()
                                Log.e("ChatActivity", "Gagal mendapatkan token", task.exception)
                            }
                        }
                } else {
                    Toast.makeText(this, "User tidak ditemukan", Toast.LENGTH_SHORT).show()
                    Log.e("ChatActivity", "User tidak ditemukan")
                }
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}

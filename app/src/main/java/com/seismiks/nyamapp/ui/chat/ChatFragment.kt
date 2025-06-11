package com.seismiks.nyamapp.ui.chat

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.seismiks.nyamapp.R
import com.seismiks.nyamapp.ViewModelFactory
import com.seismiks.nyamapp.databinding.FragmentChatBinding
import com.seismiks.nyamapp.utils.AppPreferences.getUserIdFromPreferences

class ChatFragment : Fragment() {
    private var _binding: FragmentChatBinding? = null
    private val binding get() = _binding!!
    private lateinit var rvChat: RecyclerView
    private lateinit var adapter: ChatAdapter
    private lateinit var auth: FirebaseAuth
    private lateinit var chatViewModel: ChatViewModel
    private var progressBar: ProgressBar? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        val userId = getUserIdFromPreferences(requireContext())

        // Toolbar setup (assuming you're using Fragment with Activity's toolbar)
        //(requireActivity() as AppCompatActivity).setSupportActionBar(binding.myToolbar)
        //(requireActivity() as AppCompatActivity).supportActionBar?.title = getString(R.string.chatbot_text)
        //(requireActivity() as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
        //(requireActivity() as AppCompatActivity).supportActionBar?.setDisplayShowHomeEnabled(true)

        progressBar = binding.progressBar

        rvChat = binding.rvChatList
        rvChat.layoutManager = LinearLayoutManager(requireContext())

        adapter = ChatAdapter("User")
        rvChat.adapter = adapter

        val factory = ViewModelFactory.getInstance(requireActivity())
        chatViewModel = ViewModelProvider(this, factory).get(ChatViewModel::class.java)

        binding.messageEditText.addTextChangedListener {
            binding.sendButton.isEnabled =
                it?.isNotEmpty() == true && chatViewModel.isLoading.value == false
        }

        chatViewModel.chatMessages.observe(viewLifecycleOwner) { messages ->
            adapter.submitList(messages)
            if (messages.isNotEmpty()) {
                rvChat.scrollToPosition(messages.size - 1)
            }
        }

        chatViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            progressBar?.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.sendButton.isEnabled =
                !isLoading && binding.messageEditText.text?.isNotEmpty() == true
        }

        chatViewModel.errorMessage.observe(viewLifecycleOwner) { errorMessage ->
            errorMessage?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
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
                                    chatViewModel.sendMessage(idToken, userId, messageText)
                                    binding.messageEditText.text.clear()
                                } else {
                                    Toast.makeText(
                                        requireContext(),
                                        "Gagal mendapatkan token",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    Log.e("ChatFragment", "Gagal mendapatkan token")
                                }
                            } else {
                                Toast.makeText(requireContext(), "Gagal mendapatkan token", Toast.LENGTH_SHORT)
                                    .show()
                                Log.e("ChatFragment", "Gagal mendapatkan token", task.exception)
                            }
                        }
                } else {
                    Toast.makeText(requireContext(), "User tidak ditemukan", Toast.LENGTH_SHORT).show()
                    Log.e("ChatFragment", "User tidak ditemukan")
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
package com.seismiks.nyamapp.ui.chat

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.ProgressBar
import android.widget.Toast
import androidx.annotation.MenuRes
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.dialog.MaterialAlertDialogBuilder
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
    private var topAppBar: MaterialToolbar? = null
    private var model = "gemini"

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

        topAppBar = binding.myToolbar

        topAppBar?.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.help -> {
                    MaterialAlertDialogBuilder(requireContext())
                        .setTitle("Nyambot")
                        .setMessage("Nyambot adalah asisten chatbot yang dapat membantu menjawab pertanyaan anda seputar makanan.")
                        .setPositiveButton("Oke") { dialog, _ ->
                            dialog.dismiss()
                        }
                        .show()
                    true
                }
                R.id.picker -> {
                    val anchorView = requireActivity().findViewById<View>(R.id.picker)
                    showPickerMenu(anchorView, R.menu.chatbot_models)
                    true
                }

                else -> {
                    false
                }
            }
        }

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
                                if (idToken != null && model == "gemini") {
                                    chatViewModel.sendMessageGemini(idToken, userId, messageText)
                                    binding.messageEditText.text.clear()
                                } else if (idToken != null && model == "local") {
                                    chatViewModel.sendMessageLocal(idToken, userId, messageText)
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

    private fun showPickerMenu(v: View, @MenuRes menuRes: Int) {
        val popup = PopupMenu(requireContext(), v)
        popup.menuInflater.inflate(menuRes, popup.menu)

        // Tambahkan listener untuk menangani klik pada item di dalam popup menu
        popup.setOnMenuItemClickListener { menuItem: MenuItem ->
            when (menuItem.itemId) {
                R.id.gemini -> {
                    model = "gemini"
                    Toast.makeText(context, "Pilih Model Gemini", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.local -> {
                    model = "local"
                    Toast.makeText(context, "Pilih Model Local", Toast.LENGTH_SHORT).show()
                    true
                }
                else -> false
            }
        }
        // Tampilkan popup menu
        popup.show()
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
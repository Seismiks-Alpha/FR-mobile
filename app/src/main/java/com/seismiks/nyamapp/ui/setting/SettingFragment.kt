package com.seismiks.nyamapp.ui.setting

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.seismiks.nyamapp.data.Result
import com.seismiks.nyamapp.R
import com.seismiks.nyamapp.ViewModelFactory
import com.seismiks.nyamapp.databinding.FragmentSettingBinding
import com.seismiks.nyamapp.ui.MainActivity
import com.seismiks.nyamapp.utils.AppPreferences.clearUserIdFromPreferences
import kotlinx.coroutines.launch

class SettingFragment : Fragment() {
    private var _binding: FragmentSettingBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth
    private lateinit var viewModel: SettingViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val factory = ViewModelFactory.getInstance(requireActivity())
        viewModel = ViewModelProvider(this, factory).get(SettingViewModel::class.java)

        // Setup toolbar in host activity
        //(requireActivity() as AppCompatActivity).setSupportActionBar(binding.myToolbar)
        //(requireActivity() as AppCompatActivity).supportActionBar?.title = "Setting"
        //(requireActivity() as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
        //(requireActivity() as AppCompatActivity).supportActionBar?.setDisplayShowHomeEnabled(true)

        auth = Firebase.auth

        // Load profile info
        auth.currentUser?.getIdToken(true)?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val idToken = task.result?.token
                if (idToken != null) {
                    lifecycleScope.launch {
                        viewModel.getProfileInfo(idToken).observe(viewLifecycleOwner) { result ->
                            when (result) {
                                is Result.Loading -> {
                                    Log.d("SettingFragment", "Loading...")
                                }
                                is Result.Success -> {
                                    Log.d("SettingFragment", "Success: ${result.data}")
                                    binding.tvUserEmail.text = result.data.email
                                    binding.tvUserName.text = result.data.displayName
                                    val imageUrl = result.data.photoUrl
                                    Glide.with(requireContext())
                                        .load(imageUrl)
                                        .apply(
                                            RequestOptions()
                                                .placeholder(R.drawable.user)
                                                .error(R.drawable.user)
                                        )
                                        .into(binding.ivUserProfile)
                                }
                                is Result.Error -> {
                                    Log.e("SettingFragment", "Error: Gagal mengambil data profil")
                                }
                            }
                        }
                    }
                }
            }
        }

        binding.layoutUser.setOnClickListener {
            val intent = Intent(requireActivity(), UserSettingActivity::class.java)
            startActivity(intent)
        }

        binding.btnLogout.setOnClickListener {
            showLoading(true)
            clearUserIdFromPreferences(requireContext())
            auth.signOut()
            val intent = Intent(requireActivity(), MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            showLoading(false)
            requireActivity().finish()
        }

        // Set initial email
        binding.tvUserEmail.text = auth.currentUser?.email
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
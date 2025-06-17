package com.seismiks.nyamapp.ui.setting

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.seismiks.nyamapp.R
import com.seismiks.nyamapp.ViewModelFactory
import com.seismiks.nyamapp.data.Result
import com.seismiks.nyamapp.data.remote.response.LeaderboardResponse
import com.seismiks.nyamapp.databinding.FragmentSettingBinding
import com.seismiks.nyamapp.ui.AboutActivity
import com.seismiks.nyamapp.ui.MainActivity
import com.seismiks.nyamapp.ui.leaderBoard.LeaderboardAdapter
import com.seismiks.nyamapp.utils.AppPreferences.clearUserIdFromPreferences

class SettingFragment : Fragment() {
    private var _binding: FragmentSettingBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth
    private lateinit var viewModel: SettingViewModel
    private lateinit var rvLeaderboard: RecyclerView
    private lateinit var thisAdapter: LeaderboardAdapter

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

        auth = Firebase.auth

        fetchData()
        setupRecyclerView()
        setupObserver()
        // Load profile info

        binding.layoutUser.setOnClickListener {
            val intent = Intent(requireActivity(), UserSettingActivity::class.java)
            startActivity(intent)
        }

        binding.cardAbout.setOnClickListener {
            val intent = Intent(requireActivity(), AboutActivity::class.java)
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

        binding.btnHelp.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Leaderboard")
                .setMessage(
                    "Leaderboard diurutkan berdasarkan poin tertinggi. Top 5 pengguna ditampilkan dengan rank dan poin masing-masing.\n\n" +
                            "Bonus harian:\n" +
                            "Rank 1 = +5 poin\n" +
                            "Rank 2 = +4 poin\n" +
                            "Rank 3 = +3 poin\n" +
                            "Rank 4 = +2 poin\n" +
                            "Rank 5 = +1 poin\n\n" +
                            "Bonus hanya diberikan sekali sehari."
                )
                .setPositiveButton("Oke") { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
            true
        }

        // Set initial email
        binding.tvUserEmail.text = auth.currentUser?.email
    }

    private fun setupRecyclerView() {
        thisAdapter = LeaderboardAdapter()
        binding.rvLeaderboard.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = thisAdapter
        }
    }

    private fun setupObserver() {
        viewModel.profile.observe(viewLifecycleOwner) { result ->
            when (result) {
                is Result.Loading -> {
                    showLoading(true)
                }

                is Result.Success -> {
                    showLoading(false)
                    binding.tvUserName.text = result.data.displayName
                    binding.tvUserEmail.text = result.data.email
                    Glide.with(requireActivity())
                        .load(result.data.photoUrl)
                        .apply(RequestOptions.circleCropTransform())
                        .into(binding.ivUserProfile)
                }

                is Result.Error -> {
                    showLoading(false)
                    binding.tvUserName.text = "Error"
                    binding.tvUserEmail.text = "Error"
                }
            }
        }

        viewModel.leaderboard.observe(viewLifecycleOwner) { result ->
            handleResult(result)
        }
    }

    private fun fetchData() {
        auth.currentUser?.getIdToken(true)?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val idToken = task.result?.token
                if (idToken != null) {
                    viewModel.fetchAllData(idToken)
                } else {
                    Log.e("SettingFragment", "ID Token is null")
                }
            }
        }
    }

    private fun handleResult(result: Result<LeaderboardResponse>) {
        when (result) {
            is Result.Loading -> {
                // Tampilkan ProgressBar saat memuat data
                binding.progressBar.visibility = View.VISIBLE
            }

            is Result.Success -> {
                // Sembunyikan ProgressBar
                binding.progressBar.visibility = View.GONE
                // Ambil list dari 'data' -> 'top5'
                val userList = result.data.top5
                // Kirim list ke adapter
                thisAdapter.submitList(userList)
                // Anda juga bisa menampilkan data 'currentUser' di UI lain
                val currentUser = result.data.currentUser
                binding.tvUserRank.text = getString(
                    R.string.user_rank,
                    currentUser.rank.toString(),
                    currentUser.points.toString()
                )
            }

            is Result.Error -> {
                // Sembunyikan ProgressBar
                binding.progressBar.visibility = View.GONE
                // Tampilkan pesan error
                Toast.makeText(requireContext(), result.error, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
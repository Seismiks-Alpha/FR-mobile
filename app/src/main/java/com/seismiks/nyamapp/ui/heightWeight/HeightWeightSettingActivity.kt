package com.seismiks.nyamapp.ui.heightWeight

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.seismiks.nyamapp.R
import com.seismiks.nyamapp.ViewModelFactory
import com.seismiks.nyamapp.data.Result
import com.seismiks.nyamapp.data.remote.retrofit.ProfileUser
import com.seismiks.nyamapp.databinding.ActivityHeightWeightSettingsBinding
import kotlinx.coroutines.launch

class HeightWeightSettingActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHeightWeightSettingsBinding
    private lateinit var viewModel: HeightWeightViewModel
    private lateinit var gender: String
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        gender = "Laki-laki"

        auth = Firebase.auth

        binding = ActivityHeightWeightSettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val factory: ViewModelFactory = ViewModelFactory.getInstance(this)
        viewModel = ViewModelProvider(this, factory).get(HeightWeightViewModel::class.java)

        binding.ivMale.setOnClickListener {
            gender = "Laki-laki"
            binding.ivMale.setImageResource(R.drawable.iv_male_active)
            binding.ivFemale.setImageResource(R.drawable.iv_female_inactive)
            binding.ivMale.setBackgroundColor(getColor(R.color.white))
            binding.ivFemale.setBackgroundColor(getColor(R.color.green_yellow))
        }

        binding.ivFemale.setOnClickListener {
            gender = "Perempuan"
            binding.ivMale.setImageResource(R.drawable.iv_male_inactive)
            binding.ivFemale.setImageResource(R.drawable.iv_female_active)
            binding.ivFemale.setBackgroundColor(getColor(R.color.white))
            binding.ivMale.setBackgroundColor(getColor(R.color.green_yellow))
        }

        auth.currentUser?.getIdToken(true)?.addOnCompleteListener {
            if (it.isSuccessful) {
                val idToken = it.result.token
                if (idToken != null) {
                    viewModel.getProfileInfo(idToken).observe(this) { result ->
                        when (result) {
                            is Result.Loading -> {
                                showLoading(true)
                                Log.d("HomeActivity", "Loading...")
                            }

                            is Result.Success -> {
                                showLoading(false)
                                Log.d("HomeActivity", "Success: ${result.data}")
                                binding.edHeight.setText(result.data.profile?.height.toString())
                                binding.edWeight.setText(result.data.profile?.weight.toString())
                                binding.edAge.setText(result.data.profile?.age.toString())
                            }

                            is Result.Error -> {
                                showLoading(false)
                                Log.e("HomeActivity", "Error: Gagal mengambil data profil")
                            }
                        }
                    }
                } else {
                    Log.e("HomeActivity", "ID Token is null")
                }
            }
        }

        binding.btnSave.setOnClickListener {
            val ageText = binding.edAge.text.toString()
            val weightText = binding.edWeight.text.toString()
            val heightText = binding.edHeight.text.toString()
            if (ageText.isBlank() || weightText.isBlank() || heightText.isBlank()) {
                Toast.makeText(this, "Semua field harus diisi", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val age = ageText.toIntOrNull()
            val weight = weightText.toIntOrNull()
            val height = heightText.toIntOrNull()

            if (age == null || weight == null || height == null) {
                Toast.makeText(
                    this,
                    "Masukkan angka yang valid untuk usia, berat, dan tinggi",
                    Toast.LENGTH_LONG
                ).show()
                return@setOnClickListener
            }

            if (age <= 0 || weight <= 0 || height <= 0) {
                Toast.makeText(this, "Usia, berat, dan tinggi harus positif", Toast.LENGTH_LONG)
                    .show()
                return@setOnClickListener
            }

            val currentUser = auth.currentUser
            if (currentUser != null) {
                lifecycleScope.launch {
                    currentUser?.getIdToken(true)?.addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val idToken = task.result.token
                            if (idToken != null) {
                                putProfile(idToken)
                            }
                        } else {
                            Log.e(
                                "HeightWeightSettingActivity",
                                "Error getting ID token",
                                task.exception
                            )
                        }
                    }
                }
            } else {
                Log.e("HeightWeightSettingActivity", "Current user is null")
            }
        }

        binding.btnCancel.setOnClickListener {
            finish()
        }
    }

    private fun putProfile(token: String) {
        val profileUser = ProfileUser(
            age = binding.edAge.text.toString().toInt(),
            weight = binding.edWeight.text.toString().toInt(),
            height = binding.edHeight.text.toString().toInt(),
            gender = gender
        )
        viewModel.updateProfileUpdateAndStatus(token, profileUser).observe(this) { result ->
            when (result) {
                is Result.Loading -> {
                    binding.btnSave.isEnabled = false
                    Toast.makeText(this, "Updating profile...", Toast.LENGTH_SHORT).show()
                }

                is Result.Success -> {
                    binding.btnSave.isEnabled = true
                    Toast.makeText(this, "Profil berhasil disimpan!", Toast.LENGTH_SHORT).show()
                    Log.d("HeightWeightSettingActivity", "putProfile successful: ${result.data}")
                    finish()
                }

                is Result.Error -> {
                    binding.btnSave.isEnabled = true
                    Toast.makeText(
                        this,
                        "Gagal menyimpan profil: ${result.error}",
                        Toast.LENGTH_LONG
                    ).show()
                    Log.e("HeightWeightSettingActivity", "putProfile error: ${result.error}")
                }
            }
            Log.d("HeightWeightSettingActivity", "putProfile: $profileUser")
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.btnSave.isEnabled = !isLoading
    }
}
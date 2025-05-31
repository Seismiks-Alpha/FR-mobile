package com.reza.nyamapp.ui.setting

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.reza.nyamapp.ViewModelFactory
import com.reza.nyamapp.data.Result
import com.reza.nyamapp.data.remote.retrofit.Photo
import com.reza.nyamapp.databinding.ActivityUserSettingBinding

class UserSettingActivity : AppCompatActivity() {
    private lateinit var binding: ActivityUserSettingBinding
    private lateinit var viewModel: SettingViewModel
    private lateinit var auth: FirebaseAuth
    private var currentImageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserSettingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth

        val factory: ViewModelFactory = ViewModelFactory.getInstance(this)
        viewModel = ViewModelProvider(this, factory)[SettingViewModel::class.java]

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
                                binding.tvUsername.text = result.data.displayName
                                binding.tvEmail.text = result.data.email
                                binding.tvAge.text = result.data.profile?.age.toString()
                                binding.tvGender.text = result.data.profile?.gender
                            }

                            is Result.Error -> {
                                showLoading(false)
                                Log.e("HomeActivity", "Error: Gagal mengambil data profil")
                            }
                        }
                    }
                }
            }
        }

        binding.btnIvUser.setOnClickListener {
            startGallery()
        }

        binding.btnCancel.setOnClickListener {
            finish()
        }

        binding.btnSave.setOnClickListener {
            if (currentImageUri == null) {
                Toast.makeText(
                    this@UserSettingActivity,
                    "Pilih foto terlebih dahulu",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                auth.currentUser?.getIdToken(true)?.addOnCompleteListener {
                    if (it.isSuccessful) {
                        val idToken = it.result.token
                        if (idToken != null) {
                            putPhoto(idToken)
                        }
                    }
                }
            }
        }
    }

    private fun startGallery() {
        launcherGallery.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

    private val launcherGallery = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            currentImageUri = uri
            viewModel.imageUri = uri
            showImage()
        } else {
            Log.d("Photo Picker", "No media selected")
        }
    }

    private fun putPhoto(tokenId: String) {
        val imageUri = viewModel.imageUri
        val photo = Photo(
            photoUrl = imageUri.toString()
        )
        viewModel.putPhoto(tokenId, photo).observe(this) { result ->
            when (result) {
                is Result.Loading -> {
                    showLoading(true)
                    Log.d("HomeActivity", "Loading...")
                }

                is Result.Success -> {
                    showLoading(false)
                    Log.d("HomeActivity", "Success: ${result.data}")
                }

                is Result.Error -> {
                    showLoading(false)
                    Toast.makeText(
                        this@UserSettingActivity,
                        "Gagal memperbarui foto profil",
                        Toast.LENGTH_SHORT
                    ).show()
                    Log.e("HomeActivity", "Error: Gagal mengambil data profil")
                }
            }
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    private fun showImage() {
        currentImageUri?.let {
            Log.d("Image URI", "showImage: $it")
            binding.ivUserProfile.setImageURI(it)
            viewModel.imageUri = it
        }
    }
}
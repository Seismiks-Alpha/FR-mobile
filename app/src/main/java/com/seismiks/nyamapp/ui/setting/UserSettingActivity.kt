package com.seismiks.nyamapp.ui.setting

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.seismiks.nyamapp.R
import com.seismiks.nyamapp.ViewModelFactory
import com.seismiks.nyamapp.data.Result
import com.seismiks.nyamapp.databinding.ActivityUserSettingBinding

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

        setSupportActionBar(binding.myToolbar)
        supportActionBar?.title = getString(R.string.user)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

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
                                Log.d(TAG, "Loading...")
                            }

                            is Result.Success -> {
                                showLoading(false)
                                Log.d(TAG, "Success: ${result.data}")
                                binding.tvUsername.text = result.data.displayName
                                binding.tvEmail.text = result.data.email
                                binding.tvAge.text = result.data.profile?.age.toString()
                                binding.tvGender.text = result.data.profile?.gender
                                val imageUrl = result.data.photoUrl
                                Glide.with(this@UserSettingActivity)
                                    .load(imageUrl)
                                    .apply(
                                        RequestOptions()
                                            .centerCrop()
                                    )
                                    .centerCrop()
                                    .transition(DrawableTransitionOptions.withCrossFade())
                                    .into(binding.ivUserProfile)
                            }

                            is Result.Error -> {
                                showLoading(false)
                                Log.e(TAG, "Error: Gagal mengambil data profil")
                            }
                        }
                    }
                }
            }
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }

    companion object {
        private const val TAG = "UserSettingActivity"
    }
}
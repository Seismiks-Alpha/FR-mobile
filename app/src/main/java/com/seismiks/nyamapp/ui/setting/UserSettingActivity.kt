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
import com.google.android.material.appbar.MaterialToolbar
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
    private var topAppBar: MaterialToolbar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserSettingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth

        topAppBar = binding.myToolbar
        topAppBar?.setNavigationOnClickListener {
            finish()
        }

        val factory: ViewModelFactory = ViewModelFactory.getInstance(this)
        viewModel = ViewModelProvider(this, factory)[SettingViewModel::class.java]

        setupObserver()
        fetchData()
    }

    private fun setupObserver() {
        viewModel.profile.observe(this) { result ->
            when (result) {
                is Result.Loading -> {
                    showLoading(true)
                }
                is Result.Success -> {
                    showLoading(false)
                    binding.tvUsername.text = result.data.displayName
                    binding.tvEmail.text = result.data.email
                    Glide.with(this)
                        .load(result.data.photoUrl)
                        .apply(RequestOptions.circleCropTransform())
                        .transition(DrawableTransitionOptions.withCrossFade())
                        .into(binding.ivUserProfile)
                    binding.tvGender.text = result.data.profile?.gender
                    binding.tvAge.text = result.data.profile?.age.toString()
                }
                is Result.Error -> {
                    showLoading(false)
                }
            }
        }
    }

    private fun fetchData() {
        auth.currentUser?.getIdToken(true)?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val idToken = task.result?.token
                if (idToken != null) {
                    viewModel.getProfileInfo(idToken)
                } else {
                    Log.e(TAG, "ID Token is null")
                }
            }
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    companion object {
        private const val TAG = "UserSettingActivity"
    }
}
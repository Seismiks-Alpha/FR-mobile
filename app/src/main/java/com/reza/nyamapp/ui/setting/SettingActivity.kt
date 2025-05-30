package com.reza.nyamapp.ui.setting

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.reza.nyamapp.R
import com.reza.nyamapp.ViewModelFactory
import com.reza.nyamapp.data.Result
import com.reza.nyamapp.databinding.ActivitySettingBinding
import com.reza.nyamapp.ui.MainActivity
import com.reza.nyamapp.utils.AppPreferences.clearUserIdFromPreferences
import kotlinx.coroutines.launch

class SettingActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySettingBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var viewModel: SettingViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)

        binding = ActivitySettingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val factory: ViewModelFactory = ViewModelFactory.getInstance(this)
        viewModel = ViewModelProvider(this, factory).get(SettingViewModel::class.java)

        setSupportActionBar(binding.myToolbar)
        supportActionBar?.title = "Setting"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        auth = Firebase.auth

        auth.currentUser?.getIdToken(true)?.addOnCompleteListener {
            if (it.isSuccessful) {
                val idToken = it.result?.token
                if (idToken != null) {
                    lifecycleScope.launch {
                        viewModel.getProfileInfo(idToken).observe(this@SettingActivity) { result ->
                            when (result) {
                                is Result.Loading -> {
                                    Log.d("SettingActivity", "Loading...")
                                }

                                is Result.Success -> {
                                    Log.d("SettingActivity", "Success: ${result.data}")
                                    binding.tvUserEmail.text = result.data.email
                                    binding.tvUserName.text = result.data.displayName
                                    val imageUrl = result.data.photoUrl
                                    Glide.with(this@SettingActivity)
                                        .load(imageUrl)
                                        .apply(
                                            RequestOptions()
                                                .placeholder(R.drawable.user)
                                                .error(R.drawable.user)
                                        )
                                        .into(binding.ivUserProfile)
                                }

                                is Result.Error -> {
                                    Log.e("SettingActivity", "Error: Gagal mengambil data profil")
                                }
                            }
                        }
                    }
                }
            }
        }

        binding.layoutUser.setOnClickListener {
            val intent = Intent(this, UserSettingActivity::class.java)
            startActivity(intent)
        }

        binding.btnLogout.setOnClickListener {
            showLoading(true)
            clearUserIdFromPreferences(applicationContext)
            auth.signOut()
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            showLoading(false)
            finish()
        }

        binding.tvUserEmail.text = auth.currentUser?.email
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}
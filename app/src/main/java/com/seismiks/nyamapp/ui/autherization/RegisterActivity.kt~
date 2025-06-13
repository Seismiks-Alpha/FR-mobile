package com.seismiks.nyamapp.ui.autherization

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityOptionsCompat
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.Firebase
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import com.seismiks.nyamapp.R
import com.seismiks.nyamapp.ViewModelFactory
import com.seismiks.nyamapp.data.Result
import com.seismiks.nyamapp.data.remote.response.SyncProfileResponse
import com.seismiks.nyamapp.databinding.ActivityRegisterBinding
import com.seismiks.nyamapp.ui.heightWeight.HeightWeightRegisterActivity
import com.seismiks.nyamapp.ui.heightWeight.HeightWeightSettingActivity
import com.seismiks.nyamapp.utils.AppPreferences.saveUserIdToPreferences
import kotlinx.coroutines.launch
import java.util.regex.Pattern

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var profileViewModel: ProfileViewModel
    private val regexEmail = "^[A-Za-z0-9+_.-]+@(.+)\$".toRegex()
    private val specialCharPattern = Pattern.compile("[^a-zA-Z0-9]")
    private val uppercaseCharPattern = Pattern.compile("[A-Z]")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth

        val factory: ViewModelFactory = ViewModelFactory.getInstance(this)
        profileViewModel = ViewModelProvider(this, factory)[ProfileViewModel::class.java]

        setupRealtimeValidation()

        binding.btnLogin.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }

        binding.btnGoogle.setOnClickListener {
            signIn()
        }

        binding.btnRegister.setOnClickListener {
            val email = binding.edEmail.text.toString()
            val password = binding.edPassword.text.toString()
            val confirmPassword = binding.edConfirmPassword.text.toString()

            val isEmailValid = validateEmail(email)
            val isPasswordValid = validatePassword(password)
            val isConfirmPasswordValid = validateConfirmPassword(confirmPassword)

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Tolong lengkapi email dan password", Toast.LENGTH_SHORT)
                    .show()

                if (email.isEmpty()) binding.tilEmail.error = "Email tidak boleh kosong"
                if (password.isEmpty()) binding.tilPassword.error = "Password tidak boleh kosong"
                if (confirmPassword.isEmpty()) binding.tilConfirmPassword.error =
                    "Konfirmasi password tidak boleh kosong"
                return@setOnClickListener
            }

            if (isEmailValid && isPasswordValid && isConfirmPasswordValid) {
                register(email, password)
            } else {
                if (!isEmailValid && email.isNotEmpty()) binding.tilEmail.error =
                    "Email tidak valid"
                if (!isPasswordValid && password.isNotEmpty()) validatePassword(password)
                if (!isConfirmPasswordValid && confirmPassword.isNotEmpty()) validateConfirmPassword(
                    confirmPassword
                )
            }
        }
    }

    private fun register(email: String, password: String) {
        showLoading(true)
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    showLoading(false)
                    val user = auth.currentUser
                    user?.uid?.let {
                        saveUserId(user)
                    }
                    user?.getIdToken(true)?.addOnSuccessListener { result ->
                        val token = result.token
                        if (token != null) {
                            lifecycleScope.launch {
                                profileViewModel.syncProfile(token)
                            }
                            profileViewModel.profile.observe(
                                this@RegisterActivity,
                                object : Observer<Result<SyncProfileResponse>> {
                                    override fun onChanged(result: Result<SyncProfileResponse>) {
                                        when (result) {
                                            is Result.Loading -> {
                                                Log.d(
                                                    "RegisterActivity",
                                                    "Profile Sync: Loading..."
                                                )
                                            }

                                            is Result.Success -> {
                                                Log.d(
                                                    "RegisterActivity",
                                                    "Profile Sync: Success - ${result.data}"
                                                )
                                                profileViewModel.profile.removeObserver(this)
                                                showLoading(false)
                                                updateUI(auth.currentUser)
                                            }

                                            is Result.Error -> {
                                                Log.e(
                                                    "RegisterActivity",
                                                    "Profile Sync: Error - ${result.error}"
                                                )
                                                profileViewModel.profile.removeObserver(this)
                                                showLoading(false)
                                                Toast.makeText(
                                                    this@RegisterActivity,
                                                    "Gagal sinkronisasi profil: ${result.error}",
                                                    Toast.LENGTH_LONG
                                                ).show()
                                            }
                                        }
                                    }
                                })
                        }
                    }
                    Log.d("RegisterActivity", "createUserWithEmail:success")
                    updateUI(user)
                } else {
                    Log.w("RegisterActivity", "createUserWithEmail:failure", task.exception)
                    Toast.makeText(baseContext, "Authentication failed.", Toast.LENGTH_SHORT).show()
                    updateUI(null)
                    showLoading(false)
                }
            }
    }

    private fun setupRealtimeValidation() {
        binding.edEmail.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                validateEmail(s.toString())
            }
        })

        binding.edPassword.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                validatePassword(s.toString())
            }
        })

        binding.edConfirmPassword.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                validateConfirmPassword(s.toString())
            }
        })
    }

    private fun validateEmail(email: String): Boolean {
        return if (email.isNotEmpty() && !regexEmail.matches(email)) {
            binding.tilEmail.error = "Email tidak valid"
            false
        } else {
            binding.tilEmail.error = null
            true
        }
    }

    private fun validatePassword(password: String): Boolean {
        when {
            password.isNotEmpty() && password.length < 8 -> {
                binding.tilPassword.error = getString(R.string.password_minimal_8_karakter)
                return false
            }

            password.isNotEmpty() && !specialCharPattern.matcher(password).find() -> {
                binding.tilPassword.error = "Password harus memiliki minimal satu karakter spesial."
                return false
            }

            password.isNotEmpty() && !uppercaseCharPattern.matcher(password).find() -> {
                binding.tilPassword.error = "Password harus memiliki minimal satu huruf kapital."
                return false
            }

            else -> {
                binding.tilPassword.error = null
                return true
            }
        }
    }

    private fun validateConfirmPassword(password: String): Boolean {
        return if (password.isNotEmpty() && password != binding.edPassword.text.toString()) {
            binding.tilConfirmPassword.error = "Password tidak cocok"
            false
        } else {
            binding.tilConfirmPassword.error = null
            true
        }
    }

    private fun signIn() {
        val credentialManager = CredentialManager.create(this)

        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(getString(R.string.web_client_id))
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        lifecycleScope.launch {
            try {
                val result: GetCredentialResponse =
                    credentialManager.getCredential(
                        request = request,
                        context = this@RegisterActivity,
                    )
                handleSignIn(result)
            } catch (e: GetCredentialException) {
                Log.d("Error", e.message.toString())
            }
        }
    }

    private fun handleSignIn(result: GetCredentialResponse) {
        when (val credential = result.credential) {
            is CustomCredential -> {
                if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    try {
                        val googleIdTokenCredential =
                            GoogleIdTokenCredential.createFrom(credential.data)
                        firebaseAuthWithGoogle(googleIdTokenCredential.idToken)
                    } catch (e: Exception) {
                        Log.e("RegisterActivity", "Error creating GoogleIdTokenCredential", e)
                    }
                } else {
                    Log.e("RegisterActivity", "Unexpected type of credential")
                }
            }

            else -> {
                Log.e("RegisterActivity", "Unexpected type of credential")
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        showLoading(true)
        val credential: AuthCredential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d("LoginActivity", "signInWithCredential:success")
                    val user: FirebaseUser? = auth.currentUser
                    user?.uid?.let {
                        saveUserId(user)
                    }
                    user?.getIdToken(true)?.addOnSuccessListener { result ->
                        val token = result.token
                        if (token != null) {
                            lifecycleScope.launch {
                                profileViewModel.syncProfile(token)
                            }
                            profileViewModel.profile.observe(
                                this@RegisterActivity,
                                object : Observer<Result<SyncProfileResponse>> {
                                    override fun onChanged(result: Result<SyncProfileResponse>) {
                                        when (result) {
                                            is Result.Loading -> {
                                                Log.d("LoginActivity", "Profile Sync: Loading...")
                                                // showLoading(true) sudah dipanggil di awal
                                            }

                                            is Result.Success -> {
                                                Log.d(
                                                    "LoginActivity",
                                                    "Profile Sync: Success - ${result.data}"
                                                )
                                                profileViewModel.profile.removeObserver(this) // Hentikan observasi setelah selesai
                                                showLoading(false)
                                                updateUI(auth.currentUser) // Pindah ke HomeActivity
                                            }

                                            is Result.Error -> {
                                                Log.e(
                                                    "LoginActivity",
                                                    "Profile Sync: Error - ${result.error}"
                                                )
                                                profileViewModel.profile.removeObserver(this) // Hentikan observasi setelah selesai
                                                showLoading(false)
                                                Toast.makeText(
                                                    this@RegisterActivity,
                                                    "Gagal sinkronisasi profil: ${result.error}",
                                                    Toast.LENGTH_LONG
                                                ).show()
                                                updateUI(auth.currentUser)
                                            }
                                        }
                                    }
                                })
                        }
                    }
                } else {
                    Log.w("LoginActivity", "signInWithCredential:failure", task.exception)
                    Toast.makeText(baseContext, "Authentication failed.", Toast.LENGTH_SHORT)
                        .show()
                    updateUI(null)
                    showLoading(false)
                }
            }
    }

    private fun saveUserId(firebaseUser: FirebaseUser?) {
        val userId = firebaseUser?.uid
        if (userId != null) {
            saveUserIdToPreferences(applicationContext, userId)
            Log.d("LoginActivity", "saveUserId: $userId")
        } else {
            Log.d("LoginActivity", "saveUserId: User ID is null")
        }
    }

    private fun updateUI(user: FirebaseUser?) {
        if (user != null) {
            val intent = Intent(this, HeightWeightRegisterActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(
                intent,
                ActivityOptionsCompat.makeSceneTransitionAnimation(this).toBundle()
            )
            finish()
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.btnLogin.isEnabled = !isLoading
    }
}
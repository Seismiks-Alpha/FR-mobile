package com.reza.nyamapp.ui.autherization

import android.content.Intent
import android.os.Bundle
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
import com.reza.nyamapp.R
import com.reza.nyamapp.ViewModelFactory
import com.reza.nyamapp.data.Result
import com.reza.nyamapp.data.remote.response.SyncProfileResponse
import com.reza.nyamapp.databinding.ActivityLoginBinding
import com.reza.nyamapp.ui.home.HomeActivity
import com.reza.nyamapp.utils.AppPreferences.saveUserIdToPreferences
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var profileViewModel: ProfileViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth

        val factory: ViewModelFactory = ViewModelFactory.getInstance(this)
        profileViewModel = ViewModelProvider(this, factory)[ProfileViewModel::class.java]

        binding.btnRegister.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        binding.btnLogin.setOnClickListener {
            val email = binding.edEmail.text.toString()
            val password = binding.edPassword.text.toString()
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Tolong lengkapi email dan password", Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            } else {
                firebaseAuthWithEmail(email, password)
            }
        }

        binding.btnGoogle.setOnClickListener {
            signIn()
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
                        context = this@LoginActivity,
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
                        Log.e("LoginActivity", "Error creating GoogleIdTokenCredential", e)
                    }
                } else {
                    Log.e("LoginActivity", "Unexpected type of credential")
                }
            }

            else -> {
                Log.e("LoginActivity", "Unexpected type of credential")
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
                                this@LoginActivity,
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
                                                    this@LoginActivity,
                                                    "Gagal sinkronisasi profil: ${result.error}",
                                                    Toast.LENGTH_LONG
                                                ).show()
                                                // Anda bisa memilih untuk tetap navigasi (updateUI(auth.currentUser))
                                                // atau tetap di halaman login, atau menampilkan pesan error yang lebih jelas.
                                                // Untuk contoh ini, kita tetap navigasi saja, HomeActivity harus bisa menangani jika profil gagal load.
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

    private fun firebaseAuthWithEmail(email: String, password: String) {
        showLoading(true)
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d("LoginActivity", "signInWithEmail:success")
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
                                this@LoginActivity,
                                object : Observer<Result<SyncProfileResponse>> {
                                    override fun onChanged(result: Result<SyncProfileResponse>) {
                                        when (result) {
                                            is Result.Loading -> {
                                                Log.d("LoginActivity", "Profile Sync: Loading...")
                                                // showLoading(true) sudah dipanggil di awal
                                            }

                                            is Result.Success -> {
                                                showLoading(false)
                                                Log.d(
                                                    "LoginActivity",
                                                    "Profile Sync: Success - ${result.data}"
                                                )
                                                profileViewModel.profile.removeObserver(this) // Hentikan observasi setelah selesai
                                                updateUI(auth.currentUser) // Pindah ke HomeActivity
                                            }

                                            is Result.Error -> {
                                                showLoading(false)
                                                Log.e(
                                                    "LoginActivity",
                                                    "Profile Sync: Error - ${result.error}"
                                                )
                                                profileViewModel.profile.removeObserver(this) // Hentikan observasi setelah selesai
                                                Toast.makeText(
                                                    this@LoginActivity,
                                                    "Gagal sinkronisasi profil: ${result.error}",
                                                    Toast.LENGTH_LONG
                                                ).show()
                                                // Anda bisa memilih untuk tetap navigasi (updateUI(auth.currentUser))
                                                // atau tetap di halaman login, atau menampilkan pesan error yang lebih jelas.
                                                // Untuk contoh ini, kita tetap navigasi saja, HomeActivity harus bisa menangani jika profil gagal load.
                                                updateUI(auth.currentUser)
                                            }
                                        }
                                    }
                                })
                        }
                    }
                } else {
                    Log.w("LoginActivity", "signInWithEmail:failure", task.exception)
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
            val intent = Intent(this, HomeActivity::class.java)
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
        binding.btnGoogle.isEnabled = !isLoading
    }

    override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser
        if (currentUser != null) {
            Log.d("LoginActivity", "User already logged in onStart: ${currentUser.uid}")
            saveUserId(currentUser)
            lifecycleScope.launch {
                currentUser?.getIdToken(true)?.addOnSuccessListener { task ->
                    val idToken = task.token
                    if (idToken != null) {
                        lifecycleScope.launch {
                            profileViewModel.syncProfile(idToken)
                        }
                    }
                }
            }
            updateUI(currentUser)
        }
    }
}
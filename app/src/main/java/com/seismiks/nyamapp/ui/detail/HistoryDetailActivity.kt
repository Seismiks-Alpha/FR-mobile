package com.seismiks.nyamapp.ui.detail

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.seismiks.nyamapp.R
import com.seismiks.nyamapp.ViewModelFactory
import com.seismiks.nyamapp.data.Result
import com.seismiks.nyamapp.data.remote.response.FoodHistory
import com.seismiks.nyamapp.data.remote.response.HistoryViewItem
import com.seismiks.nyamapp.databinding.ActivityHistoryDetailBinding
import com.seismiks.nyamapp.ui.history.HistoryActivity
import com.seismiks.nyamapp.utils.DATE_ADDED
import com.seismiks.nyamapp.utils.DateUtils.toFormattedDateTime
import com.seismiks.nyamapp.utils.FOOD_TODAY

class HistoryDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHistoryDetailBinding
    private lateinit var viewModel: HistoryDetailViewModel
    private lateinit var ivResult: ImageView
    private lateinit var tvFoodName: TextView
    private lateinit var tvKalori: TextView
    private lateinit var tvLemak: TextView
    private lateinit var tvKarbohidrat: TextView
    private lateinit var tvProtein: TextView
    private lateinit var tvWeight: TextView
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityHistoryDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth

        setSupportActionBar(binding.myToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        val factory = ViewModelFactory.getInstance(this)
        viewModel = ViewModelProvider(this, factory).get(HistoryDetailViewModel::class.java)

        ivResult = binding.ivResult
        tvFoodName = binding.tvFoodName
        tvKalori = binding.tvCalorie
        tvLemak = binding.tvFat
        tvKarbohidrat = binding.tvCarbo
        tvProtein = binding.tvProtein
        tvWeight = binding.tvWeight

        val foodItem = intent.getParcelableExtra<HistoryViewItem>(FOOD_TODAY)
        val image = foodItem?.imageUrl
        val foodName = foodItem?.foodName
        val kalori = foodItem?.calories
        val lemak = foodItem?.fat
        val karbo = foodItem?.carbohydrates
        val protein = foodItem?.protein
        val weight = foodItem?.grams
        val resultAddedInMillis = foodItem?.date
        val resultId = foodItem?.id

        Glide.with(this@HistoryDetailActivity)
            .load("https://nyam.seix.me" + image)
            .placeholder(R.drawable.intro)
            .into(ivResult)
        supportActionBar?.title = foodName
        tvFoodName.text = foodName
        tvKalori.text = getString(R.string.kcal, kalori.toString())
        tvLemak.text = getString(R.string.grams, lemak.toString())
        tvKarbohidrat.text = getString(R.string.grams, karbo.toString())
        tvProtein.text = getString(R.string.grams, protein.toString())
        tvWeight.text = getString(R.string.grams, weight.toString())
        binding.tvDate.text = resultAddedInMillis?.toFormattedDateTime()

        binding.btnDelete.setOnClickListener {
            auth.currentUser?.getIdToken(true)?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val idToken = task.result?.token
                    if (idToken != null) {
                        androidx.appcompat.app.AlertDialog.Builder(this)
                            .setTitle("Konfirmasi")
                            .setMessage("Apakah Anda yakin ingin menghapus riwayat ini? Aksi ini tidak dapat dibatalkan.")
                            .setPositiveButton("Hapus") { _, _ ->
                                viewModel.deleteHistory(idToken, resultId.toString())
                                    .observe(this) { result ->
                                        if (result is Result.Success) {
                                            val intent = Intent(this, HistoryActivity::class.java)
                                            startActivity(intent)
                                            finish()
                                        } else if (result is Result.Error) {
                                            Log.e("HistoryActivity", result.error)
                                        }
                                    }
                            }
                            .setNegativeButton("Batal", null)
                            .show()
                    } else {
                        Log.e("HistoryActivity", "ID Token is null")
                    }
                }
            }
        }

    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}

package com.reza.nyamapp.ui.detail

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.reza.nyamapp.R
import com.reza.nyamapp.ViewModelFactory
import com.reza.nyamapp.databinding.ActivityHistoryDetailBinding
import com.reza.nyamapp.utils.RESULT_ID
import com.reza.nyamapp.utils.convertLongToTime

class HistoryDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHistoryDetailBinding
    private lateinit var viewModel: HistoryDetailViewModel
    private lateinit var ivResult: ImageView
    private lateinit var tvFoodName: TextView
    private lateinit var tvKalori: TextView
    private lateinit var tvLemak: TextView
    private lateinit var tvKarbohidrat: TextView
    private lateinit var tvProtein: TextView
    private lateinit var tvIron: TextView
    private lateinit var tvCalcium: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityHistoryDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbarDetailHistoryActivity)
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
        tvIron = binding.tvIron
        tvCalcium = binding.tvCalcium

        val id = intent.getIntExtra(RESULT_ID, 0)

        viewModel.setResultId(id)

        viewModel.scanResult.observe(this) { scanResult ->
            if (scanResult != null) {
                with(scanResult) {
                    Glide.with(this@HistoryDetailActivity)
                        .load(image)
                        .placeholder(R.drawable.intro)
                        .into(ivResult)
                    supportActionBar?.title = foodName
                    tvFoodName.text = foodName
                    tvKalori.text = kalori.toString()
                    tvLemak.text = lemak.toString()
                    tvKarbohidrat.text = karbo.toString()
                    tvProtein.text = protein.toString()
                    tvIron.text = zatBesi.toString()
                    tvCalcium.text = kalsium.toString()
                    binding.tvDate.text = resultAddedInMillis.convertLongToTime()
                }
            }
        }

        viewModel.deleted.observe(this) { scanResult ->
            val deleted = scanResult.getContentIfNotHandled()

            if (deleted == true) {
                Toast.makeText(this, "Berhasil menghapus data", Toast.LENGTH_SHORT).show()
                finish()
            }
        }

        binding.btnDelete.setOnClickListener {
            AlertDialog.Builder(this).apply {
                setMessage(getString(R.string.konfirmasi_hapus))
                setNegativeButton(getString(R.string.tidak), null)
                setPositiveButton(getString(R.string.iya)) { _, _ ->
                    viewModel.deleteResult()
                }
                show()
            }
        }

    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}
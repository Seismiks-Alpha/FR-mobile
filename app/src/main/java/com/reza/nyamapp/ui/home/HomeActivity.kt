package com.reza.nyamapp.ui.home

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.utils.MPPointF
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.reza.nyamapp.R
import com.reza.nyamapp.ViewModelFactory
import com.reza.nyamapp.data.Result
import com.reza.nyamapp.databinding.ActivityHomeBinding
import com.reza.nyamapp.ui.setting.SettingActivity
import com.reza.nyamapp.ui.camera.CameraActivity
import com.reza.nyamapp.ui.chat.ChatActivity
import com.reza.nyamapp.ui.history.HistoryActivity
import com.reza.nyamapp.ui.heightWeight.HeightWeightSettingActivity
import java.util.Calendar

class HomeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHomeBinding

    private lateinit var viewModel: HomeViewModel

    lateinit var pieChart: PieChart

    lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth

        binding.btnChangeFit.setOnClickListener {
            val intent = Intent(this, HeightWeightSettingActivity::class.java)
            startActivity(intent)
        }

        pieChart = binding.pieChart

        pieChart.setUsePercentValues(true)
        pieChart.getDescription().setEnabled(false)
        pieChart.setExtraOffsets(5f, 10f, 5f, 5f)

        pieChart.setDragDecelerationFrictionCoef(0.95f)

        pieChart.setDrawHoleEnabled(true)
        pieChart.setHoleColor(Color.WHITE)

        pieChart.setTransparentCircleColor(Color.WHITE)
        pieChart.setTransparentCircleAlpha(110)

        pieChart.setHoleRadius(58f)
        pieChart.setTransparentCircleRadius(61f)

        pieChart.setDrawCenterText(true)

        pieChart.setRotationAngle(0f)

        pieChart.setRotationEnabled(true)
        pieChart.setHighlightPerTapEnabled(true)

        pieChart.animateY(1400, Easing.EaseInOutQuad)

        pieChart.legend.isEnabled = false
        pieChart.setEntryLabelColor(Color.WHITE)
        pieChart.setEntryLabelTextSize(12f)

        val factory = ViewModelFactory.getInstance(this)
        viewModel = ViewModelProvider(this, factory).get(HomeViewModel::class.java)

        auth.currentUser?.getIdToken(true)?.addOnCompleteListener {
            if (it.isSuccessful) {
                val idToken = it.result.token
                if (idToken != null) {
                    viewModel.getProfileInfo(idToken).observe(this) { result ->
                        when (result) {
                            is Result.Loading -> {
                                Log.d("HomeActivity", "Loading...")
                            }

                            is Result.Success -> {
                                Log.d("HomeActivity", "Success: ${result.data}")
                                binding.tvUserName.text = result.data.displayName
                                binding.tvHeightValue.text = result.data.profile?.height.toString()
                                binding.tvWeightValue.text = result.data.profile?.weight.toString()
                            }

                            is Result.Error -> {
                                Log.e("HomeActivity", "Error: Gagal mengambil data profil")
                            }
                        }
                    }
                } else {
                    Log.e("HomeActivity", "ID Token is null")
                }
            }
        }

        viewModel.getCaloriesToday(getMillisFromDate(System.currentTimeMillis()).toString())
            .observe(this) { calories ->
                val targetCalories = 2000f
                val consumedCalories = calories?.toFloat() ?: 0f

                val entries: ArrayList<PieEntry> = ArrayList()
                entries.add(PieEntry(consumedCalories, "Masuk"))
                if (targetCalories > consumedCalories) {
                    entries.add(PieEntry(targetCalories - consumedCalories, "Sisa"))
                } else {
                    entries.add(PieEntry(0f, "Sisa"))
                }
                pieChart.setEntryLabelColor(Color.WHITE)
                pieChart.setEntryLabelTypeface(Typeface.DEFAULT_BOLD)

                val dataSet = PieDataSet(entries, "Asupan Kalori Harian")

                val colors: ArrayList<Int> = ArrayList()
                colors.add(resources.getColor(R.color.green)) // Warna untuk kalori terkonsumsi
                colors.add(resources.getColor(R.color.cream)) // Warna untuk sisa kalori

                dataSet.colors = colors
                dataSet.setDrawIcons(false)
                dataSet.sliceSpace = 3f
                dataSet.iconsOffset = MPPointF(0f, 40f)
                dataSet.selectionShift = 5f

                val data = PieData(dataSet)
                data.setValueFormatter(PercentFormatter(pieChart))
                data.setValueTextSize(15f)
                data.setValueTypeface(Typeface.DEFAULT_BOLD)
                data.setValueTextColor(Color.RED)

                pieChart.data = data
                pieChart.highlightValues(null)
                pieChart.invalidate()

                pieChart.centerText = "${consumedCalories} \n dari \n $targetCalories Kcal"
                // Atur styling untuk center text jika perlu
                pieChart.setCenterTextSize(18f)
                pieChart.setCenterTextTypeface(Typeface.DEFAULT_BOLD)
                pieChart.setCenterTextColor(Color.RED)

            }

        binding.cardSetting.setOnClickListener {
            val intent = Intent(this, SettingActivity::class.java)
            startActivity(intent)
        }

        binding.cardScan.setOnClickListener {
            val intent = Intent(this, CameraActivity::class.java)
            startActivity(intent)
        }

        binding.cardHistory.setOnClickListener {
            val intent = Intent(this, HistoryActivity::class.java)
            startActivity(intent)
        }

        binding.cardChat.setOnClickListener {
            val intent = Intent(this, ChatActivity::class.java)
            startActivity(intent)
        }
    }

    private fun getMillisFromDate(dateInMillis: Long): Long {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = dateInMillis

        // Set hour, minute, second, and millisecond to zero to get the start of the day
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        return calendar.timeInMillis
    }
}
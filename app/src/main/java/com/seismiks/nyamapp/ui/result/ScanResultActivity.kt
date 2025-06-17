package com.seismiks.nyamapp.ui.result

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.seismiks.nyamapp.R
import com.seismiks.nyamapp.ViewModelFactory
import com.seismiks.nyamapp.data.local.ScanResult
import com.seismiks.nyamapp.data.remote.response.MealData
import com.seismiks.nyamapp.databinding.ActivityScanResultBinding
import com.seismiks.nyamapp.ui.ContainerActivity
import com.seismiks.nyamapp.ui.camera.CameraActivity
import com.seismiks.nyamapp.utils.CALORIES
import java.io.File
import java.io.FileOutputStream
import java.util.Calendar

class ScanResultActivity : AppCompatActivity() {
    private lateinit var binding: ActivityScanResultBinding

    private lateinit var viewModel: ScanResultViewModel

    private lateinit var imageUri: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityScanResultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val factory = ViewModelFactory.getInstance(this)
        viewModel = ViewModelProvider(this, factory).get(ScanResultViewModel::class.java)

        imageUri = intent.getStringExtra(EXTRA_IMAGE_URI).toString()
        Glide.with(this)
            .load(Uri.parse(imageUri))
            .into(binding.ivResult)

        val mealData = if (Build.VERSION.SDK_INT >= 33) {
            intent.getParcelableExtra(EXTRA_RESULT, MealData::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra<MealData>(EXTRA_RESULT)
        }

        if (mealData != null) {
            binding.tvFoodName.text = mealData.foodType
            binding.tvFat.text = getString(R.string.grams, mealData.fat.toString())
            binding.tvCarbo.text = getString(R.string.grams, mealData.carbohydrates.toString())
            binding.tvProtein.text = getString(R.string.grams, mealData.protein.toString())
            binding.tvCalorie.text = getString(R.string.kcal, mealData.calories.toString())
            binding.tvWeigth.text = getString(R.string.grams, mealData.grams.toString())
        }

        binding.btnRetry.setOnClickListener {
            saveResult()
            viewModel.loadCaloriesToday(System.currentTimeMillis().toString())

            viewModel.totalCalories.observe(this) { totalKalori ->
                val intent = Intent(this, CameraActivity::class.java)
                intent.putExtra(CALORIES, totalKalori.toString())
                startActivity(intent)
                finish()
            }

        }

        binding.btnFinish.setOnClickListener {
            saveResult()
            viewModel.loadCaloriesToday(System.currentTimeMillis().toString())

            viewModel.totalCalories.observe(this) { totalKalori ->
                val intent = Intent(this, ContainerActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                intent.putExtra(CALORIES, totalKalori.toString())
                startActivity(intent)
                finish()
            }

            viewModel.saved.observe(this) { event ->
                val saved = event.getContentIfNotHandled()
                if (saved == true) {
                    Toast.makeText(this, "Scan Result Saved", Toast.LENGTH_SHORT).show()
                }
            }
        }


    }

    private fun saveResult() {
        val mealData = if (Build.VERSION.SDK_INT >= 33) {
            intent.getParcelableExtra(EXTRA_RESULT, MealData::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra<MealData>(EXTRA_RESULT)
        }

        val foodName = mealData?.foodType
        val kalori = mealData?.calories
        val lemak = mealData?.fat
        val karbo = mealData?.carbohydrates
        val protein = mealData?.protein
        val weight = mealData?.grams

        if (foodName == null || kalori == null || lemak == null || karbo == null || protein == null) {
            Toast.makeText(this, "Invalid food data", Toast.LENGTH_SHORT).show()
            return
        }

        val result = ScanResult(
            foodName = foodName,
            kalori = kalori,
            lemak = lemak,
            karbo = karbo,
            protein = protein,
            weight = weight,
            resultAddedInMillis = getMillisFromDate(System.currentTimeMillis()),
            image = copyUriToInternalStorage(this, Uri.parse(imageUri)).toString()
        )

        if (result != null) {
            viewModel.insertResult(result)
        }
    }

    fun copyUriToInternalStorage(context: Context, sourceUri: Uri): Uri {
        val inputStream = context.contentResolver.openInputStream(sourceUri)
        val fileName = "image_${System.currentTimeMillis()}.jpg"
        val file = File(context.filesDir, fileName)
        val outputStream = FileOutputStream(file)

        inputStream?.copyTo(outputStream)
        inputStream?.close()
        outputStream.close()

        return Uri.fromFile(file)
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

    companion object {
        const val EXTRA_IMAGE_URI = "extra_image_uri"
        const val EXTRA_RESULT = "extra_result"
    }
}
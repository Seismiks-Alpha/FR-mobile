package com.reza.nyamapp.ui.camera

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.reza.nyamapp.R
import com.reza.nyamapp.databinding.ActivityCameraBinding
import com.reza.nyamapp.ui.result.ScanResultActivity
import com.reza.nyamapp.utils.getImageUri

class CameraActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCameraBinding
    private lateinit var viewModel: CameraViewModel

    private var currentImageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityCameraBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)

        setContentView(binding.root)

        setSupportActionBar(binding.myToolbar)
        supportActionBar?.title = getString(R.string.scan_food)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        viewModel = CameraViewModel()

        binding.btnCamera.setOnClickListener { startCamera() }
        binding.btnUpload.setOnClickListener { uploadImage() }
        binding.btnGallery.setOnClickListener { startGallery() }

        viewModel.imageUri?.let {
            currentImageUri = it
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

    private fun startCamera() {
        currentImageUri = getImageUri(this)
        launcherIntentCamera.launch(currentImageUri!!)
    }

    private val launcherIntentCamera = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { isSuccess ->
        if (isSuccess) {
            showImage()
        } else {
            currentImageUri = null
        }
    }

    private fun showImage() {
        currentImageUri?.let {
            Log.d("Image URI", "showImage: $it")
            binding.ivPreview.visibility = View.VISIBLE
            binding.tvPreview.visibility = View.INVISIBLE
            binding.ivPreview.setImageURI(it)
            viewModel.imageUri = it
        }
    }

    private fun uploadImage() {
        if (currentImageUri == null) {
            Toast.makeText(this, "No image selected", Toast.LENGTH_SHORT).show()
            return
        } else {
            val intent = Intent(this, ScanResultActivity::class.java).apply {
                putExtra(ScanResultActivity.EXTRA_IMAGE_URI, currentImageUri.toString())
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            startActivity(intent)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}

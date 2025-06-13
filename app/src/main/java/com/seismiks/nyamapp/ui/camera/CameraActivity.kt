package com.seismiks.nyamapp.ui.camera

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.seismiks.nyamapp.R
import com.seismiks.nyamapp.ViewModelFactory
import com.seismiks.nyamapp.data.Result
import com.seismiks.nyamapp.databinding.ActivityCameraBinding
import com.seismiks.nyamapp.ui.result.ScanResultActivity
import com.seismiks.nyamapp.utils.getImageUri
import com.seismiks.nyamapp.utils.reduceFileImage
import com.seismiks.nyamapp.utils.uriToFile
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream
import java.util.UUID
import kotlin.math.min

class CameraActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCameraBinding

    private var currentImageUri: Uri? = null

    private lateinit var viewModel: CameraViewModel

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityCameraBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)

        setContentView(binding.root)

        auth = Firebase.auth

        val factory: ViewModelFactory = ViewModelFactory.getInstance(this)
        viewModel = ViewModelProvider(this, factory)[CameraViewModel::class.java]

        if (savedInstanceState == null) { // Only restore if not already handled by system restore
            viewModel.croppedImageUri?.let {
                currentImageUri = viewModel.getUri()
                showImage()
            }
        }

        setSupportActionBar(binding.myToolbar)
        supportActionBar?.title = getString(R.string.scan_food)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        binding.btnCamera.setOnClickListener { startCamera() }
        binding.btnUpload.setOnClickListener {
            if (currentImageUri != null) {
                val intent = Intent(this, ScanResultActivity::class.java).apply {
                    putExtra(ScanResultActivity.EXTRA_IMAGE_URI, currentImageUri.toString())
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    startActivity(this)
                    finish()
                }
            } else {
                Toast.makeText(this, "Tidak ada gambar yang bisa diproses", Toast.LENGTH_SHORT).show()
            }
        }
        binding.btnGallery.setOnClickListener { startGallery() }

    }

    private fun startGallery() {
        launcherGallery.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

    private val launcherGallery = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            performAutoCropAndShow(uri)
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
            currentImageUri?.let { uri ->
                performAutoCropAndShow(uri)
            }
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
            viewModel.setUri(it)
        }
    }

    private fun uploadImage() {
        if (currentImageUri == null) {
            Toast.makeText(this, "No image selected", Toast.LENGTH_SHORT).show()
            return
        } else {
            val user = auth.currentUser
            user?.getIdToken(true)?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val idToken = task.result?.token
                    if (idToken != null) {
                        handleRecognizeImage(idToken)
                    } else {
                        Toast.makeText(this, "Token not found", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun performAutoCropAndShow(sourceUri: Uri) {
        try {
            // 1. Decode URI menjadi Bitmap
            val inputStream = contentResolver.openInputStream(sourceUri)
            val originalBitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            // 2. Lakukan cropping di tengah (center crop)
            val width = originalBitmap.width
            val height = originalBitmap.height
            val size = min(width, height)
            val x = if (width > size) (width - size) / 2 else 0
            val y = if (height > size) (height - size) / 2 else 0
            val croppedBitmap = Bitmap.createBitmap(originalBitmap, x, y, size, size)

            // 3. Resize bitmap ke ukuran maksimal 300x300, sama seperti sebelumnya
            val scaledBitmap = Bitmap.createScaledBitmap(croppedBitmap, 300, 300, true)

            // 4. Buat file tujuan baru untuk menyimpan gambar yang sudah dicrop
            val uniqueFileName = "cropped_image_${UUID.randomUUID()}.jpg"
            val destinationFile = File(cacheDir, uniqueFileName)
            val outputStream = FileOutputStream(destinationFile)
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
            outputStream.close()

            // 5. Update URI dan tampilkan gambar
            val newUri = Uri.fromFile(destinationFile)
            currentImageUri = newUri // Simpan URI baru
            viewModel.setUri(newUri) // Simpan URI ke ViewModel
            showImage()

        } catch (e: Exception) {
            Log.e("AutoCrop", "Gagal melakukan crop otomatis", e)
            Toast.makeText(this, "Gagal memproses gambar", Toast.LENGTH_SHORT).show()
        }
    }

    private fun handleRecognizeImage(token: String) {
        viewModel.croppedImageUri.observe(this) { imageUri ->
            imageUri?.let {
                val imageFile = uriToFile(imageUri, this).reduceFileImage()
                val requestFileImage = imageFile.asRequestBody("image/jpeg".toMediaTypeOrNull())
                val imageMultipart =
                    MultipartBody.Part.createFormData("food_image", imageFile.name, requestFileImage)
                viewModel.recognizeFood(token, imageMultipart).observe(this) { result ->
                    if (result != null) {
                        when (result) {
                            is Result.Loading -> {
                                binding.progressBar.visibility = View.VISIBLE
                            }

                            is Result.Success -> {
                                binding.progressBar.visibility = View.GONE
                                Toast.makeText(this, "Gambar berhasil diproses", Toast.LENGTH_SHORT)
                                    .show()
                                val intent = Intent(this, ScanResultActivity::class.java).apply {
                                    putExtra(
                                        ScanResultActivity.EXTRA_IMAGE_URI,
                                        imageUri.toString()
                                    )
                                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                                startActivity(intent)
                                finish()
                            }

                            is Result.Error -> {
                                binding.progressBar.visibility = View.GONE
                                Toast.makeText(this, result.error, Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
                Log.d("Image URI", "handleRecognizeImage: $imageUri")
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}

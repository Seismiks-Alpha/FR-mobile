package com.seismiks.nyamapp.ui.camera

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
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

        if (savedInstanceState == null) {
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
            uploadImage()
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
        val imageUri = viewModel.getUri()
        if (imageUri == null) {
            Toast.makeText(this, "Tidak ada gambar yang dipilih", Toast.LENGTH_SHORT)
                .show()
            return
        }

        val user = auth.currentUser
        user?.getIdToken(true)?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val idToken = task.result?.token
                if (idToken != null) {
                    val imageFile = uriToFile(imageUri, this)

                    if (imageFile.length() == 0L) {
                        Toast.makeText(this, "Gagal memproses gambar, silakan coba lagi.", Toast.LENGTH_SHORT).show()
                        return@addOnCompleteListener
                    }

                    val requestFileImage =
                        imageFile.asRequestBody("image/jpeg".toMediaTypeOrNull())
                    val imageMultipart = MultipartBody.Part.createFormData(
                        "image",
                        imageFile.name,
                        requestFileImage
                    )

                    viewModel.recognizeFood(idToken, imageMultipart)
                        .observe(this) { result ->
                            if (result != null) {
                                when (result) {
                                    is Result.Loading -> {
                                        binding.progressBar.visibility = View.VISIBLE
                                    }

                                    is Result.Success -> {
                                        binding.progressBar.visibility = View.GONE
                                        val responseData = result.data

                                        if (responseData != null) {
                                            if (responseData.data.isNotEmpty()) {
                                                val mealDataResult =
                                                    responseData?.data?.firstOrNull()

                                                val intent =
                                                    Intent(
                                                        this,
                                                        ScanResultActivity::class.java
                                                    ).apply {
                                                        putExtra(
                                                            ScanResultActivity.EXTRA_RESULT,
                                                            mealDataResult
                                                        )

                                                        putExtra(
                                                            ScanResultActivity.EXTRA_IMAGE_URI,
                                                            imageUri.toString()
                                                        )
                                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                                    }
                                                Log.d(
                                                    "CameraActivity",
                                                    result.data.message
                                                )
                                                startActivity(intent)
                                                finish()
                                            } else {
                                                Toast.makeText(
                                                    this,
                                                    "Makanan tidak dapat dikenali, coba lagi.",
                                                    Toast.LENGTH_LONG
                                                ).show()
                                            }
                                        }
                                    }

                                    is Result.Error -> {
                                        binding.progressBar.visibility = View.GONE
                                        Toast.makeText(
                                            this,
                                            result.error,
                                            Toast.LENGTH_SHORT
                                        )
                                            .show()
                                        Log.e(
                                            "CameraActivity",
                                            "Error: ${result.error}"
                                        )
                                    }
                                }
                            }
                        }

                } else {
                    Toast.makeText(
                        this,
                        "Gagal mendapatkan token autentikasi",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                Toast.makeText(this, "Gagal memverifikasi pengguna", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }


    private fun performAutoCropAndShow(sourceUri: Uri) {
        try {
            val inputStream = contentResolver.openInputStream(sourceUri)
            val originalBitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            if (originalBitmap == null) {
                Toast.makeText(this, "Gagal memuat gambar", Toast.LENGTH_SHORT).show()
                return
            }

            val rotatedBitmap = rotateBitmapIfRequired(originalBitmap, sourceUri)

            val width = rotatedBitmap.width
            val height = rotatedBitmap.height
            val size = min(width, height)
            val x = if (width > size) (width - size) / 2 else 0
            val y = if (height > size) (height - size) / 2 else 0
            val croppedBitmap = Bitmap.createBitmap(rotatedBitmap, x, y, size, size)

            val scaledBitmap = Bitmap.createScaledBitmap(croppedBitmap, 300, 300, true)

            val uniqueFileName = "cropped_image_${UUID.randomUUID()}.jpg"
            val destinationFile = File(cacheDir, uniqueFileName)
            val outputStream = FileOutputStream(destinationFile)
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
            outputStream.close()

            val newUri = Uri.fromFile(destinationFile)
            currentImageUri = newUri
            viewModel.setUri(newUri)
            showImage()

        } catch (e: Exception) {
            Log.e("AutoCrop", "Gagal melakukan crop otomatis", e)
            Toast.makeText(this, "Gagal memproses gambar", Toast.LENGTH_SHORT).show()
        }
    }

    private fun rotateBitmapIfRequired(bitmap: Bitmap, sourceUri: Uri): Bitmap {
        val inputStream = contentResolver.openInputStream(sourceUri) ?: return bitmap
        val exif = inputStream.use { ExifInterface(it) }
        val orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)

        val matrix = Matrix()
        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
            else -> return bitmap
        }

        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}

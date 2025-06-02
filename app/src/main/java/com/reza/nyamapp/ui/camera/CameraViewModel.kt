package com.reza.nyamapp.ui.camera

import android.net.Uri
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.reza.nyamapp.data.remote.RemoteRepository
import okhttp3.MultipartBody

class CameraViewModel(private val remoteRepository: RemoteRepository) : ViewModel() {
    private var _croppedImageUri: MutableLiveData<Uri?> = MutableLiveData<Uri?>()
    val croppedImageUri: MutableLiveData<Uri?> get() = _croppedImageUri

    fun recognizeFood(token: String, file: MultipartBody.Part) =
        remoteRepository.recognizeFood(token, file)

    fun getUri(): Uri? = croppedImageUri.value

    fun setUri(uri: Uri?) {
        _croppedImageUri.value = uri
    }
}
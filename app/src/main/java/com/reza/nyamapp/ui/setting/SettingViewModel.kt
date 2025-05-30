package com.reza.nyamapp.ui.setting

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.reza.nyamapp.data.Result
import com.reza.nyamapp.data.remote.RemoteRepository
import com.reza.nyamapp.data.remote.retrofit.Photo

class SettingViewModel(private val repository: RemoteRepository) : ViewModel() {
    var imageUri: Uri? = null

    fun getProfileInfo(tokenId: String) =
        repository.getProfile(tokenId)

    fun putPhoto(tokenId: String, photo: Photo): LiveData<Result<Photo>> {
        return repository.putPhoto(tokenId, photo)
    }
}
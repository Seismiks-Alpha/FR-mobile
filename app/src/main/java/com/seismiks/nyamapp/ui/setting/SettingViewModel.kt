package com.seismiks.nyamapp.ui.setting

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.seismiks.nyamapp.data.Result
import com.seismiks.nyamapp.data.remote.RemoteRepository
import com.seismiks.nyamapp.data.remote.response.ProfileResponse

class SettingViewModel(private val repository: RemoteRepository) : ViewModel() {

    // MutableLiveData untuk menampung hasil sinkronisasi profil
    private val _profileResult = MutableLiveData<Result<ProfileResponse>>()

    // LiveData yang akan diobservasi oleh HomeActivity
    val profile: LiveData<Result<ProfileResponse>> = _profileResult

    fun getProfileInfo(tokenId: String) {
        _profileResult.value = Result.Loading
        repository.getProfile(tokenId).observeForever { result -> _profileResult.value = result }
    }
}
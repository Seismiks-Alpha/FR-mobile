package com.seismiks.nyamapp.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.seismiks.nyamapp.data.Result
import com.seismiks.nyamapp.data.local.ResultRepository
import com.seismiks.nyamapp.data.remote.RemoteRepository
import com.seismiks.nyamapp.data.remote.response.ProfileResponse

class HomeViewModel(
    private val repository: ResultRepository,
    private val remoteRepository: RemoteRepository
) : ViewModel() {

    // MutableLiveData untuk menampung hasil sinkronisasi profil
    private val _profileResult = MutableLiveData<Result<ProfileResponse>>()

    // LiveData yang akan diobservasi oleh HomeActivity
    val profile: LiveData<Result<ProfileResponse>> = _profileResult

    fun getCaloriesToday(date: String): LiveData<Int> {
        return repository.getCaloriesToday(date)
    }

    fun getProfileInfo(tokenId: String) {
        _profileResult.value = Result.Loading
        remoteRepository.getProfile(tokenId).observeForever { result -> _profileResult.value = result }
    }
}
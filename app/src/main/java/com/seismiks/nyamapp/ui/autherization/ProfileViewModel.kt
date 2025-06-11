package com.seismiks.nyamapp.ui.autherization

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.seismiks.nyamapp.data.Result
import com.seismiks.nyamapp.data.remote.RemoteRepository
import com.seismiks.nyamapp.data.remote.response.SyncProfileResponse
import kotlinx.coroutines.launch

class ProfileViewModel(private val remoteRepository: RemoteRepository) : ViewModel() {

    private val _profileResult = MutableLiveData<Result<SyncProfileResponse>>()

    val profile: LiveData<Result<SyncProfileResponse>> = _profileResult

    fun syncProfile(tokenId: String) {
        viewModelScope.launch {
            _profileResult.value = Result.Loading
            try {
                val directResult: Result<SyncProfileResponse> = remoteRepository.syncProfile(tokenId)
                _profileResult.value = directResult
            } catch (e: Exception) {
                _profileResult.value = Result.Error(e.message ?: "Terjadi kesalahan saat sinkronisasi profil")
                Log.e("ViewModel", "Error in syncProfile: ${e.message}", e)
            }
        }
    }
}
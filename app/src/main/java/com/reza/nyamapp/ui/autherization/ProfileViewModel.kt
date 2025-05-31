package com.reza.nyamapp.ui.autherization

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.reza.nyamapp.data.Result
import com.reza.nyamapp.data.remote.RemoteRepository
import com.reza.nyamapp.data.remote.response.SyncProfileResponse
import kotlinx.coroutines.launch

class ProfileViewModel(private val remoteRepository: RemoteRepository) : ViewModel() {
//    private val _syncLoading = MutableLiveData<Boolean>(false)
//    val syncLoading: LiveData<Boolean> get() = _syncLoading
//
//    private val _syncSuccess = MutableLiveData<Boolean>(false)
//    val syncSuccess: LiveData<Boolean> get() = _syncSuccess
//
//    private val _syncError = MutableLiveData<String?>()
//    val syncError: LiveData<String?> get() = _syncError
//
//    private val _height = MutableLiveData<Int?>()
//    val height: LiveData<Int?> get() = _height
//
//    private val _weight = MutableLiveData<Int?>()
//    val weight: LiveData<Int?> get() = _weight

    // MutableLiveData untuk menampung hasil sinkronisasi profil
    private val _profileResult = MutableLiveData<Result<SyncProfileResponse>>()

    // LiveData yang akan diobservasi oleh HomeActivity
    val profile: LiveData<Result<SyncProfileResponse>> = _profileResult

//    fun syncProfile(userId: String) {
//        _syncLoading.value = true
//        _syncSuccess.value = false
//        _syncError.value = null
//
//        viewModelScope.launch {
//            try {
//                val liveDataResult = remoteRepository.syncProfile(userId)
//                liveDataResult.observeForever(object : Observer<Result<SyncProfileResponse>> {
//                    override fun onChanged(result: Result<SyncProfileResponse>) {
//                        when (result) {
//                            is Result.Loading -> {
//                            }
//
//                            is Result.Success -> {
//                                val profile = result.data.user?.profile
//                                _height.value = profile?.height
//                                _weight.value = profile?.weight
//                                Log.d("ProfileViewModel", "Success: ${result.data}")
//                                Log.d("ProfileViewModel", "Height: ${_height.value}")
//                                Log.d("ProfileViewModel", "Weight: ${_weight.value}")
//                                _syncLoading.value = false
//                                _syncSuccess.value = true
//                            }
//
//                            is Result.Error -> {
//                                Log.e("ProfileViewModel", "Error: ${result.error}")
//                                _syncLoading.value = false
//                                _syncError.value = result.error
//                            }
//
//                            null -> {
//                                Log.e("ProfileViewModel", "Result is null")
//                                _syncLoading.value = false
//                                _syncError.value = "Result is null"
//                            }
//                        }
//                        liveDataResult.removeObserver(this)
//                    }
//                })
//            } catch (e: Exception) {
//                Log.e("ProfileViewModel", "Error: ${e.message}", e)
//                _syncLoading.value = false
//                _syncError.value = e.message
//            }
//        }
//    }

    fun syncProfile(tokenId: String) { // Tidak perlu suspend lagi di sini
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
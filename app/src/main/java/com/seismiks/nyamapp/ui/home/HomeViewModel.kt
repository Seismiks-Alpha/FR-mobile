package com.seismiks.nyamapp.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.seismiks.nyamapp.data.Result
import com.seismiks.nyamapp.data.local.ResultRepository
import com.seismiks.nyamapp.data.remote.RemoteRepository
import com.seismiks.nyamapp.data.remote.response.HistoryTodayResponse
import com.seismiks.nyamapp.data.remote.response.InfoToday
import com.seismiks.nyamapp.data.remote.response.ProfileResponse

class HomeViewModel(
    private val remoteRepository: RemoteRepository
) : ViewModel() {

    private val _profileResult = MutableLiveData<Result<ProfileResponse>>()

    val profile: LiveData<Result<ProfileResponse>> = _profileResult

    private val _caloriesToday = MutableLiveData<Result<HistoryTodayResponse>>()
    val caloriesToday: LiveData<Result<HistoryTodayResponse>> = _caloriesToday

    fun getCaloriesToday(tokenId: String) {
        _caloriesToday.value = Result.Loading
        remoteRepository.getHistoryTodayAndYesterday(tokenId).observeForever { caloriesToday ->
            _caloriesToday.value = caloriesToday
        }
    }

    fun getProfileInfo(tokenId: String) {
        _profileResult.value = Result.Loading
        remoteRepository.getProfile(tokenId)
            .observeForever { result -> _profileResult.value = result }
    }
}
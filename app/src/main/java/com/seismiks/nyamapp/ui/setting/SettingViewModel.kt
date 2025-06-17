package com.seismiks.nyamapp.ui.setting

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.seismiks.nyamapp.data.Result
import com.seismiks.nyamapp.data.remote.RemoteRepository
import com.seismiks.nyamapp.data.remote.response.LeaderboardResponse
import com.seismiks.nyamapp.data.remote.response.ProfileResponse

class SettingViewModel(private val repository: RemoteRepository) : ViewModel() {

    // 1. LiveData untuk Profile
    private val _profileResult = MediatorLiveData<Result<ProfileResponse>>()
    val profile: LiveData<Result<ProfileResponse>> = _profileResult

    // 2. LiveData untuk Leaderboard
    private val _leaderboardResult = MediatorLiveData<Result<LeaderboardResponse>>()
    val leaderboard: LiveData<Result<LeaderboardResponse>> = _leaderboardResult


    // 3. Satu fungsi utama yang dipanggil oleh Fragment
    fun fetchAllData(tokenId: String) {
        // Mulai dengan mengambil data profil
        fetchProfileInfo(tokenId)
    }

    private fun fetchProfileInfo(tokenId: String) {
        _profileResult.value = Result.Loading
        val source = repository.getProfile(tokenId)

        _profileResult.addSource(source) { result ->
            _profileResult.value = result

            // Jika sudah tidak loading lagi (Success atau Error)
            if (result !is Result.Loading) {
                // Hentikan pengamatan agar tidak ada update ganda
                _profileResult.removeSource(source)

                // JIKA profil berhasil didapat, LANJUTKAN ambil data leaderboard
                if (result is Result.Success) {
                    fetchLeaderboard(tokenId)
                }
            }
        }
    }

    private fun fetchLeaderboard(tokenId: String) {
        _leaderboardResult.value = Result.Loading
        val source = repository.getLeaderboard(tokenId)

        _leaderboardResult.addSource(source) { result ->
            _leaderboardResult.value = result

            // Jika sudah tidak loading, hentikan pengamatan
            if (result !is Result.Loading) {
                _leaderboardResult.removeSource(source)
            }
        }
    }
}
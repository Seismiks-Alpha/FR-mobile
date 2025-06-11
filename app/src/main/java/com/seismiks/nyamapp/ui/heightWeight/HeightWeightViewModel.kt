package com.seismiks.nyamapp.ui.heightWeight

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.seismiks.nyamapp.data.Result
import com.seismiks.nyamapp.data.remote.RemoteRepository
import com.seismiks.nyamapp.data.remote.retrofit.ProfileUser

class HeightWeightViewModel(private val repository: RemoteRepository) : ViewModel() {

    fun updateProfileUpdateAndStatus(token: String, profile: ProfileUser): LiveData<Result<ProfileUser>> {
        return repository.putProfile(token, profile)
    }

    fun getProfileInfo(tokenId: String) =
        repository.getProfile(tokenId)
}
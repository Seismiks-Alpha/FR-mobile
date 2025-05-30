package com.reza.nyamapp.ui.heightWeight

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.reza.nyamapp.data.Result
import com.reza.nyamapp.data.remote.RemoteRepository
import com.reza.nyamapp.data.remote.retrofit.ProfileUser

class HeightWeightViewModel(private val repository: RemoteRepository) : ViewModel() {

    fun updateProfileUpdateAndStatus(token: String, profile: ProfileUser): LiveData<Result<ProfileUser>> {
        return repository.putProfile(token, profile)
    }

    fun getProfileInfo(tokenId: String) =
        repository.getProfile(tokenId)
}
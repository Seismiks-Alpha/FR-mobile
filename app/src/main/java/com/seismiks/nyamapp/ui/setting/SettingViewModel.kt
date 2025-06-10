package com.seismiks.nyamapp.ui.setting

import androidx.lifecycle.ViewModel
import com.seismiks.nyamapp.data.remote.RemoteRepository

class SettingViewModel(private val repository: RemoteRepository) : ViewModel() {

    fun getProfileInfo(tokenId: String) =
        repository.getProfile(tokenId)
}
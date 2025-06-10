package com.seismiks.nyamapp

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.seismiks.nyamapp.data.local.ResultRepository
import com.seismiks.nyamapp.data.remote.RemoteRepository
import com.seismiks.nyamapp.ui.autherization.ProfileViewModel
import com.seismiks.nyamapp.ui.camera.CameraViewModel
import com.seismiks.nyamapp.ui.chat.ChatViewModel
import com.seismiks.nyamapp.ui.detail.HistoryDetailViewModel
import com.seismiks.nyamapp.ui.heightWeight.HeightWeightViewModel
import com.seismiks.nyamapp.ui.history.HistoryViewModel
import com.seismiks.nyamapp.ui.home.HomeViewModel
import com.seismiks.nyamapp.ui.result.ScanResultViewModel
import com.seismiks.nyamapp.ui.setting.SettingViewModel
import com.seismiks.nyamapp.utils.ModelInjection

class ViewModelFactory private constructor(
    private val resultRepository: ResultRepository,
    private val RemoteRepository: RemoteRepository
) :
    ViewModelProvider.NewInstanceFactory() {

    companion object {
        fun getInstance(context: Context): ViewModelFactory {
            val (resultRepository, chatRepository) = ModelInjection.provideRepository(context)
            return ViewModelFactory(resultRepository, chatRepository)
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        when {
            modelClass.isAssignableFrom(ScanResultViewModel::class.java) -> {
                ScanResultViewModel(resultRepository) as T
            }

            modelClass.isAssignableFrom(CameraViewModel::class.java) -> {
                CameraViewModel(RemoteRepository) as T
            }

            modelClass.isAssignableFrom(HistoryViewModel::class.java) -> {
                HistoryViewModel(resultRepository) as T
            }

            modelClass.isAssignableFrom(HistoryDetailViewModel::class.java) -> {
                HistoryDetailViewModel(resultRepository) as T
            }

            modelClass.isAssignableFrom(HomeViewModel::class.java) -> {
                HomeViewModel(resultRepository, RemoteRepository) as T
            }

            modelClass.isAssignableFrom(ChatViewModel::class.java) -> {
                ChatViewModel(RemoteRepository) as T
            }

            modelClass.isAssignableFrom(ProfileViewModel::class.java) -> {
                ProfileViewModel(RemoteRepository) as T
            }

            modelClass.isAssignableFrom(HeightWeightViewModel::class.java) -> {
                HeightWeightViewModel(RemoteRepository) as T
            }

            modelClass.isAssignableFrom(SettingViewModel::class.java) -> {
                SettingViewModel(RemoteRepository) as T
            }

            else -> throw Throwable("Unknown ViewModel class: " + modelClass.name)
        }
}

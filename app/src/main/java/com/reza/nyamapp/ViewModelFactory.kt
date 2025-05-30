package com.reza.nyamapp

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.reza.nyamapp.data.local.ResultRepository
import com.reza.nyamapp.data.remote.RemoteRepository
import com.reza.nyamapp.ui.autherization.ProfileViewModel
import com.reza.nyamapp.ui.chat.ChatViewModel
import com.reza.nyamapp.ui.detail.HistoryDetailViewModel
import com.reza.nyamapp.ui.heightWeight.HeightWeightViewModel
import com.reza.nyamapp.ui.history.HistoryViewModel
import com.reza.nyamapp.ui.home.HomeViewModel
import com.reza.nyamapp.ui.result.ScanResultViewModel
import com.reza.nyamapp.ui.setting.SettingViewModel
import com.reza.nyamapp.utils.ModelInjection

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

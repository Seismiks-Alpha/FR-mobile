package com.seismiks.nyamapp.ui.history

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.paging.PagingData
import com.seismiks.nyamapp.R
import com.seismiks.nyamapp.data.local.ResultRepository
import com.seismiks.nyamapp.data.local.ScanResult
import com.seismiks.nyamapp.utils.Event

class HistoryViewModel(private val repository: ResultRepository) : ViewModel() {

    val allResults: LiveData<PagingData<ScanResult>> = repository.getAllResults()

    private val _snackbarText = MutableLiveData<Event<Int>>()
    val snackbarText: LiveData<Event<Int>> = _snackbarText

    private val _undo = MutableLiveData<Event<ScanResult>>()
    val undo: LiveData<Event<ScanResult>> = _undo

    fun deleteResult(result: ScanResult) {
        repository.deleteResult(result)
        _snackbarText.value = Event(R.string.berhasil_dihapus)
        _undo.value = Event(result)
    }

    fun insertResult(scanResult: ScanResult) {
        repository.insertResult(scanResult)
    }
}
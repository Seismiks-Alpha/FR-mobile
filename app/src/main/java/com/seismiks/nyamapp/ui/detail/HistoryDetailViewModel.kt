package com.seismiks.nyamapp.ui.detail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.switchMap
import com.seismiks.nyamapp.data.local.ResultRepository
import com.seismiks.nyamapp.data.local.ScanResult
import com.seismiks.nyamapp.utils.Event

class HistoryDetailViewModel(private val resultRepository: ResultRepository) : ViewModel() {

    private val _resultId: MutableLiveData<Int> = MutableLiveData()
    val scanResult: LiveData<ScanResult?> = _resultId.switchMap { id ->
        resultRepository.getResultById(id)
    }

    private val _deleted: MutableLiveData<Event<Boolean>> = MutableLiveData(Event(false))
    val deleted: LiveData<Event<Boolean>>
        get() = _deleted

    fun setResultId(id: Int) {
        if (_resultId.value != id) {
            _resultId.value = id
        }
    }

    fun deleteResult() {
        scanResult.value?.let {
            resultRepository.deleteResult(it)
            _deleted.value = Event(true)
        }
    }
}
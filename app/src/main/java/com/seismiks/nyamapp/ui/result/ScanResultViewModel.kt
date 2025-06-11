package com.seismiks.nyamapp.ui.result

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.seismiks.nyamapp.data.local.ResultRepository
import com.seismiks.nyamapp.data.local.ScanResult
import com.seismiks.nyamapp.utils.Event

class ScanResultViewModel(private val repository: ResultRepository) : ViewModel() {

    private val _totalCalories = MutableLiveData<Double>()
    val totalCalories: LiveData<Double> = _totalCalories

    private val _saved: MutableLiveData<Event<Boolean>> = MutableLiveData(Event(false))
    val saved: LiveData<Event<Boolean>>
        get() = _saved

    fun insertResult(result: ScanResult) {
        repository.insertResult(result)
        _saved.value = Event(true)
    }

    fun loadCaloriesToday(date: String) {
        repository.getCaloriesToday(date).observeForever { scanResults ->
            val total = scanResults
            _totalCalories.value = total.toDouble()
        }
    }
}
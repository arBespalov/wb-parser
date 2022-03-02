package com.automotivecodelab.wbgoodstracker.ui.chartfragment

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.automotivecodelab.wbgoodstracker.domain.GetOrdersChartDataUseCase
import com.automotivecodelab.wbgoodstracker.ui.Event
import kotlinx.coroutines.launch

class ChartViewModel(
    private val getOrdersChartDataUseCase: GetOrdersChartDataUseCase,
    private val itemId: String
) : ViewModel() {

    private val _chartData = MutableLiveData<List<Pair<Long, Int>>>()
    val chartData: LiveData<List<Pair<Long, Int>>> = _chartData

    private val _networkErrorEvent = MutableLiveData<Event<String>>()
    val networkErrorEvent: LiveData<Event<String>> = _networkErrorEvent

    private val _dataLoading = MutableLiveData<Boolean>()
    val dataLoading: LiveData<Boolean> = _dataLoading

    fun start() {
        viewModelScope.launch {
            _dataLoading.value = true
            getOrdersChartDataUseCase(itemId)
                .onFailure {
                    _networkErrorEvent.value = Event(it.message.toString())
                }
                .onSuccess {
                    _chartData.value = it
                }
            _dataLoading.value = false
        }
    }
}

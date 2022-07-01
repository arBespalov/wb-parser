package com.automotivecodelab.wbgoodstracker.ui.quantitychartfrag

import androidx.lifecycle.*
import com.automotivecodelab.wbgoodstracker.domain.repositories.GetQuantityChartDataUseCase
import com.automotivecodelab.wbgoodstracker.ui.Event
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.launch

class QuantityChartViewModel @AssistedInject constructor(
    getQuantityChartDataUseCase: GetQuantityChartDataUseCase,
    @Assisted itemId: String
) : ViewModel() {

    private val _chartData = MutableLiveData<List<Pair<Long, Int>>>()
    val chartData: LiveData<List<Pair<Long, Int>>> = _chartData

    private val _networkErrorEvent = MutableLiveData<Event<Throwable>>()
    val networkErrorEvent: LiveData<Event<Throwable>> = _networkErrorEvent

    private val _dataLoading = MutableLiveData<Boolean>()
    val dataLoading: LiveData<Boolean> = _dataLoading

    init {
        viewModelScope.launch {
            _dataLoading.value = true
            getQuantityChartDataUseCase(itemId)
                .onFailure {
                    _networkErrorEvent.value = Event(it)
                }
                .onSuccess {
                    _chartData.value = it
                }
            _dataLoading.value = false
        }
    }
}
package com.automotivecodelab.wbgoodstracker.ui.detailfrag

import androidx.lifecycle.*
import com.automotivecodelab.wbgoodstracker.domain.ObserveSingleItemUseCase
import com.automotivecodelab.wbgoodstracker.domain.RefreshSingleItemUseCase
import com.automotivecodelab.wbgoodstracker.domain.models.Item
import com.automotivecodelab.wbgoodstracker.domain.util.Result
import com.automotivecodelab.wbgoodstracker.ui.Event
import kotlinx.coroutines.*

class DetailViewModel(
    private val refreshSingleItemUseCase: RefreshSingleItemUseCase,
    observeSingleItemUseCase: ObserveSingleItemUseCase,
    private val itemId: String
) : ViewModel() {
    val item: LiveData<Item> = observeSingleItemUseCase(itemId)

    private val _confirmDeleteEvent = MutableLiveData<Event<String>>()
    val confirmDeleteEvent: LiveData<Event<String>> = _confirmDeleteEvent

    private val _editItemEvent = MutableLiveData<Event<String>>()
    val editItemEvent: LiveData<Event<String>> = _editItemEvent

    private val _updateErrorEvent = MutableLiveData<Event<String>>()
    val updateErrorEvent: LiveData<Event<String>> = _updateErrorEvent

    private val _dataLoading = MutableLiveData<Boolean>()
    val dataLoading: LiveData<Boolean> = _dataLoading

    private val _showOrdersChartEvent = MutableLiveData<Event<String>>()
    val showOrdersChartEvent: LiveData<Event<String>> = _showOrdersChartEvent

    fun confirmDelete() {
        _confirmDeleteEvent.value = Event(itemId)
    }

    fun editItem() {
        _editItemEvent.value = Event(itemId)
    }

    fun refreshItem() {
        viewModelScope.launch {
            _dataLoading.value = true
            if (item.value != null) {
                when (val result = refreshSingleItemUseCase(item.value!!)) {
                    is Result.Error ->
                        _updateErrorEvent.value =
                            Event(result.exception.message.toString())
                    is Result.Success -> { }
                }
            }
            _dataLoading.value = false
        }
    }

    fun showOrdersChart() {
        _showOrdersChartEvent.value = Event(itemId)
    }
}

package com.automotivecodelab.wbgoodstracker.ui.confirmremovedialogfrag

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.automotivecodelab.wbgoodstracker.domain.DeleteItemsUseCase
import com.automotivecodelab.wbgoodstracker.ui.Event
import kotlinx.coroutines.*

class ConfirmRemoveDialogViewModel(
    private val deleteItemsUseCase: DeleteItemsUseCase
) : ViewModel() {

    private val _taskCompletedEvent = MutableLiveData<Event<Unit>>()
    val taskCompletedEvent: LiveData<Event<Unit>> = _taskCompletedEvent

    private val _authorizationErrorEvent = MutableLiveData<Event<Unit>>()
    val authorizationErrorEvent: LiveData<Event<Unit>> = _authorizationErrorEvent

    fun deleteItems(itemsIdToDelete: Array<String>) {
        viewModelScope.launch {
            deleteItemsUseCase(itemsIdToDelete) // bad design
            _taskCompletedEvent.value = Event(Unit)
        }
    }
}

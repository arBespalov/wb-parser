package com.automotivecodelab.wbgoodstracker.ui.confirmremovedialogfrag

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.automotivecodelab.wbgoodstracker.domain.DeleteItemsUseCase
import com.automotivecodelab.wbgoodstracker.ui.Event
import kotlinx.coroutines.*
import javax.inject.Inject

class ConfirmRemoveDialogViewModel @Inject constructor(
    private val deleteItemsUseCase: DeleteItemsUseCase
) : ViewModel() {

    private val _taskCompletedEvent = MutableLiveData<Event<Unit>>()
    val taskCompletedEvent: LiveData<Event<Unit>> = _taskCompletedEvent

    fun deleteItems(itemsIdToDelete: Array<String>) {
        viewModelScope.launch {
            deleteItemsUseCase(itemsIdToDelete)
            _taskCompletedEvent.value = Event(Unit)
        }
    }
}

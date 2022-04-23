package com.automotivecodelab.wbgoodstracker.ui.confirmremovedialogfrag

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.automotivecodelab.wbgoodstracker.domain.DeleteItemsUseCase
import com.automotivecodelab.wbgoodstracker.ui.Event
import javax.inject.Inject
import kotlinx.coroutines.*

class ConfirmRemoveDialogViewModel @Inject constructor(
    private val deleteItemsUseCase: DeleteItemsUseCase
) : ViewModel() {

    private val _taskCompletedEvent = MutableLiveData<Event<Unit>>()
    val taskCompletedEvent: LiveData<Event<Unit>> = _taskCompletedEvent

    private val _authorizationErrorEvent = MutableLiveData<Event<Unit>>()
    val authorizationErrorEvent: LiveData<Event<Unit>> = _authorizationErrorEvent

    fun deleteItems(itemsIdToDelete: List<String>) {
        viewModelScope.launch {
            deleteItemsUseCase(
                itemsIdToDelete = itemsIdToDelete,
                onAuthenticationFailureCallback = { _authorizationErrorEvent.value = Event(Unit) }
            )
            _taskCompletedEvent.value = Event(Unit)
        }
    }
}

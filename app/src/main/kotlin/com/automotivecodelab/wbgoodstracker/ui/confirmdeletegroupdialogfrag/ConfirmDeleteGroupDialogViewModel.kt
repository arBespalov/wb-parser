package com.automotivecodelab.wbgoodstracker.ui.confirmdeletegroupdialogfrag

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.automotivecodelab.wbgoodstracker.domain.DeleteGroupUseCase
import com.automotivecodelab.wbgoodstracker.ui.Event
import javax.inject.Inject
import kotlinx.coroutines.launch

class ConfirmDeleteGroupDialogViewModel @Inject constructor(
    private val deleteGroupUseCase: DeleteGroupUseCase
) : ViewModel() {

    private val _taskCompletedEvent = MutableLiveData<Event<Unit>>()
    val taskCompletedEvent: LiveData<Event<Unit>> = _taskCompletedEvent

    fun deleteGroup(groupName: String) {
        viewModelScope.launch {
            deleteGroupUseCase(groupName)
            _taskCompletedEvent.value = Event(Unit)
        }
    }
}

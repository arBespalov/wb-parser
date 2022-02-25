package com.automotivecodelab.wbgoodstracker.ui.newgroupdialogfrag

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.automotivecodelab.wbgoodstracker.domain.CreateNewGroupUseCase
import com.automotivecodelab.wbgoodstracker.ui.Event

class NewGroupDialogViewModel(
    private val createNewGroupUseCase: CreateNewGroupUseCase
) : ViewModel() {

    private val _taskCompletedEvent = MutableLiveData<Event<Unit>>()
    val taskCompletedEvent: LiveData<Event<Unit>> = _taskCompletedEvent

    fun addGroup(groupName: String) {
        createNewGroupUseCase(groupName)
        _taskCompletedEvent.value = Event(Unit)
    }
}

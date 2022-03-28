package com.automotivecodelab.wbgoodstracker.ui.newgroupdialogfrag

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.automotivecodelab.wbgoodstracker.domain.AddItemsToGroupUseCase
import com.automotivecodelab.wbgoodstracker.ui.Event
import kotlinx.coroutines.launch

class NewGroupDialogViewModel(
    private val addItemsToGroupUseCase: AddItemsToGroupUseCase
) : ViewModel() {

    private val _closeDialogEvent = MutableLiveData<Event<Unit>>()
    val closeDialogEvent: LiveData<Event<Unit>> = _closeDialogEvent

    fun addGroup(itemIds: List<String>, groupName: String) {
        viewModelScope.launch {
            addItemsToGroupUseCase(itemIds, groupName)
            _closeDialogEvent.value = Event(Unit)
        }
    }
}

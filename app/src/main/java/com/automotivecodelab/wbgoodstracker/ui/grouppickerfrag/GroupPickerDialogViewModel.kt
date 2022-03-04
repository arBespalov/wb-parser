package com.automotivecodelab.wbgoodstracker.ui.grouppickerfrag

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.automotivecodelab.wbgoodstracker.domain.AddItemsToGroupUseCase
import com.automotivecodelab.wbgoodstracker.domain.GetGroupsUseCase
import com.automotivecodelab.wbgoodstracker.ui.Event
import kotlinx.coroutines.launch

class GroupPickerDialogViewModel(
    getGroupsUseCase: GetGroupsUseCase,
    private val addItemsToGroupUseCase: AddItemsToGroupUseCase
) : ViewModel() {

    private val _taskCompletedEvent = MutableLiveData<Event<Unit>>()
    val taskCompletedEvent: LiveData<Event<Unit>> = _taskCompletedEvent

    private val _groups = MutableLiveData<Array<String>>()
    val groups: LiveData<Array<String>> = _groups

    init {
        viewModelScope.launch {
            _groups.value = getGroupsUseCase()
        }
    }

    fun setGroupToItems(itemsId: List<String>, groupName: String) {
        viewModelScope.launch {
            addItemsToGroupUseCase(itemsId, groupName)
            _taskCompletedEvent.value = Event(Unit)
        }
    }
}

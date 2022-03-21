package com.automotivecodelab.wbgoodstracker.ui.grouppickerfrag

import androidx.lifecycle.*
import com.automotivecodelab.wbgoodstracker.domain.AddItemsToGroupUseCase
import com.automotivecodelab.wbgoodstracker.domain.GetGroupsUseCase
import com.automotivecodelab.wbgoodstracker.ui.Event
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class GroupPickerDialogViewModel(
    getGroupsUseCase: GetGroupsUseCase,
    private val addItemsToGroupUseCase: AddItemsToGroupUseCase
) : ViewModel() {

    private val _taskCompletedEvent = MutableLiveData<Event<Unit>>()
    val taskCompletedEvent: LiveData<Event<Unit>> = _taskCompletedEvent

    private val _newGroupEvent = MutableLiveData<Event<Unit>>()
    val newGroupEvent: LiveData<Event<Unit>> = _newGroupEvent

    val groups: LiveData<List<String>> = getGroupsUseCase().asLiveData()

    fun setGroupToItems(itemsId: List<String>, group: String?) {
        viewModelScope.launch {
            addItemsToGroupUseCase(itemsId, group)
            _taskCompletedEvent.value = Event(Unit)
        }
    }

    fun createNewGroup() {
        _newGroupEvent.value = Event(Unit)
    }
}

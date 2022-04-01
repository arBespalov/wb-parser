package com.automotivecodelab.wbgoodstracker.ui.grouppickerfrag

import androidx.lifecycle.*
import com.automotivecodelab.wbgoodstracker.domain.AddItemsToGroupUseCase
import com.automotivecodelab.wbgoodstracker.domain.ObserveGroupsUseCase
import com.automotivecodelab.wbgoodstracker.ui.Event
import kotlinx.coroutines.launch

class GroupPickerDialogViewModel(
    observeGroupsUseCase: ObserveGroupsUseCase,
    private val addItemsToGroupUseCase: AddItemsToGroupUseCase
) : ViewModel() {

    private val _closeDialogEvent = MutableLiveData<Event<Unit>>()
    val closeDialogEvent: LiveData<Event<Unit>> = _closeDialogEvent

    private val _newGroupEvent = MutableLiveData<Event<Unit>>()
    val newGroupEvent: LiveData<Event<Unit>> = _newGroupEvent

    val groups: LiveData<List<String>> = observeGroupsUseCase().asLiveData()

    fun setGroupToItems(itemsId: List<String>, group: String?) {
        viewModelScope.launch {
            addItemsToGroupUseCase(itemsId, group)
            _closeDialogEvent.value = Event(Unit)
        }
    }

    fun createNewGroup() {
        _newGroupEvent.value = Event(Unit)
    }
}

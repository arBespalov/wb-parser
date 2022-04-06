package com.automotivecodelab.wbgoodstracker.ui.newgroupdialogfrag

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.automotivecodelab.wbgoodstracker.domain.AddItemsToGroupUseCase
import com.automotivecodelab.wbgoodstracker.domain.ObserveCurrentGroupUseCase
import com.automotivecodelab.wbgoodstracker.domain.RenameCurrentGroupUseCase
import com.automotivecodelab.wbgoodstracker.ui.Event
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

class NewGroupDialogViewModel @Inject constructor(
    private val addItemsToGroupUseCase: AddItemsToGroupUseCase,
    private val renameCurrentGroupUseCase: RenameCurrentGroupUseCase,
    private val observeCurrentGroupUseCase: ObserveCurrentGroupUseCase
) : ViewModel() {

    private val _closeDialogEvent = MutableLiveData<Event<Unit>>()
    val closeDialogEvent: LiveData<Event<Unit>> = _closeDialogEvent

    private val _currentGroup = MutableLiveData<String?>()
    val currentGroup: LiveData<String?> = _currentGroup

    fun addGroup(itemIds: List<String>, groupName: String) {
        viewModelScope.launch {
            addItemsToGroupUseCase(itemIds, groupName)
            _closeDialogEvent.value = Event(Unit)
        }
    }

    fun renameGroup(newGroupName: String) {
        viewModelScope.launch {
            renameCurrentGroupUseCase(newGroupName)
            _closeDialogEvent.value = Event(Unit)
        }
    }

    fun getCurrentGroup() {
        viewModelScope.launch {
            _currentGroup.value = observeCurrentGroupUseCase().first()
        }
    }
}

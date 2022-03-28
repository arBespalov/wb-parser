package com.automotivecodelab.wbgoodstracker.ui.edititemfrag

import androidx.lifecycle.*
import com.automotivecodelab.wbgoodstracker.domain.EditItemUseCase
import com.automotivecodelab.wbgoodstracker.domain.GetGroupsUseCase
import com.automotivecodelab.wbgoodstracker.domain.ObserveSingleItemUseCase
import com.automotivecodelab.wbgoodstracker.ui.Event
import kotlinx.coroutines.launch

class EditItemViewModel(
    observeSingleItemUseCase: ObserveSingleItemUseCase,
    getGroupsUseCase: GetGroupsUseCase,
    itemId: String,
    private val editItemUseCase: EditItemUseCase,
) : ViewModel() {
    val item = observeSingleItemUseCase(itemId).asLiveData()
    val groups = getGroupsUseCase().asLiveData()

    var newName: String? = null
    var newGroup: String? = null

    private val _closeScreenEvent = MutableLiveData<Event<Unit>>()
    val closeScreenEvent: LiveData<Event<Unit>> = _closeScreenEvent

    private val _createNewGroupEvent = MutableLiveData<Event<String>>()
    val createNewGroupEvent: LiveData<Event<String>> = _createNewGroupEvent

    fun saveItem() {
        viewModelScope.launch {
            val sName = if (newName.isNullOrEmpty()) {
                null
            } else {
                newName
            }
            val item = item.value
            if (item != null) {
                editItemUseCase(item.copy(
                    localName = sName,
                    groupName = newGroup
                ))
                _closeScreenEvent.value = Event(Unit)
            }
        }
    }

    fun createNewGroup() {
        item.value?.id?.let {
            _createNewGroupEvent.value = Event(it)
        }
    }
}

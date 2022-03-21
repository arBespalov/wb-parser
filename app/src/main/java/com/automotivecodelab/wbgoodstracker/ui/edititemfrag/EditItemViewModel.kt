package com.automotivecodelab.wbgoodstracker.ui.edititemfrag

import androidx.lifecycle.*
import com.automotivecodelab.wbgoodstracker.domain.EditItemUseCase
import com.automotivecodelab.wbgoodstracker.domain.GetGroupsUseCase
import com.automotivecodelab.wbgoodstracker.domain.ObserveSingleItemUseCase
import com.automotivecodelab.wbgoodstracker.ui.Event
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class EditItemViewModel(
    observeSingleItemUseCase: ObserveSingleItemUseCase,
    getGroupsUseCase: GetGroupsUseCase,
    itemId: String,
    private val editItemUseCase: EditItemUseCase,
) : ViewModel() {

    private val _viewState = MutableLiveData(
        EditItemViewState(
            item = null,
            groups = listOf()
        )
    )
    val viewState: LiveData<EditItemViewState> = _viewState

    var newName: String? = null
    var newGroup: String? = null

    private val _saveItemEvent = MutableLiveData<Event<Unit>>()
    val saveItemEvent: LiveData<Event<Unit>> = _saveItemEvent

    init {
        viewModelScope.launch {
            getGroupsUseCase().collect {
                _viewState.value = _viewState.value?.copy(
                    groups = it
                )
            }
            observeSingleItemUseCase(itemId).collect {
                _viewState.value = _viewState.value?.copy(item = it)
            }
        }
    }

    fun saveItem() {
        viewModelScope.launch {
            val sName = if (newName.isNullOrEmpty()) {
                null
            } else {
                newName
            }
            val item = viewState.value?.item
            if (item != null) {
                editItemUseCase(item.copy(
                    localName = sName,
                    groupName = newGroup
                ))
                _saveItemEvent.value = Event(Unit)
            }
        }
    }
}

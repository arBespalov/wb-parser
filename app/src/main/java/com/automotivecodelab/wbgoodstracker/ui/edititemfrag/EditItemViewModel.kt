package com.automotivecodelab.wbgoodstracker.ui.edititemfrag

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.automotivecodelab.wbgoodstracker.domain.EditItemUseCase
import com.automotivecodelab.wbgoodstracker.domain.GetGroupsUseCase
import com.automotivecodelab.wbgoodstracker.domain.ObserveSingleItemUseCase
import com.automotivecodelab.wbgoodstracker.ui.Event
import kotlinx.coroutines.launch

class EditItemViewModel(
    observeSingleItemUseCase: ObserveSingleItemUseCase,
    itemId: String,
    private val editItemUseCase: EditItemUseCase,
    private val getGroupsUseCase: GetGroupsUseCase
) : ViewModel() {

    val item = observeSingleItemUseCase(itemId)

    private val _saveItemEvent = MutableLiveData<Event<Unit>>()
    val saveItemEvent: LiveData<Event<Unit>> = _saveItemEvent

    var cachedName: String? = null
    var cachedGroupName: String? = null

    fun saveItem() {
        viewModelScope.launch {
            val sName = if (cachedName.isNullOrEmpty()) {
                null
            } else {
                cachedName
            }

            item.value!!.copy(
                localName = sName,
                groupName = cachedGroupName
            ).also {
                editItemUseCase(it)
            }
            _saveItemEvent.value = Event(Unit)
        }
    }

    fun getSavedGroupNames(): Array<String> {
        return getGroupsUseCase()
    }
}

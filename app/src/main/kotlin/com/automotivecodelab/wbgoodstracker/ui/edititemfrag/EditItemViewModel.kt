package com.automotivecodelab.wbgoodstracker.ui.edititemfrag

import androidx.lifecycle.*
import com.automotivecodelab.wbgoodstracker.domain.AddItemsToGroupUseCase
import com.automotivecodelab.wbgoodstracker.domain.ObserveGroupsUseCase
import com.automotivecodelab.wbgoodstracker.domain.ObserveSingleItemUseCase
import com.automotivecodelab.wbgoodstracker.domain.SetLocalNameToItemUseCase
import com.automotivecodelab.wbgoodstracker.ui.Event
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.launch

class EditItemViewModel @AssistedInject constructor(
    observeSingleItemUseCase: ObserveSingleItemUseCase,
    observeGroupsUseCase: ObserveGroupsUseCase,
    @Assisted private val itemId: String,
    private val setLocalNameToItem: SetLocalNameToItemUseCase,
    private val addItemsToGroupUseCase: AddItemsToGroupUseCase
) : ViewModel() {
    val item = observeSingleItemUseCase(itemId).asLiveData()
    val groups = observeGroupsUseCase().asLiveData()

    var newName: String? = null
    var newGroup: String? = null

    private val _closeScreenEvent = MutableLiveData<Event<Unit>>()
    val closeScreenEvent: LiveData<Event<Unit>> = _closeScreenEvent

    private val _createNewGroupEvent = MutableLiveData<Event<String>>()
    val createNewGroupEvent: LiveData<Event<String>> = _createNewGroupEvent

    fun saveItem() {
        viewModelScope.launch {
            val sName = if (newName.isNullOrEmpty()) null else newName
            if (sName != item.value?.name)
                setLocalNameToItem(itemId, sName)
            if (newGroup != item.value?.groupName)
                addItemsToGroupUseCase(listOf(itemId), newGroup)
            _closeScreenEvent.value = Event(Unit)
        }
    }

    fun createNewGroup() {
        item.value?.id?.let {
            _createNewGroupEvent.value = Event(it)
        }
    }
}

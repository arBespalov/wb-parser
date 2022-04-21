package com.automotivecodelab.wbgoodstracker.ui.itemsfrag

import androidx.lifecycle.*
import com.automotivecodelab.wbgoodstracker.domain.*
import com.automotivecodelab.wbgoodstracker.domain.models.Item
import com.automotivecodelab.wbgoodstracker.domain.models.ItemGroups
import com.automotivecodelab.wbgoodstracker.domain.models.SortingMode
import com.automotivecodelab.wbgoodstracker.ui.Event
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import java.util.*
import kotlin.Comparator
import kotlinx.coroutines.launch
import javax.inject.Inject

class ItemsViewModel @Inject constructor(
    observeItemsByGroupUseCase: ObserveItemsWithGroupUseCase,
    observeSortingModeWithComparatorUseCase: ObserveSortingModeWithComparatorUseCase,
    private val setSortingModeUseCase: SetSortingModeUseCase,
    private val refreshAllItemsUseCase: RefreshAllItemsUseCase,
    observeGroupsUseCase: ObserveGroupsUseCase,
    private val setCurrentGroupUseCase: SetCurrentGroupUseCase,
    private val signOutUseCase: SignOutUseCase,
    private val deleteItemsUseCase: DeleteItemsUseCase
) : ViewModel() {

    val itemGroups: LiveData<ItemGroups> = observeGroupsUseCase().asLiveData()

    val currentSortingModeWithItemsComparator: LiveData<Pair<SortingMode, Comparator<Item>>> =
        observeSortingModeWithComparatorUseCase().asLiveData()

    private val searchQuery = MutableStateFlow("")

    @OptIn(ExperimentalCoroutinesApi::class)
    val itemsWithCurrentGroup: LiveData<Pair<List<Item>, String?>> = observeItemsByGroupUseCase()
        .flatMapLatest { (items, group) ->
            searchQuery.mapLatest { query ->
                items.filter { item ->
                    val byName = (item.localName ?: item.name)
                        .lowercase()
                        .contains(query.lowercase())
                    val byId = item.id.contains(query.lowercase())
                    byName || byId
                } to group
            }
        }
        .asLiveData()

    private val _selectedItemIds = MutableLiveData<Set<String>>(emptySet())
    val selectedItemIds: LiveData<Set<String>> = _selectedItemIds

    private val _openItemEvent = MutableLiveData<Event<Int>>()
    val openItemEvent: LiveData<Event<Int>> = _openItemEvent

    private val _addItemEvent = MutableLiveData<Event<Unit>>()
    val addItemEvent: LiveData<Event<Unit>> = _addItemEvent

    private val _confirmDeleteEvent = MutableLiveData<Event<List<String>>>()
    val confirmDeleteEvent: LiveData<Event<List<String>>> = _confirmDeleteEvent

    private val _editItemEvent = MutableLiveData<Event<String>>()
    val editItemEvent: LiveData<Event<String>> = _editItemEvent

    private val _updateErrorEvent = MutableLiveData<Event<Throwable>>()
    val updateErrorEvent: LiveData<Event<Throwable>> = _updateErrorEvent

    private val _deleteGroupEvent = MutableLiveData<Event<String>>()
    val deleteGroupEvent: LiveData<Event<String>> = _deleteGroupEvent

    private val _dataLoading = MutableLiveData<Boolean>()
    val dataLoading: LiveData<Boolean> = _dataLoading

    private val _addToGroupEvent = MutableLiveData<Event<List<String>>>()
    val addToGroupEvent: LiveData<Event<List<String>>> = _addToGroupEvent

    private val _signInEvent = MutableLiveData<Event<Unit>>()
    val signInEvent: LiveData<Event<Unit>> = _signInEvent

    private val _changeThemeEvent = MutableLiveData<Event<Unit>>()
    val changeThemeEvent: LiveData<Event<Unit>> = _changeThemeEvent

    private val _authorizationErrorEvent = MutableLiveData<Event<Unit>>()
    val authorizationErrorEvent: LiveData<Event<Unit>> = _authorizationErrorEvent

    private val _renameCurrentGroupEvent = MutableLiveData<Event<Unit>>()
    val renameCurrentGroupEvent: LiveData<Event<Unit>> = _renameCurrentGroupEvent

    private val _askUserForReviewEvent = MutableLiveData<Event<Unit>>()
    val askUserForReviewEvent: LiveData<Event<Unit>> = _askUserForReviewEvent

    private val _showContactsEvent = MutableLiveData<Event<Unit>>()
    val showContactsEvent: LiveData<Event<Unit>> = _showContactsEvent

    fun openItem(recyclerItemPosition: Int) {
        _openItemEvent.value = Event(recyclerItemPosition)
    }

    fun addItem() {
        _addItemEvent.value = Event(Unit)
    }

    fun confirmDelete() {
        selectedItemIds.value?.let {
            _confirmDeleteEvent.value = Event(it.toList())
        }
    }

    fun deleteSingleItem(itemId: String) {
        viewModelScope.launch {
            deleteItemsUseCase(
                itemsIdToDelete = listOf(itemId),
                onAuthenticationFailureCallback = { _authorizationErrorEvent.value = Event(Unit) }
            )
        }
    }

    fun editItem() {
        if (selectedItemIds.value?.size == 1) {
            val itemId = selectedItemIds.value?.elementAt(0)
            if (itemId != null) _editItemEvent.value = Event(itemId)
        }

    }

    fun deleteGroup() {
        val currentGroup = itemsWithCurrentGroup.value?.second
        if (currentGroup != null) {
            _deleteGroupEvent.value = Event(currentGroup)
        }
    }

    fun updateItems() {
        viewModelScope.launch {
            _dataLoading.value = true
            refreshAllItemsUseCase(
                onAuthenticationFailureCallback = { _authorizationErrorEvent.value = Event(Unit) },
                askUserForReviewCallback = { _askUserForReviewEvent.value = Event(Unit) }
            )
                .onFailure {
                    _updateErrorEvent.value = Event(it)
                }
            _dataLoading.value = false
        }
    }

    fun setCurrentGroup(group: String?) {
        viewModelScope.launch {
            setCurrentGroupUseCase(group)
        }
    }

    fun setSortingMode(sortingMode: SortingMode) {
        viewModelScope.launch {
            setSortingModeUseCase(sortingMode)
        }
    }

    fun addToGroup() {
        selectedItemIds.value?.let {
            _addToGroupEvent.value = Event(it.toList())
        }
    }

    fun filterItems(query: String) {
        searchQuery.value = query
    }

    fun signIn() {
        _signInEvent.value = Event(Unit)
    }

    fun changeTheme() {
        _changeThemeEvent.value = Event(Unit)
    }

    fun selectItem(id: String) {
        _selectedItemIds.value = _selectedItemIds.value?.plus(id)
    }

    fun unselectItem(id: String) {
        _selectedItemIds.value = _selectedItemIds.value?.minus(id)
    }

    fun clearSelection() {
        _selectedItemIds.value = emptySet()
    }

    fun renameGroup() {
        _renameCurrentGroupEvent.value = Event(Unit)
    }

    fun showContacts() {
        _showContactsEvent.value = Event(Unit)
    }
}

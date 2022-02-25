package com.automotivecodelab.wbgoodstracker.ui.itemsfrag

import androidx.lifecycle.*
import com.automotivecodelab.wbgoodstracker.data.util.Wrapper
import com.automotivecodelab.wbgoodstracker.domain.*
import com.automotivecodelab.wbgoodstracker.domain.models.Item
import com.automotivecodelab.wbgoodstracker.domain.models.SortingMode
import com.automotivecodelab.wbgoodstracker.domain.util.Result
import com.automotivecodelab.wbgoodstracker.ui.Event
import java.util.*
import kotlin.Comparator
import kotlinx.coroutines.launch

class ItemsViewModel(
    observeItemsByGroupUseCase: ObserveItemsByGroupUseCase,
    private val getUserSortingModeComparatorUseCase: GetUserSortingModeComparatorUseCase,
    private val setSortingModeUseCase: SetSortingModeUseCase,
    private val refreshAllItemsUseCase: RefreshAllItemsUseCase,
    private val getGroupsUseCase: GetGroupsUseCase,
    getCurrentGroupUseCase: GetCurrentGroupUseCase,
    private val setCurrentGroupUseCase: SetCurrentGroupUseCase,
    private val signOutUseCase: SignOutUseCase
) : ViewModel() {

    private val _currentGroup = MutableLiveData(getCurrentGroupUseCase())
    val currentGroup: LiveData<String> = _currentGroup

    val items: LiveData<List<Item>> = Transformations.switchMap(_currentGroup) { groupName ->
        observeItemsByGroupUseCase(groupName)
    }

    private val _openItemEvent = MutableLiveData<Event<Int>>()
    val openItemEvent: LiveData<Event<Int>> = _openItemEvent

    private val _addItemEvent = MutableLiveData<Event<Wrapper<String?>>>()
    val addItemEvent: LiveData<Event<Wrapper<String?>>> = _addItemEvent

    private val _confirmDeleteEvent = MutableLiveData<Event<List<String>>>()
    val confirmDeleteEvent: LiveData<Event<List<String>>> = _confirmDeleteEvent

    private val _editItemEvent = MutableLiveData<Event<String>>()
    val editItemEvent: LiveData<Event<String>> = _editItemEvent

    private val _updateErrorEvent = MutableLiveData<Event<String>>()
    val updateErrorEvent: LiveData<Event<String>> = _updateErrorEvent

    private val _newGroupEvent = MutableLiveData<Event<Unit>>()
    val newGroupEvent: LiveData<Event<Unit>> = _newGroupEvent

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

    var cachedSearchQuery: String? = null
        private set

    fun openItem(position: Int) {
        _openItemEvent.value = Event(position)
    }

    fun addItem(url: String?) {
        _addItemEvent.value = Event(Wrapper(url))
    }

    fun confirmDelete(itemsIdToDelete: List<String>) {
        _confirmDeleteEvent.value = Event(itemsIdToDelete)
    }

    fun editItem(itemId: String) {
        _editItemEvent.value = Event(itemId)
    }

    fun newGroup() {
        _newGroupEvent.value = Event(Unit)
    }

    fun deleteGroup() {
        if (_currentGroup.value != null) {
            _deleteGroupEvent.value = Event(_currentGroup.value!!)
        }
    }

    fun updateItems() {
        viewModelScope.launch {
            _dataLoading.value = true
            val result = refreshAllItemsUseCase {
                _authorizationErrorEvent.value = Event(Unit)
            }
            if (result is Result.Error) {
                _updateErrorEvent.value = Event(result.exception.message.toString())
            }
            _dataLoading.value = false
        }
    }

    fun changeCurrentGroup(groupName: String) {
        _currentGroup.value = groupName
        setCurrentGroupUseCase(groupName)
    }

    fun saveSortingMode(sortingMode: SortingMode) {
        setSortingModeUseCase(sortingMode)
    }

    fun getSavedGroupNames(): Array<String> {
        return getGroupsUseCase()
    }

    fun addToGroup(itemsId: List<String>) {
        _addToGroupEvent.value = Event(itemsId)
    }

    fun filterItems(query: String?): List<Item> {
        cachedSearchQuery = query
        val lowerCaseQuery = query?.lowercase(Locale.ROOT) ?: ""
        val filteredList = mutableListOf<Item>()
        items.value?.forEach { item ->
            val text = (item.local_name ?: item.name).lowercase(Locale.ROOT)
            if (text.contains(lowerCaseQuery)) {
                filteredList.add(item)
            }
        }
        return filteredList
    }

    fun signIn() {
        _signInEvent.value = Event(Unit)
    }

    fun signOut() {
        signOutUseCase()
    }

    fun getItemsComparator(): Comparator<Item> {
        return getUserSortingModeComparatorUseCase()
    }

    fun changeTheme() {
        _changeThemeEvent.value = Event(Unit)
    }
}

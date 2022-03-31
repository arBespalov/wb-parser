package com.automotivecodelab.wbgoodstracker.ui.itemsfrag

import androidx.lifecycle.*
import com.automotivecodelab.wbgoodstracker.domain.*
import com.automotivecodelab.wbgoodstracker.domain.models.Item
import com.automotivecodelab.wbgoodstracker.domain.models.SortingMode
import com.automotivecodelab.wbgoodstracker.log
import com.automotivecodelab.wbgoodstracker.ui.Event
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import java.util.*
import kotlin.Comparator
import kotlinx.coroutines.launch

class ItemsViewModel(
    observeItemsByGroupUseCase: ObserveItemsByGroupUseCase,
    getUserSortingModeComparatorUseCase: GetUserSortingModeComparatorUseCase,
    private val setSortingModeUseCase: SetSortingModeUseCase,
    private val refreshAllItemsUseCase: RefreshAllItemsUseCase,
    getGroupsUseCase: GetGroupsUseCase,
    private val setCurrentGroupUseCase: SetCurrentGroupUseCase,
    private val signOutUseCase: SignOutUseCase,
    private val deleteItemsUseCase: DeleteItemsUseCase
) : ViewModel() {

    val groups: LiveData<List<String>> = getGroupsUseCase().asLiveData()

    val itemsComparator: LiveData<Comparator<Item>> = getUserSortingModeComparatorUseCase()
        .asLiveData()

    private val searchQuery = MutableStateFlow("")

    // second in pair - current group
    @OptIn(ExperimentalCoroutinesApi::class)
    val itemsWithCurrentGroup: LiveData<Pair<List<Item>, String?>> = searchQuery
        .flatMapLatest { query ->
            observeItemsByGroupUseCase().mapLatest { (items, group) ->
                items.filter { item ->
                    (item.localName ?: item.name)
                        .lowercase(Locale.ROOT)
                        .contains(query.lowercase(Locale.ROOT))
                } to group
            }
        }
        .asLiveData()

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

//    var cachedSearchQuery: String? = null
//        private set

    fun openItem(recyclerItemPosition: Int) {
        _openItemEvent.value = Event(recyclerItemPosition)
    }

    fun addItem() {
        _addItemEvent.value = Event(Unit)
    }

    fun confirmDelete(itemsIdToDelete: List<String>) {
        _confirmDeleteEvent.value = Event(itemsIdToDelete)
    }

    fun deleteSingleItem(itemId: String) {
        viewModelScope.launch {
            deleteItemsUseCase(arrayOf(itemId))
        }
    }

    fun editItem(itemId: String) {
        _editItemEvent.value = Event(itemId)
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
            refreshAllItemsUseCase(onAuthenticationFailureCallback = {
                _authorizationErrorEvent.value = Event(Unit)
            })
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

    fun addToGroup(itemsId: List<String>) {
        _addToGroupEvent.value = Event(itemsId)
    }

    fun filterItems(query: String) {
        searchQuery.value = query
    }

    fun signIn() {
        _signInEvent.value = Event(Unit)
    }

    fun signOut() {
        viewModelScope.launch {
            signOutUseCase()
        }
    }

    fun changeTheme() {
        _changeThemeEvent.value = Event(Unit)
    }
}

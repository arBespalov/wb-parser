package com.automotivecodelab.wbgoodstracker.ui.additemfrag

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.automotivecodelab.wbgoodstracker.domain.*
import com.automotivecodelab.wbgoodstracker.ui.Event
import kotlinx.coroutines.launch

class AddItemViewModel(
    private val addItemUseCase: AddItemUseCase,
    private val signOutUseCase: SignOutUseCase
) : ViewModel() {

    private val _saveSuccessfulEvent = MutableLiveData<Event<Unit>>()
    val saveSuccessfulEvent: LiveData<Event<Unit>> = _saveSuccessfulEvent

    private val _invalidUrl = MutableLiveData<Boolean>()
    val invalidUrl: LiveData<Boolean> = _invalidUrl

    private val _networkErrorEvent = MutableLiveData<Event<String>>()
    val networkErrorEvent: LiveData<Event<String>> = _networkErrorEvent

    private val _dataLoading = MutableLiveData<Boolean>()
    val dataLoading: LiveData<Boolean> = _dataLoading

    private val _authorizationErrorEvent = MutableLiveData<Event<Unit>>()
    val authorizationErrorEvent: LiveData<Event<Unit>> = _authorizationErrorEvent

    private var url: String = ""

    fun saveItem() {
        viewModelScope.launch {
            _dataLoading.value = true
            addItemUseCase(
                url = url,
                onAuthenticationFailureCallback =  {
                    _authorizationErrorEvent.value = Event(Unit)
                }
            )
                .onFailure {
                when (it) {
                    is InvalidUrlException -> _invalidUrl.value = true
                    else -> _networkErrorEvent.value = Event(it.message.toString())
                }
            }
                .onSuccess {
                    _saveSuccessfulEvent.value = Event(Unit)
                }
            _dataLoading.value = false
        }
    }

    fun handleTextInput(text: String) {
        if (text != url) {
            _invalidUrl.value = false
            url = text
        }
    }

    fun signOut() {
        viewModelScope.launch {
            signOutUseCase()
        }
    }
}

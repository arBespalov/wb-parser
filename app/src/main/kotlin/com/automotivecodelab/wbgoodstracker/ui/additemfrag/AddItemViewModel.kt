package com.automotivecodelab.wbgoodstracker.ui.additemfrag

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.automotivecodelab.wbgoodstracker.domain.*
import com.automotivecodelab.wbgoodstracker.ui.Event
import javax.inject.Inject
import kotlinx.coroutines.launch

class AddItemViewModel @Inject constructor(
    private val addItemUseCase: AddItemUseCase
) : ViewModel() {

    private val _saveSuccessfulEvent = MutableLiveData<Event<Unit>>()
    val saveSuccessfulEvent: LiveData<Event<Unit>> = _saveSuccessfulEvent

    private val _inputState = MutableLiveData<UserInputState>()
    val inputState: LiveData<UserInputState> = _inputState

    private val _errorEvent = MutableLiveData<Event<Throwable>>()
    val errorEvent: LiveData<Event<Throwable>> = _errorEvent

    private val _dataLoading = MutableLiveData<Boolean>()
    val dataLoading: LiveData<Boolean> = _dataLoading

    private val _authorizationErrorEvent = MutableLiveData<Event<Unit>>()
    val authorizationErrorEvent: LiveData<Event<Unit>> = _authorizationErrorEvent

    private var input: String = ""

    fun saveItem() {
        viewModelScope.launch {
            _dataLoading.value = true
            addItemUseCase(
                input = input,
                onAuthenticationFailureCallback = {
                    _authorizationErrorEvent.value = Event(Unit)
                }
            )
                .onFailure {
                    when (it) {
                        is InvalidUrlException ->
                            _inputState.value =
                                UserInputState.INVALID_URL
                        is InvalidVendorCodeException ->
                            _inputState.value =
                                UserInputState.INVALID_VENDOR_CODE
                        is ItemsQuotaExceededException ->
                            _errorEvent.value = Event(it)
                        else ->
                            _errorEvent.value = Event(it)
                    }
                }
                .onSuccess {
                    _saveSuccessfulEvent.value = Event(Unit)
                }
            _dataLoading.value = false
        }
    }

    fun handleTextInput(text: String) {
        if (text != input) {
            _inputState.value = UserInputState.OK
            input = text
        }
    }
}

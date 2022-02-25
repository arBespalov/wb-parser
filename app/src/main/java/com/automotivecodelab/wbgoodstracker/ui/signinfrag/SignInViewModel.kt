package com.automotivecodelab.wbgoodstracker.ui.signinfrag

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.automotivecodelab.wbgoodstracker.domain.GetUserUseCase
import com.automotivecodelab.wbgoodstracker.domain.SignInUseCase
import com.automotivecodelab.wbgoodstracker.domain.SignOutUseCase
import com.automotivecodelab.wbgoodstracker.domain.models.User
import com.automotivecodelab.wbgoodstracker.domain.util.Result
import com.automotivecodelab.wbgoodstracker.ui.Event
import kotlinx.coroutines.launch

class SignInViewModel(
    private val signInUseCase: SignInUseCase,
    private val getUserUseCase: GetUserUseCase,
    private val signOutUseCase: SignOutUseCase
) : ViewModel() {

    private val _viewState = MutableLiveData<SignInViewState>()
    val viewState: LiveData<SignInViewState> = _viewState

    private val _networkErrorEvent = MutableLiveData<Event<String>>()
    val networkErrorEvent: LiveData<Event<String>> = _networkErrorEvent

    fun start() {
        viewModelScope.launch {
            when (val userResult = getUserUseCase()) {
                is Result.Success -> {
                    if (userResult.data != null) {
                        _viewState.value = SignInViewState.SignedInState(userResult.data.email)
                    } else {
                        _viewState.value = SignInViewState.SignedOutState
                    }
                }
                else -> _viewState.value = SignInViewState.SignedOutState
            }
        }
    }

    fun signOut() {
        signOutUseCase()
        _viewState.value = SignInViewState.SignedOutState
    }

    fun handleSignInResult(user: User) {
        viewModelScope.launch {
            _viewState.value = SignInViewState.LoadingState
            val result = signInUseCase(user)
            if (result is Result.Error) {
                _viewState.value = SignInViewState.SignedOutState
                _networkErrorEvent.value = Event(result.exception.message.toString())
            } else {
                _viewState.value = SignInViewState.SignedInState(user.email)
            }
        }
    }
}

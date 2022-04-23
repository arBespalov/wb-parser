package com.automotivecodelab.wbgoodstracker.ui.signinfrag

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.automotivecodelab.wbgoodstracker.domain.GetUserUseCase
import com.automotivecodelab.wbgoodstracker.domain.SignInUseCase
import com.automotivecodelab.wbgoodstracker.domain.SignOutUseCase
import com.automotivecodelab.wbgoodstracker.ui.Event
import javax.inject.Inject
import kotlinx.coroutines.launch

class SignInViewModel @Inject constructor(
    private val signInUseCase: SignInUseCase,
    getUserUseCase: GetUserUseCase,
    private val signOutUseCase: SignOutUseCase
) : ViewModel() {

    private val _viewState = MutableLiveData<SignInViewState>()
    val viewState: LiveData<SignInViewState> = _viewState

    private val _networkErrorEvent = MutableLiveData<Event<Throwable>>()
    val networkErrorEvent: LiveData<Event<Throwable>> = _networkErrorEvent

    init {
        viewModelScope.launch {
            getUserUseCase()
                .onFailure {
                    _viewState.value = SignInViewState.SignedOutState
                }
                .onSuccess { user ->
                    _viewState.value = SignInViewState.SignedInState(user.email)
                }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            signOutUseCase()
            _viewState.value = SignInViewState.SignedOutState
        }
    }

    fun signIn(idToken: String) {
        viewModelScope.launch {
            _viewState.value = SignInViewState.LoadingState
            signInUseCase(idToken)
                .onFailure {
                    _viewState.value = SignInViewState.SignedOutState
                    _networkErrorEvent.value = Event(it)
                }
                .onSuccess {
                    _viewState.value = SignInViewState.SignedInState(null)
                }
        }
    }

    fun setError(t: Throwable) {
        _networkErrorEvent.value = Event(t)
    }
}

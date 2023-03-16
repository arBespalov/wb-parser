package com.automotivecodelab.wbgoodstracker.ui.signinfrag

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.automotivecodelab.wbgoodstracker.domain.*
import com.automotivecodelab.wbgoodstracker.ui.Event
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import kotlinx.coroutines.launch

class SignInViewModel @Inject constructor(
    private val signInUseCase: SignInUseCase,
    private val signInDebugUseCase: SignInDebugUseCase,
    getUserUseCase: GetUserUseCase,
    private val signOutUseCase: SignOutUseCase,
    observeMergeLoadingState: ObserveMergeLoadingState
) : ViewModel() {

    private val _viewState = MutableStateFlow<SignInViewState>(SignInViewState.SignedOutState)
    val viewState = _viewState.asStateFlow()

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
        observeMergeLoadingState()
            .onEach { isLoading ->
                if (isLoading) _viewState.value = SignInViewState.LoadingState
            }
            .launchIn(viewModelScope)
    }

    fun signOut() {
        viewModelScope.launch {
            signOutUseCase()
            _viewState.value = SignInViewState.SignedOutState
        }
    }

    fun signIn(idToken: String) {
        viewModelScope.launch {
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

    fun signInDebug(userId: String) {
        viewModelScope.launch {
            signInDebugUseCase(userId)
                .onFailure {
                    _networkErrorEvent.value = Event(it)
                }
            _viewState.value = SignInViewState.SignedOutState
        }
    }

    fun setError(t: Throwable) {
        _networkErrorEvent.value = Event(t)
    }
}

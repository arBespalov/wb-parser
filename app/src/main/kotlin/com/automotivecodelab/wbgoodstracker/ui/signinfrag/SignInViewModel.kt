package com.automotivecodelab.wbgoodstracker.ui.signinfrag

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.automotivecodelab.wbgoodstracker.domain.*
import com.automotivecodelab.wbgoodstracker.domain.models.MergeStatus
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
        observeMergeLoadingState()
            .onEach { status ->
                when (status) {
                    MergeStatus.Idle, MergeStatus.Success -> {
                        getUserUseCase()
                            .onFailure {
                                _viewState.value = SignInViewState.SignedOutState
                            }
                            .onSuccess { user ->
                                _viewState.value = SignInViewState.SignedInState(user.email)
                            }
                    }
                    MergeStatus.InProgress -> {
                        _viewState.value = SignInViewState.LoadingState
                    }
                    is MergeStatus.Error -> {
                        _viewState.value = SignInViewState.SignedOutState
                        _networkErrorEvent.value = Event(status.t)
                    }
                }
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
        signInUseCase(idToken)
    }

    fun signInDebug(userId: String) {
        signInDebugUseCase(userId)
    }

    fun setError(t: Throwable) {
        _networkErrorEvent.value = Event(t)
    }
}

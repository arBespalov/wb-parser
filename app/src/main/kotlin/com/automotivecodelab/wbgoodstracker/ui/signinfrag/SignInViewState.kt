package com.automotivecodelab.wbgoodstracker.ui.signinfrag

sealed interface SignInViewState {
    class SignedInState(val email: String?) : SignInViewState
    object SignedOutState : SignInViewState
    object LoadingState : SignInViewState
}

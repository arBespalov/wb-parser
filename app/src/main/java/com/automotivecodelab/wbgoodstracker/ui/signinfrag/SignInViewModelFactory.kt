package com.automotivecodelab.wbgoodstracker.ui.signinfrag

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.automotivecodelab.wbgoodstracker.domain.GetUserUseCase
import com.automotivecodelab.wbgoodstracker.domain.SignInUseCase
import com.automotivecodelab.wbgoodstracker.domain.SignOutUseCase
import com.automotivecodelab.wbgoodstracker.domain.repositories.ItemsRepository
import com.automotivecodelab.wbgoodstracker.domain.repositories.UserRepository

class SignInViewModelFactory(
    private val itemsRepository: ItemsRepository,
    private val userRepository: UserRepository
    ): ViewModelProvider.NewInstanceFactory(){
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return SignInViewModel(
            SignInUseCase(userRepository, itemsRepository),
            GetUserUseCase(userRepository),
            SignOutUseCase(userRepository)
        ) as T
    }
}

package com.automotivecodelab.wbgoodstracker.ui.additemfrag

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.automotivecodelab.wbgoodstracker.domain.AddItemUseCase
import com.automotivecodelab.wbgoodstracker.domain.SignOutUseCase
import com.automotivecodelab.wbgoodstracker.domain.repositories.ItemsRepository
import com.automotivecodelab.wbgoodstracker.domain.repositories.UserRepository

class AddItemViewModelFactory (
    private val itemsRepository: ItemsRepository,
    private val userRepository: UserRepository
): ViewModelProvider.NewInstanceFactory(){

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return AddItemViewModel(
            AddItemUseCase(itemsRepository, userRepository),
            SignOutUseCase(userRepository)
            ) as T
    }
}
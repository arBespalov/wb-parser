package com.automotivecodelab.wbgoodstracker.ui.confirmremovedialogfrag

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.automotivecodelab.wbgoodstracker.domain.DeleteItemsUseCase
import com.automotivecodelab.wbgoodstracker.domain.SignOutUseCase
import com.automotivecodelab.wbgoodstracker.domain.repositories.ItemsRepository
import com.automotivecodelab.wbgoodstracker.domain.repositories.UserRepository

class ConfirmRemoveDialogViewModelFactory (
        private val itemsRepository: ItemsRepository,
        private val userRepository: UserRepository
): ViewModelProvider.NewInstanceFactory(){

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ConfirmRemoveDialogViewModel(
            DeleteItemsUseCase(itemsRepository, userRepository)
        ) as T
    }
}
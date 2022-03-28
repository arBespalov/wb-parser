package com.automotivecodelab.wbgoodstracker.ui.confirmdeletegroupdialogfrag

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.automotivecodelab.wbgoodstracker.domain.DeleteGroupUseCase
import com.automotivecodelab.wbgoodstracker.domain.repositories.ItemsRepository

class ConfirmDeleteGroupDialogViewModelFactory(
    private val itemsRepository: ItemsRepository
) : ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ConfirmDeleteGroupDialogViewModel(
            DeleteGroupUseCase(itemsRepository)
        ) as T
    }
}

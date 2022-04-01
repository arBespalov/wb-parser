package com.automotivecodelab.wbgoodstracker.ui.grouppickerfrag

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.automotivecodelab.wbgoodstracker.domain.AddItemsToGroupUseCase
import com.automotivecodelab.wbgoodstracker.domain.ObserveGroupsUseCase
import com.automotivecodelab.wbgoodstracker.domain.repositories.ItemsRepository

class GroupPickerDialogViewModelFactory(
    private val itemsRepository: ItemsRepository
) : ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return GroupPickerDialogViewModel(
            ObserveGroupsUseCase(itemsRepository),
            AddItemsToGroupUseCase(itemsRepository)
        ) as T
    }
}

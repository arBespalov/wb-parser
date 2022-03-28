package com.automotivecodelab.wbgoodstracker.ui.grouppickerfrag

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.automotivecodelab.wbgoodstracker.domain.AddItemsToGroupUseCase
import com.automotivecodelab.wbgoodstracker.domain.GetGroupsUseCase
import com.automotivecodelab.wbgoodstracker.domain.repositories.ItemsRepository

class GroupPickerDialogViewModelFactory(
    private val itemsRepository: ItemsRepository
) : ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return GroupPickerDialogViewModel(
            GetGroupsUseCase(itemsRepository),
            AddItemsToGroupUseCase(itemsRepository)
        ) as T
    }
}

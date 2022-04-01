package com.automotivecodelab.wbgoodstracker.ui.newgroupdialogfrag

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.automotivecodelab.wbgoodstracker.domain.AddItemsToGroupUseCase
import com.automotivecodelab.wbgoodstracker.domain.ObserveCurrentGroupUseCase
import com.automotivecodelab.wbgoodstracker.domain.RenameCurrentGroupUseCase
import com.automotivecodelab.wbgoodstracker.domain.repositories.ItemsRepository

class NewGroupDialogViewModelFactory(
    private val itemsRepository: ItemsRepository
) : ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return NewGroupDialogViewModel(
            AddItemsToGroupUseCase(itemsRepository),
            RenameCurrentGroupUseCase(itemsRepository),
            ObserveCurrentGroupUseCase(itemsRepository)
        ) as T
    }
}

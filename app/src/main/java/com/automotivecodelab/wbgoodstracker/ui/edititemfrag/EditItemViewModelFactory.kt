package com.automotivecodelab.wbgoodstracker.ui.edititemfrag

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.automotivecodelab.wbgoodstracker.domain.EditItemUseCase
import com.automotivecodelab.wbgoodstracker.domain.GetGroupsUseCase
import com.automotivecodelab.wbgoodstracker.domain.ObserveSingleItemUseCase
import com.automotivecodelab.wbgoodstracker.domain.repositories.ItemsRepository

class EditItemViewModelFactory(
    private val itemId: String,
    private val itemsRepository: ItemsRepository,
) : ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return EditItemViewModel(
            ObserveSingleItemUseCase(itemsRepository),
            itemId,
            EditItemUseCase(itemsRepository),
            GetGroupsUseCase(itemsRepository)
        ) as T
    }
}

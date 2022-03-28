package com.automotivecodelab.wbgoodstracker.ui.detailfrag

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.automotivecodelab.wbgoodstracker.domain.ObserveSingleItemUseCase
import com.automotivecodelab.wbgoodstracker.domain.RefreshSingleItemUseCase
import com.automotivecodelab.wbgoodstracker.domain.repositories.ItemsRepository

class DetailViewModelFactory(
    private val itemsRepository: ItemsRepository,
    private val itemId: String
) : ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return DetailViewModel(
            RefreshSingleItemUseCase(itemsRepository),
            ObserveSingleItemUseCase(itemsRepository),
            itemId
        ) as T
    }
}

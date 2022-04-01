package com.automotivecodelab.wbgoodstracker.ui.itemsfrag

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.automotivecodelab.wbgoodstracker.domain.*
import com.automotivecodelab.wbgoodstracker.domain.repositories.ItemsRepository
import com.automotivecodelab.wbgoodstracker.domain.repositories.SortRepository
import com.automotivecodelab.wbgoodstracker.domain.repositories.UserRepository

class ItemsViewModelFactory(
    private val itemsRepository: ItemsRepository,
    private val userRepository: UserRepository,
    private val sortRepository: SortRepository
) : ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ItemsViewModel(
            ObserveItemsWithGroupUseCase(itemsRepository),
            ObserveUserSortingModeComparatorUseCase(sortRepository),
            SetSortingModeUseCase(sortRepository),
            RefreshAllItemsUseCase(userRepository, itemsRepository),
            ObserveGroupsUseCase(itemsRepository),
            SetCurrentGroupUseCase(itemsRepository),
            SignOutUseCase(userRepository),
            DeleteItemsUseCase(itemsRepository, userRepository)
        ) as T
    }
}

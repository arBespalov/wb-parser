package com.automotivecodelab.wbgoodstracker.ui.itemsfrag

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.automotivecodelab.wbgoodstracker.domain.*
import com.automotivecodelab.wbgoodstracker.domain.repositories.ItemsRepository
import com.automotivecodelab.wbgoodstracker.domain.repositories.UserRepository

class ItemsViewModelFactory(
    private val itemsRepository: ItemsRepository,
    private val userRepository: UserRepository
) : ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ItemsViewModel(
            ObserveItemsByGroupUseCase(itemsRepository),
            GetUserSortingModeComparatorUseCase(itemsRepository),
            SetSortingModeUseCase(itemsRepository),
            RefreshAllItemsUseCase(userRepository, itemsRepository),
            GetGroupsUseCase(itemsRepository),
            GetCurrentGroupUseCase(itemsRepository),
            SetCurrentGroupUseCase(itemsRepository),
            SignOutUseCase(userRepository)
        ) as T
    }
}

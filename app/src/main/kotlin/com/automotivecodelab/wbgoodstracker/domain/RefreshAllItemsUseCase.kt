package com.automotivecodelab.wbgoodstracker.domain

import com.automotivecodelab.wbgoodstracker.domain.repositories.ItemsRepository
import com.automotivecodelab.wbgoodstracker.domain.repositories.UsageStatisticsRepository
import com.automotivecodelab.wbgoodstracker.domain.repositories.UserRepository
import javax.inject.Inject

class RefreshAllItemsUseCase @Inject constructor(
    private val userRepository: UserRepository,
    private val itemsRepository: ItemsRepository,
    private val usageStatisticsRepository: UsageStatisticsRepository
) {
    suspend operator fun invoke(
        onAuthenticationFailureCallback: () -> Unit,
        askUserForReviewCallback: () -> Unit
    ): Result<Unit> {
        val result = if (userRepository.isUserAuthenticated()) {
            val user = userRepository.getUser()
            if (user.isFailure) {
                onAuthenticationFailureCallback()
                itemsRepository.refreshAllItems()
            } else {
                itemsRepository.syncItems(user.getOrThrow().idToken)
            }
        } else {
            itemsRepository.refreshAllItems()
        }
        if (result.isSuccess) usageStatisticsRepository.incrementUpdateAllItemsAction()
        if (usageStatisticsRepository.getCountOfUpdateAllItemsAction() == 20)
            askUserForReviewCallback()
        return result
    }
}

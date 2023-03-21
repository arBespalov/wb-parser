package com.automotivecodelab.wbgoodstracker.domain

import com.automotivecodelab.wbgoodstracker.domain.models.MergeStatus
import com.automotivecodelab.wbgoodstracker.domain.repositories.ItemsRepository
import com.automotivecodelab.wbgoodstracker.domain.repositories.UserRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class SignInUseCase @Inject constructor(
    private val userRepository: UserRepository,
    private val itemsRepository: ItemsRepository
) {
    suspend operator fun invoke(idToken: String) {
        itemsRepository.mergeItems(idToken)
        val result = itemsRepository.mergeStatus.first { mergeStatus ->
            mergeStatus is MergeStatus.Success || mergeStatus is MergeStatus.Error
        }
        if (result is MergeStatus.Success)
            userRepository.signIn()
    }
}

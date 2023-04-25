package com.automotivecodelab.wbgoodstracker.domain

import com.automotivecodelab.wbgoodstracker.domain.models.MergeStatus
import com.automotivecodelab.wbgoodstracker.domain.repositories.ItemsRepository
import com.automotivecodelab.wbgoodstracker.domain.repositories.UserRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

class SignInUseCase @Inject constructor(
    private val userRepository: UserRepository,
    private val itemsRepository: ItemsRepository,
    private val scope: CoroutineScope
) {
    operator fun invoke(idToken: String) {
        scope.launch {
            if (itemsRepository.mergeStatus.first() == MergeStatus.InProgress)
                error("trying to start merge while it is already started")
            itemsRepository.setMergeStatus(MergeStatus.InProgress)
            itemsRepository.mergeItems(idToken)
                .onSuccess {
                    userRepository.signIn()
                    itemsRepository.setMergeStatus(MergeStatus.Success)
                }
                .onFailure {
                    itemsRepository.setMergeStatus(MergeStatus.Error(it))
                }
        }
    }
}

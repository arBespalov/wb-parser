package com.automotivecodelab.wbgoodstracker.domain

import com.automotivecodelab.wbgoodstracker.domain.models.Ad
import com.automotivecodelab.wbgoodstracker.domain.repositories.ItemsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveAdUseCase @Inject constructor(
    private val itemsRepository: ItemsRepository
) {
    operator fun invoke(): Flow<Ad?> {
        return itemsRepository.observeAd()
    }
}
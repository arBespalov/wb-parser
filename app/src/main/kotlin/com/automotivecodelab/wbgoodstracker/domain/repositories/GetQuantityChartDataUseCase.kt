package com.automotivecodelab.wbgoodstracker.domain.repositories

import javax.inject.Inject

class GetQuantityChartDataUseCase @Inject constructor(
    private val itemsRepository: ItemsRepository
) {
    suspend operator fun invoke(itemId: String): Result<List<Pair<Long, Int>>> {
        return itemsRepository.getQuantityChartData(itemId)
    }
}
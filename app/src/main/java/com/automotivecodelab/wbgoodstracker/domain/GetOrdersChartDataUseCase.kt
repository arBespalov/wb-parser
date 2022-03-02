package com.automotivecodelab.wbgoodstracker.domain

import com.automotivecodelab.wbgoodstracker.domain.repositories.ItemsRepository

class GetOrdersChartDataUseCase(
    private val itemsRepository: ItemsRepository
) {
    suspend operator fun invoke(itemId: String): Result<List<Pair<Long, Int>>> {
        return itemsRepository.getOrdersChartData(itemId)
    }
}

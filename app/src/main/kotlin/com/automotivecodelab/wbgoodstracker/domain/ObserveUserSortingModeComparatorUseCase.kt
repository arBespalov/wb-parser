package com.automotivecodelab.wbgoodstracker.domain

import com.automotivecodelab.wbgoodstracker.domain.models.Item
import com.automotivecodelab.wbgoodstracker.domain.repositories.ItemsRepository
import com.automotivecodelab.wbgoodstracker.domain.repositories.SortRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveUserSortingModeComparatorUseCase @Inject constructor(
    private val sortRepository: SortRepository
) {
    operator fun invoke(): Flow<Comparator<Item>> {
        return sortRepository.getSortingModeComparator()
    }
}

package com.automotivecodelab.wbgoodstracker.domain

import com.automotivecodelab.wbgoodstracker.domain.models.Item
import com.automotivecodelab.wbgoodstracker.domain.models.SortingMode
import com.automotivecodelab.wbgoodstracker.domain.repositories.SortRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class ObserveSortingModeWithComparatorUseCase @Inject constructor(
    private val sortRepository: SortRepository
) {
    operator fun invoke(): Flow<Pair<SortingMode, Comparator<Item>>> {
        return sortRepository.observeSortingModeWithComparator()
    }
}

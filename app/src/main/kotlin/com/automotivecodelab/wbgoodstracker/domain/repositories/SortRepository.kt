package com.automotivecodelab.wbgoodstracker.domain.repositories

import com.automotivecodelab.wbgoodstracker.domain.models.Item
import com.automotivecodelab.wbgoodstracker.domain.models.SortingMode
import java.util.Comparator
import kotlinx.coroutines.flow.Flow

interface SortRepository {
    fun observeSortingModeWithComparator(): Flow<Pair<SortingMode, Comparator<Item>>>
    suspend fun setSortingMode(sortingMode: SortingMode)
}

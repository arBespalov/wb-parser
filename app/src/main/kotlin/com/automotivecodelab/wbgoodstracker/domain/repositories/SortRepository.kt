package com.automotivecodelab.wbgoodstracker.domain.repositories

import com.automotivecodelab.wbgoodstracker.domain.models.Item
import com.automotivecodelab.wbgoodstracker.domain.models.SortingMode
import kotlinx.coroutines.flow.Flow
import java.util.Comparator

interface SortRepository {
    fun getSortingModeComparator(): Flow<Comparator<Item>>
    suspend fun setSortingMode(sortingMode: SortingMode)
}
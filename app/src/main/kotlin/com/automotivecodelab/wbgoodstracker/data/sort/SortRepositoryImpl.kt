package com.automotivecodelab.wbgoodstracker.data.sort

import com.automotivecodelab.wbgoodstracker.domain.models.Item
import com.automotivecodelab.wbgoodstracker.domain.models.SortingMode
import com.automotivecodelab.wbgoodstracker.domain.repositories.SortRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class SortRepositoryImpl @Inject constructor(
    private val localDataSource: SortLocalDataSource
): SortRepository {
    override fun observeSortingModeWithComparator():
            Flow<Pair<SortingMode, Comparator<Item>>> {
        return localDataSource.getSortingMode()
            .map { sortingMode ->
                sortingMode to Comparator { o1, o2 ->
                    val comp = when (sortingMode) {
                        SortingMode.BY_NAME_ASC ->
                            o1.name.compareTo(o2.name)
                        SortingMode.BY_NAME_DESC ->
                            o2.name.compareTo(o1.name)
                        SortingMode.BY_DATE_ASC ->
                            o2.creationTimestamp.compareTo(o1.creationTimestamp)
                        SortingMode.BY_DATE_DESC ->
                            o1.creationTimestamp.compareTo(o2.creationTimestamp)
                        SortingMode.BY_ORDERS_COUNT ->
                            o2.ordersCount.compareTo(o1.ordersCount)
                        SortingMode.BY_ORDERS_COUNT_PER_DAY ->
                            o2.averageOrdersCountPerDay.compareTo(o1.averageOrdersCountPerDay)
                        SortingMode.BY_LAST_CHANGES -> o2.lastChangesTimestamp
                                .compareTo(o1.lastChangesTimestamp)
                    }
                    if (comp == 0)
                        o2.id.compareTo(o1.id)
                    else
                        comp
                }
            }
    }

    override suspend fun setSortingMode(sortingMode: SortingMode) {
        localDataSource.setSortingMode(sortingMode)
    }
}
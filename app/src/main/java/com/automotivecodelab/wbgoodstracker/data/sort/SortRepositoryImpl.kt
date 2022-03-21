package com.automotivecodelab.wbgoodstracker.data.sort

import android.os.Build
import androidx.annotation.RequiresApi
import com.automotivecodelab.wbgoodstracker.domain.models.Item
import com.automotivecodelab.wbgoodstracker.domain.models.SortingMode
import com.automotivecodelab.wbgoodstracker.domain.repositories.SortRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map

class SortRepositoryImpl(private val localDataSource: SortLocalDataSource): SortRepository {
    override fun getSortingModeComparator(): Flow<Comparator<Item>> {
        return localDataSource.getSortingMode()
            .map { sortingMode ->
                val comp = Comparator<Item> { o1, o2 ->
                    when (sortingMode) {
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
                    }
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    comp.thenComparing(Item::id)
                } else {
                    comp
                }
            }
    }

    override suspend fun setSortingMode(sortingMode: SortingMode) {
        localDataSource.setSortingMode(sortingMode)
    }
}
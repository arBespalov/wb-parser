package com.automotivecodelab.wbgoodstracker.domain.repositories

import com.automotivecodelab.wbgoodstracker.domain.models.Item
import com.automotivecodelab.wbgoodstracker.domain.models.SortingMode
import kotlinx.coroutines.flow.Flow
import java.util.Comparator

interface ItemsRepository {
    fun observeItems(groupName: String): Flow<List<Item>>
    fun observeSingleItem(id: String): Flow<Item>
    suspend fun deleteItems(itemsId: Array<String>)
    suspend fun deleteItems(itemsId: Array<String>, token: String)
    suspend fun updateItem(item: Item)
    suspend fun addItem(url: String, groupName: String): Result<Unit>
    suspend fun addItem(url: String, groupName: String, token: String): Result<Unit>
    suspend fun refreshSingleItem(item: Item): Result<Unit>
    suspend fun refreshAllItems(): Result<Unit>
    suspend fun syncItems(token: String): Result<Unit>
    suspend fun mergeItems(token: String): Result<Unit>
    suspend fun setItemsGroupName(itemIds: List<String>, groupName: String)
    suspend fun deleteGroup(groupName: String)
    suspend fun getGroups(): Array<String>
    suspend fun getSortingModeComparator(): Comparator<Item>
    suspend fun setSortingMode(sortingMode: SortingMode)
    suspend fun getCurrentGroup(): String
    suspend fun setCurrentGroup(groupName: String)
    suspend fun setDefaultGroup()
    suspend fun createNewGroup(groupName: String)
    suspend fun getOrdersChartData(itemId: String): Result<List<Pair<Long, Int>>>
}

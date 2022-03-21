package com.automotivecodelab.wbgoodstracker.domain.repositories

import com.automotivecodelab.wbgoodstracker.domain.models.Item
import com.automotivecodelab.wbgoodstracker.domain.models.SortingMode
import kotlinx.coroutines.flow.Flow
import java.util.Comparator

interface ItemsRepository {
    fun observeItems(): Flow<Pair<List<Item>, String?>>
    fun observeSingleItem(id: String): Flow<Item>
    suspend fun deleteItems(itemsId: Array<String>)
    suspend fun deleteItems(itemsId: Array<String>, token: String)
    suspend fun updateItem(item: Item)
    suspend fun addItem(url: String): Result<Unit>
    suspend fun addItem(url: String, token: String): Result<Unit>
    suspend fun refreshSingleItem(item: Item): Result<Unit>
    suspend fun refreshAllItems(): Result<Unit>
    suspend fun syncItems(token: String): Result<Unit>
    suspend fun mergeItems(token: String): Result<Unit>
    suspend fun addItemsToGroup(itemIds: List<String>, groupName: String?)
    suspend fun getOrdersChartData(itemId: String): Result<List<Pair<Long, Int>>>
    suspend fun deleteGroup(groupName: String)
    fun getGroups(): Flow<List<String>>
    suspend fun setCurrentGroup(groupName: String?)
}

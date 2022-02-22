package com.automotivecodelab.wbgoodstracker.domain.repositories

import androidx.lifecycle.LiveData
import com.automotivecodelab.wbgoodstracker.domain.util.Result
import com.automotivecodelab.wbgoodstracker.domain.models.Item
import com.automotivecodelab.wbgoodstracker.domain.models.SortingMode
import java.util.Comparator

interface ItemsRepository {
    fun observeItems(groupName: String): LiveData<List<Item>>
    fun observeSingleItem(id: String): LiveData<Item>
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
    fun getGroups(): Array<String>
    fun getSortingModeComparator(): Comparator<Item>
    fun setSortingMode(sortingMode: SortingMode)
    fun getCurrentGroup(): String
    fun setCurrentGroup(groupName: String)
    fun setDefaultGroup()
    fun createNewGroup(groupName: String)
    suspend fun getOrdersChartData(itemId: String): Result<List<Pair<Long, Int>>>
}
package com.automotivecodelab.wbgoodstracker.domain.repositories

import com.automotivecodelab.wbgoodstracker.domain.models.Ad
import com.automotivecodelab.wbgoodstracker.domain.models.Item
import com.automotivecodelab.wbgoodstracker.domain.models.ItemGroups
import com.automotivecodelab.wbgoodstracker.domain.models.MergeStatus
import kotlinx.coroutines.flow.Flow

interface ItemsRepository {
    val mergeStatus: Flow<MergeStatus>
    fun setMergeStatus(status: MergeStatus)
    fun observeItems(): Flow<Pair<List<Item>, String?>>
    fun observeSingleItem(id: String): Flow<Item>
    suspend fun deleteItems(itemsId: List<String>)
    suspend fun deleteItems(itemsId: List<String>, token: String)
    suspend fun setItemLocalName(itemId: String, localName: String?)
    suspend fun addItem(url: String): Result<Unit>
    suspend fun addItem(url: String, token: String): Result<Unit>
    suspend fun refreshSingleItem(itemId: String): Result<Unit>
    suspend fun refreshAllItems(): Result<Unit>
    suspend fun syncItems(token: String): Result<Unit>
    suspend fun mergeItems(token: String): Result<Unit>
    suspend fun mergeItemsDebug(userId: String): Result<Unit>
    suspend fun addItemsToGroup(itemIds: List<String>, groupName: String?)
    suspend fun getOrdersChartData(itemId: String): Result<List<Pair<Long, Int>>>
    suspend fun deleteGroup(groupName: String)
    fun observeGroups(): Flow<ItemGroups>
    suspend fun setCurrentGroup(groupName: String?)
    suspend fun renameCurrentGroup(newGroupName: String)
    fun observeCurrentGroup(): Flow<String?>
    suspend fun getQuantityChartData(itemId: String): Result<List<Pair<Long, Int>>>
    fun observeAd(): Flow<Ad?>
}

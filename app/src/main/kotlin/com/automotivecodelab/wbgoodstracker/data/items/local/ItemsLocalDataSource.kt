package com.automotivecodelab.wbgoodstracker.data.items.local

import com.automotivecodelab.wbgoodstracker.domain.models.ItemGroups
import kotlinx.coroutines.flow.Flow

interface ItemsLocalDataSource {
    fun observeAll(): Flow<List<ItemWithSizesDBModel>>
    fun observeByGroup(groupName: String): Flow<List<ItemWithSizesDBModel>>
    suspend fun getAll(): List<ItemWithSizesDBModel>
    suspend fun getByGroup(groupName: String): List<ItemWithSizesDBModel>
    suspend fun addItem(item: ItemWithSizesDBModel)
    suspend fun getItem(id: String): ItemWithSizesDBModel
    fun observeItem(id: String): Flow<ItemWithSizesDBModel>
    suspend fun deleteItems(itemsId: List<String>)
    suspend fun updateItem(items: List<ItemWithSizesDBModel>)
    fun observeItemGroups(): Flow<ItemGroups>
}

package com.automotivecodelab.wbgoodstracker.data.items.local

import androidx.lifecycle.LiveData
import com.automotivecodelab.wbgoodstracker.domain.models.SortingMode
import kotlinx.coroutines.flow.Flow

interface ItemsLocalDataSource {
    fun observeAll(): Flow<List<ItemWithSizesDBModel>>
    fun observeByGroup(groupName: String): Flow<List<ItemWithSizesDBModel>>
    suspend fun getAll(): List<ItemWithSizesDBModel>
    suspend fun getByGroup(groupName: String): List<ItemWithSizesDBModel>
    suspend fun addItem(item: ItemWithSizesDBModel)
    suspend fun getItem(id: String): ItemWithSizesDBModel
    fun observeItem(id: String): Flow<ItemWithSizesDBModel>
    suspend fun deleteItems(itemsId: Array<String>)
    suspend fun updateItem(vararg item: ItemWithSizesDBModel)
    fun getCurrentGroup(): Flow<String?>
    suspend fun setCurrentGroup(groupName: String?)
    fun getGroups(): Flow<List<String>>
}
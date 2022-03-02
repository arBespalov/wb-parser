package com.automotivecodelab.wbgoodstracker.data.items.local

import androidx.lifecycle.LiveData
import com.automotivecodelab.wbgoodstracker.domain.models.SortingMode

interface ItemsLocalDataSource {
    fun observeAll(): LiveData<List<ItemWithSizesDBModel>>
    fun observeByGroup(groupName: String): LiveData<List<ItemWithSizesDBModel>>
    suspend fun getAll(): List<ItemWithSizesDBModel>
    suspend fun getByGroup(groupName: String): List<ItemWithSizesDBModel>
    suspend fun addItem(item: ItemWithSizesDBModel)
    suspend fun getItem(id: String): ItemWithSizesDBModel
    fun observeItem(id: String): LiveData<ItemWithSizesDBModel>
    suspend fun deleteItems(itemsId: Array<String>)
    suspend fun updateItem(vararg item: ItemWithSizesDBModel)
    fun getCurrentGroup(): String?
    fun setCurrentGroup(groupName: String?)
    fun getSortingMode(): SortingMode
    fun setSortingMode(sortingMode: SortingMode)
    fun getGroups(): Array<String>
    fun deleteGroup(groupName: String)
    fun addGroup(groupName: String)
}

package com.automotivecodelab.wbgoodstracker.data.items.local

import androidx.lifecycle.LiveData
import com.automotivecodelab.wbgoodstracker.domain.models.Item
import com.automotivecodelab.wbgoodstracker.domain.models.SortingMode

interface ItemsLocalDataSource {

    fun observeAll(): LiveData<List<Item>>

    fun observeByGroup(groupName: String): LiveData<List<Item>>

    suspend fun getAll(): List<Item>

    suspend fun getByGroup(groupName: String): List<Item>

    suspend fun addItem(item: Item)

    suspend fun getItem(id: String): Item

    fun observeItem(id: String): LiveData<Item>

    suspend fun deleteItems(itemsId: Array<String>)

    suspend fun updateItem(item: Item): Int

    suspend fun updateItems(items: List<Item>): Int

    fun getCurrentGroup(): String?

    fun setCurrentGroup(groupName: String?)

    fun getSortingMode(): SortingMode

    fun setSortingMode(sortingMode: SortingMode)

    fun getGroups(): Array<String>

    fun deleteGroup(groupName: String)

    fun addGroup(groupName: String)
}

package com.automotivecodelab.wbgoodstracker.data.items.local

import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import com.automotivecodelab.wbgoodstracker.domain.models.SortingMode
import com.automotivecodelab.wbgoodstracker.domain.models.Item
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

const val SAVED_SORTING_MODE = "savedSortingMode"
const val GROUP_NAMES = "groupNames"
const val SAVED_CURRENT_GROUP = "savedCurrentGroup"
const val IS_USER_SIGNED_IN = "isUserSignedIn"

class ItemsLocalDataSourceImpl(
    private val itemDao: ItemDao,
    private val sharedPreferences: SharedPreferences
): ItemsLocalDataSource {
    override fun observeAll(): LiveData<List<Item>> {
        return itemDao.observeAll()
    }

    override fun observeByGroup(groupName: String): LiveData<List<Item>> {
        return itemDao.observeByGroup(groupName)
    }

    override suspend fun getAll(): List<Item> {
        return withContext(Dispatchers.IO) { itemDao.getAll() }
    }

    override suspend fun getByGroup(groupName: String): List<Item> {
        return withContext(Dispatchers.IO) { itemDao.getByGroup(groupName) }
    }

    override suspend fun addItem(item: Item) {
        return withContext(Dispatchers.IO) {itemDao.insert(item)}
    }

    override suspend fun getItem(id: String): Item {
        return withContext(Dispatchers.IO){ itemDao.getById(id) }
    }

    override fun observeItem(id: String): LiveData<Item> {
        return itemDao.observeById(id)
    }

    override suspend fun deleteItems(itemsId: Array<String>) {
        withContext(Dispatchers.IO){
            itemsId.forEach {
                val item = getItem(it)
                itemDao.delete(item)
            }
        }
    }

    override suspend fun updateItem(item: Item): Int {
        return withContext(Dispatchers.IO){ itemDao.update(item) }
    }

    override suspend fun updateItems(items: List<Item>): Int {
        return withContext(Dispatchers.IO){ itemDao.batchUpdate(items) }
    }

    override fun getCurrentGroup(): String? {
        return sharedPreferences.getString(SAVED_CURRENT_GROUP, null)
    }

    override fun setCurrentGroup(groupName: String?) {
        if (groupName.isNullOrEmpty()) {
            sharedPreferences.edit()
                .remove(SAVED_CURRENT_GROUP)
                .apply()
        } else {
            sharedPreferences.edit()
                .putString(SAVED_CURRENT_GROUP, groupName)
                .apply()
        }
    }

    override fun getSortingMode(): SortingMode {
        val ordinal = sharedPreferences.getInt(SAVED_SORTING_MODE, SortingMode.BY_DATE_DESC.ordinal)
        return SortingMode.values()[ordinal]
    }

    override fun setSortingMode(sortingMode: SortingMode) {
        sharedPreferences.edit()
            .putInt(SAVED_SORTING_MODE, sortingMode.ordinal)
            .apply()
    }

    override fun getGroups(): Array<String> {
        val groupNames = sharedPreferences.getStringSet(GROUP_NAMES, setOf())
        return groupNames?.toTypedArray() ?: arrayOf()
    }

    override fun deleteGroup(groupName: String) {
        val groupNames = sharedPreferences.getStringSet(GROUP_NAMES, setOf())
        val newGroupNames = HashSet<String>(groupNames)//modifying groupNames is not allowed according to docs
        newGroupNames.remove(groupName)
        sharedPreferences.edit()
            .putStringSet(GROUP_NAMES, newGroupNames)
            .remove(SAVED_CURRENT_GROUP)
            .apply()
    }

    override fun addGroup(groupName: String) {
        val groupNames = sharedPreferences.getStringSet(GROUP_NAMES, setOf())
        val newGroupNames = HashSet<String>(groupNames)//modifying groupNames is not allowed according to docs
        newGroupNames.add(groupName)
        sharedPreferences.edit()
            .putStringSet(GROUP_NAMES, newGroupNames)
            .apply()
    }
}
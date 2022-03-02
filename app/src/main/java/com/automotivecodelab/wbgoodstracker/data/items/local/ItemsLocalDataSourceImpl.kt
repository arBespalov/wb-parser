package com.automotivecodelab.wbgoodstracker.data.items.local

import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import com.automotivecodelab.wbgoodstracker.domain.models.SortingMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

const val SAVED_SORTING_MODE = "savedSortingMode"
const val GROUP_NAMES = "groupNames"
const val SAVED_CURRENT_GROUP = "savedCurrentGroup"

class ItemsLocalDataSourceImpl(
    private val itemDao: ItemDao,
    private val sizeDao: SizeDao,
    private val sharedPreferences: SharedPreferences
) : ItemsLocalDataSource {
    override fun observeAll(): LiveData<List<ItemWithSizesDBModel>> {
        return itemDao.observeAll()
    }

    override fun observeByGroup(groupName: String): LiveData<List<ItemWithSizesDBModel>> {
        return itemDao.observeByGroup(groupName)
    }

    override suspend fun getAll(): List<ItemWithSizesDBModel> {
        return withContext(Dispatchers.IO) { itemDao.getAll() }
    }

    override suspend fun getByGroup(groupName: String): List<ItemWithSizesDBModel> {
        return withContext(Dispatchers.IO) { itemDao.getByGroup(groupName) }
    }

    override suspend fun addItem(item: ItemWithSizesDBModel) {
        return withContext(Dispatchers.IO) {
        // todo make async
            itemDao.insert(item.item)
            sizeDao.insert(*item.sizes.toTypedArray())
        }
    }

    override suspend fun getItem(id: String): ItemWithSizesDBModel {
        return withContext(Dispatchers.IO) { itemDao.getById(id) }
    }

    override fun observeItem(id: String): LiveData<ItemWithSizesDBModel> {
        return itemDao.observeById(id)
    }

    override suspend fun deleteItems(itemsId: Array<String>) {
        withContext(Dispatchers.IO) {
            itemsId.forEach {
                val item = getItem(it)
                itemDao.delete(item.item)
            }
        }
    }

    override suspend fun updateItem(vararg item: ItemWithSizesDBModel) {
        return withContext(Dispatchers.IO) {
            itemDao.update(*item.map { it.item }.toTypedArray())
            item.forEach { updatedItem ->
                val localItemSizes = itemDao.getById(updatedItem.item.id).sizes
                val localItemSizeNames = localItemSizes.map { it.sizeName }
                val updatedItemSizeNames = updatedItem.sizes.map { it.sizeName }
                val sizesToAdd = updatedItemSizeNames.minus(localItemSizeNames)
                val sizesToDelete = localItemSizeNames.minus(updatedItemSizeNames)
                val sizesToUpdate = updatedItemSizeNames.minus(sizesToAdd).minus(sizesToDelete)

                sizesToAdd.map { sizeName ->
                    updatedItem.sizes.find { sizeDBModel ->
                        sizeDBModel.sizeName == sizeName
                    }!!
                }.also { list ->
                    sizeDao.insert(*list.toTypedArray())
                }

                sizesToDelete.map { sizeName ->
                    localItemSizes.find { sizeDBModel ->
                        sizeDBModel.sizeName == sizeName
                    }!!
                }.also { list ->
                    sizeDao.delete(*list.toTypedArray())
                }

                sizesToUpdate.map { sizeName ->
                    updatedItem.sizes.find { sizeDBModel ->
                        sizeDBModel.sizeName == sizeName
                    }!!
                }.also { list ->
                    sizeDao.update(*list.toTypedArray())
                }
            }
        }
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
        // modifying groupNames is not allowed according to docs
        val newGroupNames = HashSet<String>(groupNames)
        newGroupNames.remove(groupName)
        sharedPreferences.edit()
            .putStringSet(GROUP_NAMES, newGroupNames)
            .remove(SAVED_CURRENT_GROUP)
            .apply()
    }

    override fun addGroup(groupName: String) {
        val groupNames = sharedPreferences.getStringSet(GROUP_NAMES, setOf())
        // modifying groupNames is not allowed according to docs
        val newGroupNames = HashSet<String>(groupNames)
        newGroupNames.add(groupName)
        sharedPreferences.edit()
            .putStringSet(GROUP_NAMES, newGroupNames)
            .apply()
    }
}

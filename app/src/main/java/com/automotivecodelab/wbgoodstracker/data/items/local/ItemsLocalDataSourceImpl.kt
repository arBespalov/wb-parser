package com.automotivecodelab.wbgoodstracker.data.items.local

import android.content.SharedPreferences
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.LiveData
import com.automotivecodelab.wbgoodstracker.domain.models.SortingMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext

class ItemsLocalDataSourceImpl(
    private val itemDao: ItemDao,
    private val sizeDao: SizeDao,
    private val dataStore: DataStore<Preferences>
) : ItemsLocalDataSource {

    private val CURRENT_GROUP = stringPreferencesKey("current_group")

    override fun observeAll(): Flow<List<ItemWithSizesDBModel>> {
        return itemDao.observeAll()
    }

    override fun observeByGroup(groupName: String): Flow<List<ItemWithSizesDBModel>> {
        return itemDao.observeByGroup(groupName)
    }

    override suspend fun getAll(): List<ItemWithSizesDBModel> {
        return itemDao.getAll()
    }

    override suspend fun getByGroup(groupName: String): List<ItemWithSizesDBModel> {
        return itemDao.getByGroup(groupName)
    }

    override suspend fun addItem(item: ItemWithSizesDBModel) {
        // if make this calls async, sqlite throws "foreign key constraints failed"
        itemDao.insert(item.item)
        sizeDao.insert(*item.sizes.toTypedArray())
    }

    override suspend fun getItem(id: String): ItemWithSizesDBModel {
        return itemDao.getById(id)
    }

    override fun observeItem(id: String): Flow<ItemWithSizesDBModel> {
        return itemDao.observeById(id)
    }

    override suspend fun deleteItems(itemsId: Array<String>) {
        withContext(Dispatchers.IO) {
            itemsId.map {
                async {
                    val item = getItem(it)
                    itemDao.delete(item.item)
                }
            }.awaitAll()
        }

    }

    override suspend fun updateItem(vararg item: ItemWithSizesDBModel) {
        return withContext(Dispatchers.IO) {
            itemDao.update(*item.map { it.item }.toTypedArray())
            withContext(Dispatchers.IO) {
                item.map { updatedItem ->
                    async {
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
                }.awaitAll()
            }
        }
    }

    override fun getCurrentGroup(): Flow<String?> {
        return dataStore.data
            .map { prefs ->
                prefs[CURRENT_GROUP]
            }
            //.distinctUntilChanged()
    }

    override suspend fun setCurrentGroup(groupName: String?) {
        dataStore.edit { prefs ->
            if (groupName == null) {
                prefs.remove(CURRENT_GROUP)
            } else {
                prefs[CURRENT_GROUP] = groupName
            }
        }
    }

    override fun getGroups(): Flow<List<String>> {
        return itemDao.getGroups()
            .map {
                it.filterNotNull()
            }
            //.distinctUntilChanged()
    }
}

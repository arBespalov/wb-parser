package com.automotivecodelab.wbgoodstracker.data.items.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.room.RoomDatabase
import androidx.room.withTransaction
import com.automotivecodelab.wbgoodstracker.domain.models.ItemGroups
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import timber.log.Timber
import javax.inject.Inject

class ItemsLocalDataSourceImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>,
    private val appDatabase: AppDatabase
) : ItemsLocalDataSource {

    private val CURRENT_GROUP = stringPreferencesKey("current_group")

    override fun observeAll(): Flow<List<ItemWithSizesDBModel>> {
        return appDatabase.itemDao().observeAll()
    }

    override fun observeByGroup(groupName: String): Flow<List<ItemWithSizesDBModel>> {
        return appDatabase.itemDao().observeByGroup(groupName)
    }

    override suspend fun getAll(): List<ItemWithSizesDBModel> {
        return appDatabase.itemDao().getAll()
    }

    override suspend fun getByGroup(groupName: String): List<ItemWithSizesDBModel> {
        return appDatabase.itemDao().getByGroup(groupName)
    }

    override suspend fun addItem(item: ItemWithSizesDBModel) {
        appDatabase.withTransaction {
            appDatabase.itemDao().insert(item.item)
            appDatabase.sizeDao().insert(*item.sizes.toTypedArray())
        }
    }

    override suspend fun getItem(id: String): ItemWithSizesDBModel {
        return appDatabase.itemDao().getById(id)
    }

    override fun observeItem(id: String): Flow<ItemWithSizesDBModel> {
        return appDatabase.itemDao().observeById(id)
    }

    override suspend fun deleteItems(itemsId: List<String>) {
        withContext(Dispatchers.IO) {
            itemsId.map {
                async {
                    val item = getItem(it)
                    appDatabase.itemDao().delete(item.item)
                }
            }.awaitAll()
        }

    }

    override suspend fun updateItem(vararg item: ItemWithSizesDBModel) {
            appDatabase.withTransaction {
                withContext(Dispatchers.IO) {
                    appDatabase.itemDao().update(*item.map { it.item }.toTypedArray())
                    item.map { updatedItem ->
                        async {
                            val localItemSizes = appDatabase.itemDao()
                                .getById(updatedItem.item.id).sizes
                            val localItemSizeNames = localItemSizes.map { it.sizeName }
                            val updatedItemSizeNames = updatedItem.sizes.map { it.sizeName }
                            val sizesToAdd = updatedItemSizeNames.minus(localItemSizeNames)
                            val sizesToDelete = localItemSizeNames.minus(updatedItemSizeNames)
                            val sizesToUpdate = updatedItemSizeNames.minus(sizesToAdd)
                                .minus(sizesToDelete)
                            sizesToAdd.map { sizeName ->
                                updatedItem.sizes.find { sizeDBModel ->
                                    sizeDBModel.sizeName == sizeName
                                }!!
                            }.also { list ->
                                appDatabase.sizeDao().insert(*list.toTypedArray())
                            }
                            sizesToDelete.map { sizeName ->
                                localItemSizes.find { sizeDBModel ->
                                    sizeDBModel.sizeName == sizeName
                                }!!
                            }.also { list ->
                                appDatabase.sizeDao().delete(*list.toTypedArray())
                            }
                            sizesToUpdate.map { sizeName ->
                                updatedItem.sizes.find { sizeDBModel ->
                                    sizeDBModel.sizeName == sizeName
                                }!!
                            }.also { list ->
                                appDatabase.sizeDao().update(*list.toTypedArray())
                            }
                        }
                    }.awaitAll()
                }
            }
    }

    override fun observeCurrentGroup(): Flow<String?> {
        return dataStore.data
            .map { prefs ->
                prefs[CURRENT_GROUP]
            }
            .distinctUntilChanged()
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

    override fun observeItemGroups(): Flow<ItemGroups> {
        return appDatabase.itemDao().getGroups()
            .map { list ->
                ItemGroups(
                    totalItemsQuantity = list.sumOf { it.count },
                    groups = list
                        .filter { it.groupName != null }
                        .map { it.groupName!! to it.count }
                )
            }
            .distinctUntilChanged()
    }
}

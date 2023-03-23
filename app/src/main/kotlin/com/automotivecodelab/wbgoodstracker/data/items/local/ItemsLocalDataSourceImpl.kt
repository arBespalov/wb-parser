package com.automotivecodelab.wbgoodstracker.data.items.local

import androidx.room.withTransaction
import com.automotivecodelab.wbgoodstracker.domain.models.ItemGroups
import javax.inject.Inject
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

class ItemsLocalDataSourceImpl @Inject constructor(
    private val appDatabase: AppDatabase
) : ItemsLocalDataSource {

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
        return appDatabase.itemDao().getById(id) ?: throw IllegalArgumentException("item id: $id")
    }

    override fun observeItem(id: String): Flow<ItemWithSizesDBModel> {
        return appDatabase.itemDao().observeById(id)
            .map {
                it ?: throw IllegalArgumentException("item id: $id")
            }
    }

    override suspend fun deleteItems(itemsId: List<String>) {
        coroutineScope {
            itemsId.map {
                async {
                    val item = getItem(it)
                    appDatabase.itemDao().delete(item.item)
                }
            }.awaitAll()
        }
    }

    override suspend fun updateItem(items: List<ItemWithSizesDBModel>) {
        appDatabase.withTransaction {
            // db calls will switch to io dispatcher by themselves
            withContext(Dispatchers.Default) {
                appDatabase.itemDao().update(items.map { it.item })
                items.map { updatedItem ->
                    async {
                        val localItemSizes = getItem(updatedItem.item.id).sizes
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
                            appDatabase.sizeDao().update(list)
                        }
                    }
                }.awaitAll()
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

package com.automotivecodelab.wbgoodstracker.data.items

import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import com.automotivecodelab.wbgoodstracker.data.ResourceManager
import com.automotivecodelab.wbgoodstracker.data.items.local.ItemWithSizesDBModel
import com.automotivecodelab.wbgoodstracker.data.items.local.ItemsLocalDataSource
import com.automotivecodelab.wbgoodstracker.data.items.local.toDBModel
import com.automotivecodelab.wbgoodstracker.data.items.local.toDomainModel
import com.automotivecodelab.wbgoodstracker.data.items.remote.ItemsRemoteDataSource
import com.automotivecodelab.wbgoodstracker.data.items.remote.toDBModel
import com.automotivecodelab.wbgoodstracker.domain.models.Item
import com.automotivecodelab.wbgoodstracker.domain.models.SortingMode
import com.automotivecodelab.wbgoodstracker.domain.repositories.ItemsRepository
import com.automotivecodelab.wbgoodstracker.log
import java.util.*
import kotlin.Comparator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class ItemsRepositoryImpl(
    private val localDataSource: ItemsLocalDataSource,
    private val remoteDataSource: ItemsRemoteDataSource,
    private val resourceManager: ResourceManager
) : ItemsRepository {

    override fun observeItems(groupName: String): Flow<List<Item>> {
        return if (groupName == resourceManager.getAllItemsString()) {
            localDataSource.observeAll().map { list ->
                list.map { dbModel ->
                    dbModel.toDomainModel()
                }
            }
        } else {
            localDataSource.observeByGroup(groupName).map { list ->
                list.map { dbModel ->
                    dbModel.toDomainModel()
                }
            }
        }
    }

    override fun observeSingleItem(id: String): Flow<Item> {
        return localDataSource.observeItem(id).map { dbModel ->
            dbModel.toDomainModel()
        }
    }

    override suspend fun deleteItems(itemsId: Array<String>) {
        deleteItemsWithNullableToken(itemsId, null)
    }

    override suspend fun deleteItems(itemsId: Array<String>, token: String) {
        deleteItemsWithNullableToken(itemsId, token)
    }

    private suspend fun deleteItemsWithNullableToken(itemsId: Array<String>, token: String?) {
        localDataSource.deleteItems(itemsId)
        runCatching {
            if (token != null) {
                remoteDataSource.deleteItems(itemsId.map { id -> id.toInt() }, token)
            }
        }
    }

    override suspend fun updateItem(item: Item) {
        if (item.localName == resourceManager.getAllItemsString()) {
            item.copy(localName = null)
        } else {
            item
        }.also {
            localDataSource.updateItem(it.toDBModel())
        }
    }

    override suspend fun addItem(url: String, groupName: String): Result<Unit> {
        return addItemWithNullableToken(url, groupName, null)
    }

    override suspend fun addItem(url: String, groupName: String, token: String): Result<Unit> {
        return addItemWithNullableToken(url, groupName, token)
    }

    private suspend fun addItemWithNullableToken(
        url: String,
        groupName: String,
        token: String?
    ): Result<Unit> {
        return runCatching {
            withContext(Dispatchers.IO) {
                val newItemDeferred = async { remoteDataSource.addItem(url, token) }
                val localItemsDeferred = async { localDataSource.getAll() }

                val newItem = newItemDeferred.await()
                val localItems = localItemsDeferred.await()

                val nullableGroupName = if (groupName == resourceManager.getAllItemsString())
                    null
                else groupName

                localItems.find { itemWithSizes -> newItem._id == itemWithSizes.item.id }
                    ?.also { dbModel ->
                        localDataSource.updateItem(newItem.toDBModel(
                            creationTimestamp = dbModel.item.creationTimestamp,
                            previousOrdersCount = dbModel.item.ordersCount,
                            previousAveragePrice = dbModel.item.averagePrice,
                            previousTotalQuantity = dbModel.item.totalQuantity,
                            localName = dbModel.item.localName,
                            groupName = nullableGroupName
                        ))
                        return@withContext
                    }

                localDataSource.addItem(
                    newItem.toDBModel(
                        creationTimestamp = Date().time,
                        previousOrdersCount = newItem.info[0].ordersCount,
                        previousAveragePrice = newItem.averagePrice,
                        previousTotalQuantity = newItem.totalQuantity,
                        localName = null,
                        groupName = nullableGroupName
                    )
                )
            }
        }
    }

    override suspend fun refreshSingleItem(item: Item): Result<Unit> {
        return refreshItems(listOf(item))
    }

    override suspend fun refreshAllItems(): Result<Unit> {
        localDataSource.getAll().also {
            return refreshItems(it.map { dbItem -> dbItem.toDomainModel() })
        }
    }

    //todo test
    override suspend fun syncItems(token: String): Result<Unit> {
        return runCatching {
            withContext(Dispatchers.IO) {
                val serverItemsDeferred = async { remoteDataSource.getItemsForUserId(token) }
                val localItemsDeferred = async { localDataSource.getAll() }

                val serverItems = serverItemsDeferred.await()
                val localItems = localItemsDeferred.await()

                val serverItemIds = serverItems.map { it._id }
                val localItemIds = localItems.map { it.item.id }

                val itemIdsToDelete = localItemIds.minus(serverItemIds)
                val itemIdsToAdd = serverItemIds.minus(localItemIds)
                val itemIdsToUpdate = serverItemIds.minus(itemIdsToAdd).minus(itemIdsToDelete)

                log("items to delete: ${itemIdsToDelete.size}")
                log("items to add: ${itemIdsToAdd.size}")

                localDataSource.deleteItems(itemIdsToDelete.toTypedArray())

                itemIdsToAdd.forEach { id ->
                    val item = serverItems.find { remoteItem -> remoteItem._id == id }!!
                    localDataSource.addItem(
                        item.toDBModel(
                            creationTimestamp = Date().time,
                            previousOrdersCount = item.info[0].ordersCount,
                            previousAveragePrice = item.averagePrice,
                            previousTotalQuantity = item.totalQuantity,
                            localName = null,
                            groupName = null
                        )
                    )
                }

                itemIdsToUpdate.map { id ->
                    val localItem = localItems.find { localItem -> localItem.item.id == id }!!
                    val updatedItem = serverItems.find { remoteItem -> remoteItem._id == id }!!

                    updatedItem.toDBModel(
                        creationTimestamp = localItem.item.creationTimestamp,
                        previousOrdersCount = localItem.item.ordersCount,
                        previousAveragePrice = localItem.item.averagePrice,
                        previousTotalQuantity = localItem.item.totalQuantity,
                        localName = localItem.item.localName,
                        groupName = localItem.item.groupName
                    )
                }.also {
                    localDataSource.updateItem(*it.toTypedArray())
                }
            }
        }
    }

    private suspend fun refreshItems(items: List<Item>): Result<Unit> {
        return runCatching {
            val itemIds = items.map { item -> item.id.toInt() }
            val updatedItems = remoteDataSource.updateItems(itemIds)
            updatedItems.map { updatedItem ->
                val localItem = items.find { localItem -> localItem.id == updatedItem._id }!!
                updatedItem.toDBModel(
                    creationTimestamp = localItem.creationTimestamp,
                    previousOrdersCount = localItem.ordersCount,
                    previousAveragePrice = localItem.averagePrice,
                    previousTotalQuantity = localItem.totalQuantity,
                    localName = localItem.localName,
                    groupName = localItem.groupName
                )
            }.also {
                localDataSource.updateItem(*it.toTypedArray())
            }
        }
    }

    //todo test
    override suspend fun mergeItems(token: String): Result<Unit> {
        return runCatching {
            val localItems = localDataSource.getAll()
            val mergedItems = remoteDataSource.mergeItems(
                localItems.map { localItem -> localItem.item.id.toInt() },
                token
            )
            val localItemIds = localItems.map { localItem -> localItem.item.id }
            val mergedItemIds = mergedItems.map { remoteItem -> remoteItem._id }
            val itemIdsToAdd = mergedItemIds.minus(localItemIds)
            val itemIdsToUpdate = mergedItemIds.minus(itemIdsToAdd)
            log("items to add: ${itemIdsToAdd.size}")
            itemIdsToAdd.forEach { id ->
                val item = mergedItems.find { remoteItem -> remoteItem._id == id }!!
                localDataSource.addItem(
                    item.toDBModel(
                        creationTimestamp = Date().time,
                        previousOrdersCount = item.info[0].ordersCount,
                        previousAveragePrice = item.averagePrice,
                        previousTotalQuantity = item.totalQuantity,
                        localName = null,
                        groupName = null
                    )
                )
            }
            itemIdsToUpdate.forEach { id ->
                val updatedItem = mergedItems.find { remoteItem -> remoteItem._id == id }!!
                localItems.find { localItem -> localItem.item.id == id }?.also { localItem ->
                    localDataSource.updateItem(updatedItem.toDBModel(
                        creationTimestamp = localItem.item.creationTimestamp,
                        previousOrdersCount = localItem.item.ordersCount,
                        previousAveragePrice = localItem.item.averagePrice,
                        previousTotalQuantity = localItem.item.totalQuantity,
                        localName = localItem.item.localName,
                        groupName = localItem.item.groupName
                    ))
                }
            }
        }
    }

    override suspend fun setItemsGroupName(itemIds: List<String>, groupName: String) {
        setGroupNameToItemsList(itemIds.map { localDataSource.getItem(it) }, groupName)
    }

    private suspend fun setGroupNameToItemsList(
        items: List<ItemWithSizesDBModel>,
        groupName: String
    ) {
        if (groupName == resourceManager.getAllItemsString()) {
            null
        } else {
            groupName
        }.also {
            localDataSource.updateItem(*items.map { item ->
                item.copy(
                    item = item.item.copy(groupName = it)
                )
            }.toTypedArray())
        }
    }

    override suspend fun deleteGroup(groupName: String) {
        if (groupName != resourceManager.getAllItemsString()) {
            val items = localDataSource.getByGroup(groupName)
            setGroupNameToItemsList(items, resourceManager.getAllItemsString())
            localDataSource.deleteGroup(groupName)
        }
    }

    override suspend fun getGroups(): Array<String> {
        return localDataSource.getGroups().plus(resourceManager.getAllItemsString())
    }

    override suspend fun getSortingModeComparator(): Comparator<Item> {
        return when (localDataSource.getSortingMode()) {
            SortingMode.BY_NAME_ASC -> Comparator { o1, o2 -> o1.name.compareTo(o2.name) }
            SortingMode.BY_NAME_DESC -> Comparator { o1, o2 -> o2.name.compareTo(o1.name) }
            SortingMode.BY_DATE_ASC -> Comparator { o1, o2 ->
                o2.creationTimestamp.compareTo(o1.creationTimestamp)
            }
            SortingMode.BY_DATE_DESC -> Comparator { o1, o2 ->
                o1.creationTimestamp.compareTo(o2.creationTimestamp)
            }
            SortingMode.BY_ORDERS_COUNT -> Comparator { o1, o2 ->
                o2.ordersCount.compareTo(o1.ordersCount)
            }
            SortingMode.BY_ORDERS_COUNT_PER_DAY -> Comparator { o1, o2 ->
                o2.averageOrdersCountPerDay.compareTo(o1.averageOrdersCountPerDay)
            }
        }
    }

    override suspend fun setSortingMode(sortingMode: SortingMode) {
        localDataSource.setSortingMode(sortingMode)
    }

    override suspend fun getCurrentGroup(): String {
        return localDataSource.getCurrentGroup() ?: resourceManager.getAllItemsString()
    }

    override suspend fun setCurrentGroup(groupName: String) {
        if (groupName == resourceManager.getAllItemsString()) {
            null
        } else {
            groupName
        }.also {
            localDataSource.setCurrentGroup(it)
        }
    }

    override suspend fun setDefaultGroup() {
        localDataSource.setCurrentGroup(null)
    }

    override suspend fun createNewGroup(groupName: String) {
        if (groupName != resourceManager.getAllItemsString()) {
            localDataSource.addGroup(groupName)
        }
    }

    override suspend fun getOrdersChartData(itemId: String): Result<List<Pair<Long, Int>>> {
        return runCatching {
            val item = remoteDataSource.getItemWithFullData(itemId)
            item.info.map {
                Pair(it.timeOfCreationInMs, it.ordersCount)
            }
        }
    }
}

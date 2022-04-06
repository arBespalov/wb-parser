package com.automotivecodelab.wbgoodstracker.data.items

import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import com.automotivecodelab.wbgoodstracker.data.items.local.ItemWithSizesDBModel
import com.automotivecodelab.wbgoodstracker.data.items.local.ItemsLocalDataSource
import com.automotivecodelab.wbgoodstracker.data.items.local.toDBModel
import com.automotivecodelab.wbgoodstracker.data.items.local.toDomainModel
import com.automotivecodelab.wbgoodstracker.data.items.remote.ItemsRemoteDataSource
import com.automotivecodelab.wbgoodstracker.data.items.remote.toDBModel
import com.automotivecodelab.wbgoodstracker.domain.models.Item
import com.automotivecodelab.wbgoodstracker.domain.models.SortingMode
import com.automotivecodelab.wbgoodstracker.domain.repositories.ItemsRepository
import kotlinx.coroutines.*
import java.util.*
import kotlinx.coroutines.flow.*
import timber.log.Timber
import javax.inject.Inject

class ItemsRepositoryImpl @Inject constructor(
    private val localDataSource: ItemsLocalDataSource,
    private val remoteDataSource: ItemsRemoteDataSource,
) : ItemsRepository {

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun observeItems(): Flow<Pair<List<Item>, String?>> {
        var _group: String? = null
        return localDataSource.getCurrentGroup()
            .flatMapLatest { group ->
                _group = group
                if (group == null) {
                    localDataSource.observeAll()
                } else {
                    localDataSource.observeByGroup(group)
                }
            }
            .map {
                it.map { itemDBModel -> itemDBModel.toDomainModel() } to _group
            }
    }

    override fun observeSingleItem(id: String): Flow<Item> {
        return localDataSource.observeItem(id)
            .map { dbModel ->
                dbModel.toDomainModel()
            }
    }

    override suspend fun deleteItems(itemsId: Array<String>) {
        deleteItemsWithNullableToken(itemsId, null)
    }

    override suspend fun deleteItems(itemsId: Array<String>, token: String) {
        deleteItemsWithNullableToken(itemsId, token)
    }

    @OptIn(DelicateCoroutinesApi::class)
    private suspend fun deleteItemsWithNullableToken(itemsId: Array<String>, token: String?) {
        runCatching {
            val currentGroup = localDataSource.getCurrentGroup().first()
            if (currentGroup != null && localDataSource.getByGroup(currentGroup).size == 1) {
                setCurrentGroup(null)
            }
            localDataSource.deleteItems(itemsId)
            if (token != null) {
                GlobalScope.launch {
                    remoteDataSource.deleteItems(itemsId.map { id -> id.toInt() }, token)
                }
            }
        }
    }

    override suspend fun updateItem(item: Item) {
        localDataSource.updateItem(item.toDBModel())
    }

    override suspend fun addItem(url: String): Result<Unit> {
        return addItemWithNullableToken(url, null)
    }

    override suspend fun addItem(url: String, token: String): Result<Unit> {
        return addItemWithNullableToken(url, token)
    }

    private suspend fun addItemWithNullableToken(
        url: String,
        token: String?
    ): Result<Unit> {
        return runCatching {
            withContext(Dispatchers.IO) {
                val newItemDeferred = async { remoteDataSource.addItem(url, token) }
                val localItemsDeferred = async { localDataSource.getAll() }
                val currentGroupDeferred = async { localDataSource.getCurrentGroup().first() }

                val newItem = newItemDeferred.await()
                val localItems = localItemsDeferred.await()
                val currentGroup = currentGroupDeferred.await()

                localItems.find {
                        itemWithSizes -> newItem._id == itemWithSizes.item.id
                }?.let { dbModel ->
                    localDataSource.updateItem(newItem.toDBModel(
                        creationTimestamp = dbModel.item.creationTimestamp,
                        previousOrdersCount = dbModel.item.ordersCount,
                        previousAveragePrice = dbModel.item.averagePrice,
                        previousTotalQuantity = dbModel.item.totalQuantity,
                        localName = dbModel.item.localName,
                        groupName = dbModel.item.groupName
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
                        groupName = currentGroup
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

                Timber.d("items to delete: ${itemIdsToDelete.size}")
                Timber.d("items to add: ${itemIdsToAdd.size}")

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
            Timber.d("items to add: ${itemIdsToAdd.size}")
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

    override suspend fun addItemsToGroup(itemIds: List<String>, groupName: String?) {
        setGroupNameToItemsList(itemIds.map { localDataSource.getItem(it) }, groupName)
        val currentGroup = localDataSource.getCurrentGroup().first()
        if (currentGroup != null && localDataSource.getByGroup(currentGroup).isEmpty()) {
            setCurrentGroup(null)
        }
    }

    override suspend fun renameCurrentGroup(newGroupName: String) {
        val currentGroup = localDataSource.getCurrentGroup().first()
        if (currentGroup != null) {
            val items = localDataSource.getByGroup(currentGroup)
            setGroupNameToItemsList(items, newGroupName)
            setCurrentGroup(newGroupName)
        }
    }

    override fun observeCurrentGroup(): Flow<String?> {
        return localDataSource.getCurrentGroup()
    }

    private suspend fun setGroupNameToItemsList(
        items: List<ItemWithSizesDBModel>,
        groupName: String?
    ) {
        localDataSource.updateItem(*items.map { item ->
            item.copy(
                item = item.item.copy(groupName = groupName)
            )
        }.toTypedArray())
    }

    override suspend fun getOrdersChartData(itemId: String): Result<List<Pair<Long, Int>>> {
        return runCatching {
            val item = remoteDataSource.getItemWithFullData(itemId)
            item.info.map {
                it.timeOfCreationInMs to it.ordersCount
            }
        }
    }

    override suspend fun deleteGroup(groupName: String) {
        val items = localDataSource.getByGroup(groupName)
        setGroupNameToItemsList(items, null)
        setCurrentGroup(null)
    }

    override fun getGroups(): Flow<List<String>> {
        return localDataSource.getGroups()
    }

    override suspend fun setCurrentGroup(groupName: String?) {
        localDataSource.setCurrentGroup(groupName)
    }
}

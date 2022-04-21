package com.automotivecodelab.wbgoodstracker.data.items

import com.automotivecodelab.wbgoodstracker.data.items.local.ItemWithSizesDBModel
import com.automotivecodelab.wbgoodstracker.data.items.local.ItemsLocalDataSource
import com.automotivecodelab.wbgoodstracker.data.items.local.toDomainModel
import com.automotivecodelab.wbgoodstracker.data.items.remote.ItemsRemoteDataSource
import com.automotivecodelab.wbgoodstracker.data.items.remote.toDBModel
import com.automotivecodelab.wbgoodstracker.domain.models.Item
import com.automotivecodelab.wbgoodstracker.domain.models.ItemGroups
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
        return localDataSource.observeCurrentGroup()
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
            .distinctUntilChanged()
    }

    override fun observeSingleItem(id: String): Flow<Item> {
        return localDataSource.observeItem(id)
            .map { dbModel ->
                dbModel.toDomainModel()
            }
            .distinctUntilChanged()
    }

    override suspend fun deleteItems(itemsId: List<String>) {
        deleteItemsWithNullableToken(itemsId, null)
    }

    override suspend fun deleteItems(itemsId: List<String>, token: String) {
        deleteItemsWithNullableToken(itemsId, token)
    }

    override suspend fun setItemLocalName(itemId: String, localName: String?) {
        val itemDbModel = localDataSource.getItem(itemId)
        localDataSource.updateItem(
            itemDbModel.copy(
                item = itemDbModel.item.copy(
                    localName = localName
                )
            )
        )
    }

    @OptIn(DelicateCoroutinesApi::class)
    private suspend fun deleteItemsWithNullableToken(itemsId: List<String>, token: String?) {
        val currentGroup = localDataSource.observeCurrentGroup().first()
        if (currentGroup != null && localDataSource.getByGroup(currentGroup).size == 1) {
            setCurrentGroup(null)
        }
        localDataSource.deleteItems(itemsId)
        if (token != null) {
            GlobalScope.launch {
                runCatching {
                    remoteDataSource.deleteItems(itemsId.map { id -> id.toInt() }, token)
                }.onFailure { Timber.d(it) }
            }
        }
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
                val currentGroupDeferred = async { localDataSource.observeCurrentGroup().first() }

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
                        groupName = dbModel.item.groupName,
                        previousLastChangesTimestamp =
                            dbModel.item.lastChangesTimestamp,
                        previousSizeQuantity = dbModel.sizes.associate { sizeDBModel ->
                            sizeDBModel.sizeName to sizeDBModel.quantity
                        },
                        previousFeedbacks = dbModel.item.feedbacks
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
                        groupName = currentGroup,
                        previousLastChangesTimestamp = 0,
                        previousSizeQuantity = null,
                        previousFeedbacks = newItem.feedbacks
                    )
                )
            }
        }
    }

    override suspend fun refreshSingleItem(itemId: String): Result<Unit> {
        return refreshItems(listOf(itemId))
    }

    override suspend fun refreshAllItems(): Result<Unit> {
        return refreshItems(localDataSource.getAll().map { dbItem -> dbItem.item.id })
    }

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

                localDataSource.deleteItems(itemIdsToDelete)

                itemIdsToAdd.forEach { id ->
                    val item = serverItems.find { remoteItem -> remoteItem._id == id }!!
                    localDataSource.addItem(
                        item.toDBModel(
                            creationTimestamp = Date().time,
                            previousOrdersCount = item.info[0].ordersCount,
                            previousAveragePrice = item.averagePrice,
                            previousTotalQuantity = item.totalQuantity,
                            localName = null,
                            groupName = null,
                            previousLastChangesTimestamp = 0,
                            previousSizeQuantity = null,
                            previousFeedbacks = item.feedbacks
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
                        groupName = localItem.item.groupName,
                        previousLastChangesTimestamp =
                            localItem.item.lastChangesTimestamp,
                        previousSizeQuantity = localItem.sizes.associate { sizeDBModel ->
                            sizeDBModel.sizeName to sizeDBModel.quantity
                        },
                        previousFeedbacks = localItem.item.feedbacks
                    )
                }.also {
                    localDataSource.updateItem(*it.toTypedArray())
                }
            }
        }
    }

    private suspend fun refreshItems(itemIds: List<String>): Result<Unit> {
        return runCatching {
            val intItemIds = itemIds.map { it.toInt() }
            val updatedItems = remoteDataSource.updateItems(intItemIds)
            updatedItems.map { updatedItem ->
                val localItem = localDataSource.getItem(updatedItem._id)
                updatedItem.toDBModel(
                    creationTimestamp = localItem.item.creationTimestamp,
                    previousOrdersCount = localItem.item.ordersCount,
                    previousAveragePrice = localItem.item.averagePrice,
                    previousTotalQuantity = localItem.item.totalQuantity,
                    localName = localItem.item.localName,
                    groupName = localItem.item.groupName,
                    previousLastChangesTimestamp =
                        localItem.item.lastChangesTimestamp,
                    previousSizeQuantity = localItem.sizes.associate { sizeDBModel ->
                        sizeDBModel.sizeName to sizeDBModel.quantity
                    },
                    previousFeedbacks = localItem.item.feedbacks
                )
            }.also {
                localDataSource.updateItem(*it.toTypedArray())
            }
        }
    }

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
                        groupName = null,
                        previousLastChangesTimestamp = 0,
                        previousSizeQuantity = null,
                        previousFeedbacks = item.feedbacks
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
                        groupName = localItem.item.groupName,
                        previousLastChangesTimestamp =
                            localItem.item.lastChangesTimestamp,
                        previousSizeQuantity = localItem.sizes.associate { sizeDBModel ->
                            sizeDBModel.sizeName to sizeDBModel.quantity
                        },
                        previousFeedbacks = localItem.item.feedbacks
                    ))
                }
            }
        }
    }

    override suspend fun addItemsToGroup(itemIds: List<String>, groupName: String?) {
        setGroupNameToItemsList(itemIds.map { localDataSource.getItem(it) }, groupName)
        val currentGroup = localDataSource.observeCurrentGroup().first()
        if (currentGroup != null && localDataSource.getByGroup(currentGroup).isEmpty()) {
            setCurrentGroup(null)
        }
    }

    override suspend fun renameCurrentGroup(newGroupName: String) {
        val currentGroup = localDataSource.observeCurrentGroup().first()
        if (currentGroup != null) {
            val items = localDataSource.getByGroup(currentGroup)
            setGroupNameToItemsList(items, newGroupName)
            setCurrentGroup(newGroupName)
        }
    }

    override fun observeCurrentGroup(): Flow<String?> {
        return localDataSource.observeCurrentGroup()
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

    override fun observeGroups(): Flow<ItemGroups> {
        return localDataSource.observeItemGroups()
    }

    override suspend fun setCurrentGroup(groupName: String?) {
        localDataSource.setCurrentGroup(groupName)
    }
}

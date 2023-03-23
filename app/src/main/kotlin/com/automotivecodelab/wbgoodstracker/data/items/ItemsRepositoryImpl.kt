package com.automotivecodelab.wbgoodstracker.data.items

import com.automotivecodelab.wbgoodstracker.data.items.local.*
import com.automotivecodelab.wbgoodstracker.data.items.remote.ItemRemoteModel
import com.automotivecodelab.wbgoodstracker.data.items.remote.ItemsAndAdRemoteDataSource
import com.automotivecodelab.wbgoodstracker.data.items.remote.toDBModel
import com.automotivecodelab.wbgoodstracker.data.items.remote.toDomainModel
import com.automotivecodelab.wbgoodstracker.domain.models.Ad
import com.automotivecodelab.wbgoodstracker.domain.models.Item
import com.automotivecodelab.wbgoodstracker.domain.models.ItemGroups
import com.automotivecodelab.wbgoodstracker.domain.models.MergeStatus
import com.automotivecodelab.wbgoodstracker.domain.repositories.ItemsRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import timber.log.Timber
import java.util.*
import javax.inject.Inject

class ItemsRepositoryImpl @Inject constructor(
    private val itemsLocalDataSource: ItemsLocalDataSource,
    private val remoteDataSource: ItemsAndAdRemoteDataSource,
    private val currentGroupLocalDataSource: CurrentGroupLocalDataSource,
    private val adLocalDataSource: AdLocalDataSource,
    private val scope: CoroutineScope
) : ItemsRepository {

    private val _mergeStatus = MutableStateFlow<MergeStatus>(MergeStatus.Idle)
    override val mergeStatus: Flow<MergeStatus> = _mergeStatus

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun observeItems(): Flow<Pair<List<Item>, String?>> {
        var _group: String? = null
        return currentGroupLocalDataSource.observeCurrentGroup()
            .flatMapLatest { group ->
                _group = group
                if (group == null) {
                    itemsLocalDataSource.observeAll()
                } else {
                    itemsLocalDataSource.observeByGroup(group)
                }
            }
            .map {
                it.map { itemDBModel -> itemDBModel.toDomainModel() } to _group
            }
            .distinctUntilChanged()
    }

    override fun observeSingleItem(id: String): Flow<Item> {
        return itemsLocalDataSource.observeItem(id)
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
        val itemDbModel = itemsLocalDataSource.getItem(itemId)
        itemsLocalDataSource.updateItem(
            listOf(
                itemDbModel.copy(
                    item = itemDbModel.item.copy(
                        localName = localName
                    )
                )
            )
        )
    }

    override suspend fun addItem(url: String): Result<Unit> {
        return addItemWithNullableToken(url, null)
    }

    override suspend fun addItem(url: String, token: String): Result<Unit> {
        return addItemWithNullableToken(url, token)
    }

    override suspend fun refreshSingleItem(itemId: String): Result<Unit> {
        return refreshItems(listOf(itemId))
    }

    override suspend fun refreshAllItems(): Result<Unit> {
        return refreshItems(itemsLocalDataSource.getAll().map { dbItem -> dbItem.item.id })
    }

    override suspend fun syncItems(token: String): Result<Unit> {
        return runCatching {
            // network and db calls will switch to io dispatcher by themselves
            withContext(Dispatchers.Default) {
                val serverResponseDeferred = async { remoteDataSource.getItemsAndAdForUserId(token) }
                val localItemsDeferred = async { itemsLocalDataSource.getAll() }

                val serverResponse = serverResponseDeferred.await()
                val localItems = localItemsDeferred.await()

                val serverItems = serverResponse.items
                adLocalDataSource.setAd(serverResponse.ad?.toDomainModel())

                val serverItemIds = serverItems.map { it._id }
                val localItemIds = localItems.map { it.item.id }

                val itemIdsToDelete = localItemIds.minus(serverItemIds)
                val itemIdsToAdd = serverItemIds.minus(localItemIds)
                val itemIdsToUpdate = serverItemIds.minus(itemIdsToAdd).minus(itemIdsToDelete)

                Timber.d("items to delete: ${itemIdsToDelete.size}")
                Timber.d("items to add: ${itemIdsToAdd.size}")

                itemsLocalDataSource.deleteItems(itemIdsToDelete)

                itemIdsToAdd.map { id ->
                    async {
                        val item = serverItems.find { remoteItem -> remoteItem._id == id }!!
                        itemsLocalDataSource.addItem(
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
                }.awaitAll()

                itemIdsToUpdate.map { id ->
                    async {
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
                    }
                }
                    .awaitAll()
                    .also { itemsLocalDataSource.updateItem(it) }
            }
        }
    }

    override suspend fun mergeItems(token: String) {
        if (_mergeStatus.value == MergeStatus.InProgress)
            error("trying to start merge while it is already started")

        return withContext(scope.coroutineContext) {
            _mergeStatus.value = MergeStatus.InProgress
            runCatching {
                val localItems = itemsLocalDataSource.getAll()
                val mergedItems = remoteDataSource.mergeItems(
                    localItems.map { localItem -> localItem.item.id.toInt() },
                    token
                )
                saveMergedItems(localItems, mergedItems)
            }
                .onSuccess { _mergeStatus.value = MergeStatus.Success }
                .onFailure { _mergeStatus.value = MergeStatus.Error(it) }
        }
    }

    override suspend fun mergeItemsDebug(userId: String) {
        if (_mergeStatus.value == MergeStatus.InProgress)
            error("trying to start merge while it is already started")

        return withContext(scope.coroutineContext) {
            _mergeStatus.value = MergeStatus.InProgress
            runCatching {
                val localItems = itemsLocalDataSource.getAll()
                val mergedItems = remoteDataSource.mergeItemsDebug(
                    localItems.map { localItem -> localItem.item.id.toInt() },
                    userId
                )
                saveMergedItems(localItems, mergedItems)
            }
                .onSuccess { _mergeStatus.value = MergeStatus.Success }
                .onFailure { _mergeStatus.value = MergeStatus.Error(it) }
        }
    }

    override fun observeAd(): Flow<Ad?> {
        return adLocalDataSource.observeAd()
    }

    override suspend fun addItemsToGroup(itemIds: List<String>, groupName: String?) {
        setGroupNameToItemsList(itemIds.map { itemsLocalDataSource.getItem(it) }, groupName)
        val currentGroup = currentGroupLocalDataSource.observeCurrentGroup().first()
        if (currentGroup != null && itemsLocalDataSource.getByGroup(currentGroup).isEmpty()) {
            setCurrentGroup(null)
        }
    }

    override suspend fun renameCurrentGroup(newGroupName: String) {
        val currentGroup = currentGroupLocalDataSource.observeCurrentGroup().first()
        if (currentGroup != null) {
            val items = itemsLocalDataSource.getByGroup(currentGroup)
            setGroupNameToItemsList(items, newGroupName)
            setCurrentGroup(newGroupName)
        }
    }

    override fun observeCurrentGroup(): Flow<String?> {
        return currentGroupLocalDataSource.observeCurrentGroup()
    }

    override suspend fun getOrdersChartData(itemId: String): Result<List<Pair<Long, Int>>> {
        return runCatching {
            val item = remoteDataSource.getItemWithFullData(itemId)
            item.info.map {
                it.timeOfCreationInMs to it.ordersCount
            }
        }
    }

    override suspend fun getQuantityChartData(itemId: String): Result<List<Pair<Long, Int>>> {
        return runCatching {
            val item = remoteDataSource.getItemWithFullData(itemId)
            item.info.map {
                it.timeOfCreationInMs to it.sizes.sumOf { sizeRemoteModel ->
                    sizeRemoteModel.quantity
                }
            }
        }
    }

    override suspend fun deleteGroup(groupName: String) {
        val items = itemsLocalDataSource.getByGroup(groupName)
        setGroupNameToItemsList(items, null)
        setCurrentGroup(null)
    }

    override fun observeGroups(): Flow<ItemGroups> {
        return itemsLocalDataSource.observeItemGroups()
    }

    override suspend fun setCurrentGroup(groupName: String?) {
        currentGroupLocalDataSource.setCurrentGroup(groupName)
    }

    private suspend fun setGroupNameToItemsList(
        items: List<ItemWithSizesDBModel>,
        groupName: String?
    ) {
        itemsLocalDataSource.updateItem(
            items.map { item ->
                item.copy(
                    item = item.item.copy(groupName = groupName)
                )
            }
        )
    }

    private suspend fun addItemWithNullableToken(
        url: String,
        token: String?
    ): Result<Unit> {
        return runCatching {
            coroutineScope {
                val newItemDeferred = async { remoteDataSource.addItem(url, token) }
                val localItemsDeferred = async { itemsLocalDataSource.getAll() }
                val currentGroupDeferred = async {
                    currentGroupLocalDataSource.observeCurrentGroup().first()
                }

                val newItem = newItemDeferred.await()
                val localItems = localItemsDeferred.await()
                val currentGroup = currentGroupDeferred.await()

                localItems.find {
                        itemWithSizes ->
                    newItem._id == itemWithSizes.item.id
                }?.let { dbModel ->
                    itemsLocalDataSource.updateItem(
                        listOf(
                            newItem.toDBModel(
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
                            )
                        )
                    )
                    return@coroutineScope
                }

                itemsLocalDataSource.addItem(
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

    private suspend fun refreshItems(itemIds: List<String>): Result<Unit> {
        return runCatching {
            withContext(Dispatchers.Default) {
                val intItemIds = itemIds.map { it.toInt() }
                val response = remoteDataSource.updateItemsAndAd(intItemIds)
                adLocalDataSource.setAd(response.ad?.toDomainModel())
                response.items.map { updatedItem ->
                    async {
                        val localItem = itemsLocalDataSource.getItem(updatedItem._id)
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
                    }
                }
                    .awaitAll()
                    .also { itemsLocalDataSource.updateItem(it) }
            }
        }
    }

    private suspend fun saveMergedItems(
        localItems: List<ItemWithSizesDBModel>,
        mergedItems: List<ItemRemoteModel>
    ) {
        withContext(Dispatchers.Default) {
            val localItemIds = localItems.map { localItem -> localItem.item.id }
            val mergedItemIds = mergedItems.map { remoteItem -> remoteItem._id }
            val itemIdsToAdd = mergedItemIds.minus(localItemIds)
            val itemIdsToUpdate = mergedItemIds.minus(itemIdsToAdd)
            Timber.d("items to add: ${itemIdsToAdd.size}")
            val addingJobDeferred = itemIdsToAdd.map { id ->
                async {
                    val item = mergedItems.find { remoteItem -> remoteItem._id == id }!!
                    itemsLocalDataSource.addItem(
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
            }
            itemIdsToUpdate.map { id ->
                async {
                    val updatedItem = mergedItems.find { remoteItem -> remoteItem._id == id }!!
                    val localItem = localItems.find { localItem -> localItem.item.id == id }!!
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
                            sizeDBModel.sizeName to sizeDBModel.quantity },
                        previousFeedbacks = localItem.item.feedbacks
                    )
                }
            }
                .awaitAll()
                .also { itemsLocalDataSource.updateItem(it) }
            addingJobDeferred.awaitAll()
        }
    }

    private suspend fun deleteItemsWithNullableToken(itemsId: List<String>, token: String?) {
        val currentGroup = currentGroupLocalDataSource.observeCurrentGroup().first()
        if (currentGroup != null &&
            itemsLocalDataSource.getByGroup(currentGroup).size == itemsId.size
        ) {
            setCurrentGroup(null)
        }
        itemsLocalDataSource.deleteItems(itemsId)
        if (token != null) {
            scope.launch {
                runCatching {
                    remoteDataSource.deleteItems(itemsId.map { id -> id.toInt() }, token)
                }.onFailure { Timber.d(it) }
            }
        }
    }
}

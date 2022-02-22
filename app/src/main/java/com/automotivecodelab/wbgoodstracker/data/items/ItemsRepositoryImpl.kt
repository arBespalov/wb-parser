package com.automotivecodelab.wbgoodstracker.data.items

import androidx.lifecycle.LiveData
import com.automotivecodelab.wbgoodstracker.data.ResourcesManager
import com.automotivecodelab.wbgoodstracker.data.items.local.ItemsLocalDataSource
import com.automotivecodelab.wbgoodstracker.data.items.remote.ItemsRemoteDataSource
import com.automotivecodelab.wbgoodstracker.domain.models.Item
import com.automotivecodelab.wbgoodstracker.domain.repositories.ItemsRepository
import com.automotivecodelab.wbgoodstracker.domain.util.Result
import com.automotivecodelab.wbgoodstracker.domain.models.SortingMode
import com.automotivecodelab.wbgoodstracker.log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import java.util.*
import kotlin.Comparator

class ItemsRepositoryImpl(
    private val localDataSource: ItemsLocalDataSource,
    private val remoteDataSource: ItemsRemoteDataSource,
    private val resourcesManager: ResourcesManager
): ItemsRepository {

    override fun observeItems(groupName: String): LiveData<List<Item>> {
        return if (groupName == resourcesManager.getAllItemsString()) {
            localDataSource.observeAll()
        } else {
            localDataSource.observeByGroup(groupName)
        }
    }

    override fun observeSingleItem(id: String): LiveData<Item> {
        return localDataSource.observeItem(id)
    }

    override suspend fun deleteItems(itemsId: Array<String>) {
        deleteItemsWithNullableToken(itemsId, null)
    }

    override suspend fun deleteItems(itemsId: Array<String>, token: String) {
        deleteItemsWithNullableToken(itemsId, token)
    }

    private suspend fun deleteItemsWithNullableToken(itemsId: Array<String>, token: String?) {
        withContext(Dispatchers.IO) {
            localDataSource.deleteItems(itemsId)
            try {
                if (token != null) {
                    remoteDataSource.deleteItems(itemsId.map { id -> id.toInt() }, token)
                }
            } catch (e: Exception) {

            }
        }
    }

    override suspend fun updateItem(item: Item) {
        if (item.local_name == resourcesManager.getAllItemsString()) {
            item.copy(local_name = null)
        } else {
            item
        }.also {
            localDataSource.updateItem(it)
        }
    }

    override suspend fun addItem(url: String, groupName: String): Result<Unit> {
        return addItemWithNullableToken(url, groupName, null)
    }

    override suspend fun addItem(url: String, groupName: String, token: String): Result<Unit> {
        return addItemWithNullableToken(url, groupName, token)
    }

    private suspend fun addItemWithNullableToken(url: String, groupName: String, token: String?): Result<Unit> {
        try {
            withContext(Dispatchers.IO) {
                val newItemDeferred = async { remoteDataSource.addItem(url, token) }
                val localItemsDeferred = async { localDataSource.getAll() }

                val newItem = newItemDeferred.await()
                val localItems = localItemsDeferred.await()

                val nullableGroupName = if (groupName == resourcesManager.getAllItemsString()) null else groupName

                localItems.find { item -> newItem._id == item._id }?.also {
                    val result = handleLocalValues(it, newItem)
                    localDataSource.updateItem(result.copy(local_groupName = nullableGroupName))
                    return@withContext
                }

                localDataSource.addItem(newItem.copy(local_creationTimeInMs = Date().time, local_groupName = nullableGroupName))
            }
            return Result.Success(Unit)
        } catch (e: Exception) {
            log(e.message.toString())
            return Result.Error(Exception(""))//hided error message in prod
        }
    }

    override suspend fun refreshSingleItem(item: Item): Result<Unit> {
        return refreshItems(listOf(item))
    }

    override suspend fun refreshAllItems(): Result<Unit> {
        localDataSource.getAll().also {
            return refreshItems(it)
        }
    }

    override suspend fun syncItems(token: String): Result<Unit> {
        return try {
            withContext(Dispatchers.IO) {
                val serverItemsDeferred = async { remoteDataSource.getItemsForUserId(token) }
                val localItemsDeferred = async { localDataSource.getAll() }

                val serverItems = serverItemsDeferred.await()
                val localItems = localItemsDeferred.await()

                val serverItemIds = serverItems.map { it._id }
                val localItemIds = localItems.map { it._id }

                val itemIdsToDelete = localItemIds.minus(serverItemIds)
                val itemIdsToAdd = serverItemIds.minus(localItemIds)
                val itemIdsToUpdate = serverItemIds.minus(itemIdsToAdd).minus(itemIdsToDelete)

                log("items to delete: ${itemIdsToDelete.size}")
                log("items to add: ${itemIdsToAdd.size}")

                localDataSource.deleteItems(itemIdsToDelete.toTypedArray())

                itemIdsToAdd.forEach { id ->
                    val item = serverItems.find { item -> item._id == id }!!
                    localDataSource.addItem(item.copy(local_creationTimeInMs = Date().time))
                }

                itemIdsToUpdate.map { id ->
                    val updatedItem = serverItems.find { item -> item._id == id }!!

                    handleLocalValues(localItems.find { oldItem -> oldItem._id == id }!!, updatedItem)
                }.also {
                    localDataSource.updateItems(it)
                }
            }
            Result.Success(Unit)
        } catch (e: Exception) {
            log(e.message.toString())
            Result.Error(Exception(""))
        }
    }

    private suspend fun refreshItems(items: List<Item>): Result<Unit> {
        return try {
            withContext(Dispatchers.IO) {
                val itemIds = items.map { item -> item._id.toInt() }
                val updatedItems = remoteDataSource.updateItems(itemIds)

                updatedItems.map { updatedItem ->
                    handleLocalValues(items.find { oldItem -> oldItem._id == updatedItem._id }!!, updatedItem)
                }.also {
                    localDataSource.updateItems(it)
                }
            }
            Result.Success(Unit)
        } catch (e: Exception) {
            log(e.message.toString())
            Result.Error(Exception(""))
        }
    }

    override suspend fun mergeItems(token: String): Result<Unit> {
        try {
            withContext(Dispatchers.IO) {
                val localItems = localDataSource.getAll()
                val mergedItems = remoteDataSource.mergeItems(
                    localItems.map { item -> item._id.toInt() },
                    token
                )

                val localItemIds = localItems.map { item -> item._id }
                val mergedItemIds = mergedItems.map { item -> item._id }

                val itemIdsToAdd = mergedItemIds.minus(localItemIds)
                val itemIdsToUpdate = mergedItemIds.minus(itemIdsToAdd)

                log("items to add: ${itemIdsToAdd.size}")

                itemIdsToAdd.forEach { id ->
                    val item = mergedItems.find { item -> item._id == id }!!
                    localDataSource.addItem(item.copy(local_creationTimeInMs = Date().time))
                }
                itemIdsToUpdate.forEach { id ->
                    val updatedItem = mergedItems.find { item -> item._id == id }!!
                    localItems.find { oldItem -> oldItem._id == id }?.also {
                        localDataSource.updateItem(handleLocalValues(it, updatedItem))
                    }
                }
            }
            return Result.Success(Unit)
        } catch (e: Exception) {
            log(e.message.toString())
            return Result.Error(Exception(""))
        }
    }

    override suspend fun setItemsGroupName(itemIds: List<String>, groupName: String) {
        setGroupNameToItemsList(itemIds.map { localDataSource.getItem(it) }, groupName)
    }


    private suspend fun setGroupNameToItemsList(items: List<Item>, groupName: String) {
        withContext(Dispatchers.IO) {
            if (groupName == resourcesManager.getAllItemsString()) {
                null
            } else {
                groupName
            }.also {
                localDataSource.updateItems(items.map { item -> item.copy(local_groupName = it) })
            }
        }
    }

    override suspend fun deleteGroup(groupName: String) {
        if (groupName != resourcesManager.getAllItemsString()) {
            withContext(Dispatchers.IO) {
                val items = localDataSource.getByGroup(groupName)
                setGroupNameToItemsList(items, resourcesManager.getAllItemsString())
                localDataSource.deleteGroup(groupName)
            }
        }
    }

    override fun getGroups(): Array<String> {
        return localDataSource.getGroups().plus(resourcesManager.getAllItemsString())
    }

    override fun getSortingModeComparator(): Comparator<Item> {
        return when (localDataSource.getSortingMode()) {
            SortingMode.BY_NAME_ASC -> Comparator { o1, o2 -> o1.name.compareTo(o2.name) }
            SortingMode.BY_NAME_DESC -> Comparator { o1, o2 -> o2.name.compareTo(o1.name) }
            SortingMode.BY_DATE_ASC -> Comparator { o1, o2 -> o2.local_creationTimeInMs.compareTo(o1.local_creationTimeInMs) }
            SortingMode.BY_DATE_DESC -> Comparator { o1, o2 -> o1.local_creationTimeInMs.compareTo(o2.local_creationTimeInMs) }
            SortingMode.BY_ORDERS_COUNT -> Comparator { o1, o2 -> o2.info!![0].ordersCount.compareTo(o1.info!![0].ordersCount) }
            SortingMode.BY_ORDERS_COUNT_PER_DAY -> Comparator { o1, o2 -> o2.averageOrdersCountInDay.compareTo(o1.averageOrdersCountInDay) }
        }
    }

    override fun setSortingMode(sortingMode: SortingMode) {
        localDataSource.setSortingMode(sortingMode)
    }

    override fun getCurrentGroup(): String {
        return localDataSource.getCurrentGroup() ?: resourcesManager.getAllItemsString()
    }

    override fun setCurrentGroup(groupName: String) {
        if (groupName == resourcesManager.getAllItemsString()) {
            null
        } else {
            groupName
        }.also {
            localDataSource.setCurrentGroup(it)
        }
    }

    override fun setDefaultGroup() {
        localDataSource.setCurrentGroup(null)
    }

    override fun createNewGroup(groupName: String) {
        if (groupName != resourcesManager.getAllItemsString()) {
            localDataSource.addGroup(groupName)
        }
    }

    override suspend fun getOrdersChartData(itemId: String): Result<List<Pair<Long, Int>>> {
        return try {
            val item = remoteDataSource.getItemWithFullData(itemId)
            Result.Success(item.info!!.map {
                Pair(it.timeOfCreationInMs, it.ordersCount)
            })
        } catch (e: Exception) {
            log(e.message.toString())
            Result.Error(Exception(""))
        }
    }

    private fun handleLocalValues(oldItem: Item, newItem: Item): Item {

        val ordersCountDelta = newItem.info!![0].ordersCount - oldItem.info!![0].ordersCount
        val sOrdersCountDelta: String? = when {
            ordersCountDelta < 0 -> "$ordersCountDelta"
            ordersCountDelta > 0 -> "+$ordersCountDelta"
            else -> null
        }

        val averagePriceDelta = newItem.averagePrice - oldItem.averagePrice
        val sAveragePriceDelta: String? = when {
            averagePriceDelta < 0 -> "$averagePriceDelta"
            averagePriceDelta > 0 -> "+$averagePriceDelta"
            else -> null
        }

        val totalQuantityDelta = newItem.totalQuantity - oldItem.totalQuantity
        val sTotalQuantityDelta: String? = when {
            totalQuantityDelta < 0 -> "$totalQuantityDelta"
            totalQuantityDelta > 0 -> "+$totalQuantityDelta"
            else -> null
        }

        return newItem.copy(
            local_creationTimeInMs = oldItem.local_creationTimeInMs,
            local_name = oldItem.local_name,
            local_groupName = oldItem.local_groupName,
            local_ordersCountDelta = sOrdersCountDelta,
            local_averagePriceDelta = sAveragePriceDelta,
            local_totalQuantityDelta = sTotalQuantityDelta
        )
    }
}
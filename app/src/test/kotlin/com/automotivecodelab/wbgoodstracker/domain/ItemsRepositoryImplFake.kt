package com.automotivecodelab.wbgoodstracker.domain

import com.automotivecodelab.wbgoodstracker.domain.models.Item
import com.automotivecodelab.wbgoodstracker.domain.models.ItemGroups
import com.automotivecodelab.wbgoodstracker.domain.repositories.ItemsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class ItemsRepositoryImplFake(val totalItemsCount: Int) : ItemsRepository {
    override fun observeItems(): Flow<Pair<List<Item>, String?>> {
        TODO("Not yet implemented")
    }

    override fun observeSingleItem(id: String): Flow<Item> {
        TODO("Not yet implemented")
    }

    override suspend fun deleteItems(itemsId: List<String>) {
        TODO("Not yet implemented")
    }

    override suspend fun deleteItems(itemsId: List<String>, token: String) {
        TODO("Not yet implemented")
    }

    override suspend fun setItemLocalName(itemId: String, localName: String?) {
        TODO("Not yet implemented")
    }

    override suspend fun addItem(url: String): Result<Unit> {
        TODO("Not yet implemented")
    }

    override suspend fun addItem(url: String, token: String): Result<Unit> {
        TODO("Not yet implemented")
    }

    override suspend fun refreshSingleItem(itemId: String): Result<Unit> {
        TODO("Not yet implemented")
    }

    override suspend fun refreshAllItems(): Result<Unit> {
        return Result.success(Unit)
    }

    override suspend fun syncItems(token: String): Result<Unit> {
        TODO("Not yet implemented")
    }

    override suspend fun mergeItems(token: String): Result<Unit> {
        TODO("Not yet implemented")
    }

    override suspend fun addItemsToGroup(itemIds: List<String>, groupName: String?) {
        TODO("Not yet implemented")
    }

    override suspend fun getOrdersChartData(itemId: String): Result<List<Pair<Long, Int>>> {
        TODO("Not yet implemented")
    }

    override suspend fun deleteGroup(groupName: String) {
        TODO("Not yet implemented")
    }

    override fun observeGroups(): Flow<ItemGroups> {
        return flow {
            emit(
                ItemGroups(
                    totalItemsQuantity = totalItemsCount,
                    groups = listOf()
                )
            )
        }
    }

    override suspend fun setCurrentGroup(groupName: String?) {
        TODO("Not yet implemented")
    }

    override suspend fun renameCurrentGroup(newGroupName: String) {
        TODO("Not yet implemented")
    }

    override fun observeCurrentGroup(): Flow<String?> {
        TODO("Not yet implemented")
    }

    override suspend fun getQuantityChartData(itemId: String): Result<List<Pair<Long, Int>>> {
        TODO("Not yet implemented")
    }
}

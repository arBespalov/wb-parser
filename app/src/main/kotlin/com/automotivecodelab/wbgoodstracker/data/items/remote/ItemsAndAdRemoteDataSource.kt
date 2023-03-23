package com.automotivecodelab.wbgoodstracker.data.items.remote

import java.io.IOException
import javax.inject.Inject

interface ItemsAndAdRemoteDataSource {
    suspend fun addItem(url: String, idToken: String?): ItemRemoteModel
    suspend fun deleteItems(itemsId: List<Int>, idToken: String)
    suspend fun getItemsAndAdForUserId(idToken: String): UpdateItemResponse
    suspend fun updateItemsAndAd(itemsId: List<Int>): UpdateItemResponse
    suspend fun mergeItems(itemsId: List<Int>, idToken: String): List<ItemRemoteModel>
    suspend fun getItemWithFullData(itemId: String): ItemRemoteModel
    suspend fun mergeItemsDebug(itemsId: List<Int>, userId: String): List<ItemRemoteModel>
}

class ItemsAndAdAndAdRemoteDataSourceImpl @Inject constructor(
    private val api: ServerApi
): ItemsAndAdRemoteDataSource {
    override suspend fun addItem(url: String, idToken: String?): ItemRemoteModel {
        return api.addItem(url, idToken)
    }
    override suspend fun deleteItems(itemsId: List<Int>, idToken: String) {
        val response = api.deleteItems(itemsId, idToken)
        if (!response.isSuccessful) throw IOException()
    }
    override suspend fun getItemsAndAdForUserId(idToken: String): UpdateItemResponse {
        return api.getItemsForUserId(idToken)
    }
    override suspend fun updateItemsAndAd(itemsId: List<Int>): UpdateItemResponse {
        return api.updateItems(itemsId)
    }
    override suspend fun mergeItems(itemsId: List<Int>, idToken: String): List<ItemRemoteModel> {
        return api.mergeItems(itemsId, idToken)
    }
    override suspend fun getItemWithFullData(itemId: String): ItemRemoteModel {
        return api.getItemWithFullData(itemId)
    }

    override suspend fun mergeItemsDebug(itemsId: List<Int>, userId: String): List<ItemRemoteModel> {
        return api.mergeItemsDebug(itemsId, userId)
    }
}

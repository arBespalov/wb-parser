package com.automotivecodelab.wbgoodstracker.data.items.remote

import com.automotivecodelab.wbgoodstracker.domain.models.Item

interface ItemsRemoteDataSource {

    suspend fun addItem(url: String, idToken: String?): Item

    suspend fun deleteItems(itemsId: List<Int>, idToken: String)

    suspend fun getItemsForUserId(idToken: String): List<Item>

    suspend fun updateItems(itemsId: List<Int>): List<Item>

    suspend fun mergeItems(itemsId: List<Int>, idToken: String): List<Item>

    suspend fun getItemWithFullData(itemId: String): Item
}
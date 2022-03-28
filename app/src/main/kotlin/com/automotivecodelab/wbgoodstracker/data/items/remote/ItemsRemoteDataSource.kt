package com.automotivecodelab.wbgoodstracker.data.items.remote

interface ItemsRemoteDataSource {
    suspend fun addItem(url: String, idToken: String?): ItemRemoteModel
    suspend fun deleteItems(itemsId: List<Int>, idToken: String)
    suspend fun getItemsForUserId(idToken: String): List<ItemRemoteModel>
    suspend fun updateItems(itemsId: List<Int>): List<ItemRemoteModel>
    suspend fun mergeItems(itemsId: List<Int>, idToken: String): List<ItemRemoteModel>
    suspend fun getItemWithFullData(itemId: String): ItemRemoteModel
}

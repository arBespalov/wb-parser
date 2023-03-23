package com.automotivecodelab.wbgoodstracker.data.items.remote

import retrofit2.Response
import retrofit2.http.*

interface ServerApi {
    @POST("wbparserapi/add_item")
    suspend fun addItem(
        @Query("url") url: String,
        @Query("id_token") idToken: String?
    ): ItemRemoteModel

    @POST("wbparserapi/delete_items")
    suspend fun deleteItems(
        @Body itemIds: List<Int>,
        @Query("id_token") idToken: String
    ): Response<Unit>

    @POST("wbparserapi/update_items_and_ad")
    suspend fun updateItems(@Body itemIds: List<Int>): UpdateItemResponse

    @GET("wbparserapi/update_items_and_ad")
    suspend fun getItemsForUserId(@Query("id_token") idToken: String): UpdateItemResponse

    @POST("wbparserapi/merge_items")
    suspend fun mergeItems(
        @Body itemIds: List<Int>,
        @Query("id_token") idToken: String
    ): List<ItemRemoteModel>

    @POST("wbparserapi/get_full_data_item")
    suspend fun getItemWithFullData(@Query("id") itemId: String): ItemRemoteModel

    @POST("wbparserapi/merge_items_debug")
    suspend fun mergeItemsDebug(
        @Body itemIds: List<Int>,
        @Query("user_id") userId: String
    ): List<ItemRemoteModel>
}

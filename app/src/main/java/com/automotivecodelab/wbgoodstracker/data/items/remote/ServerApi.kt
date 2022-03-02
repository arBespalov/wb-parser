package com.automotivecodelab.wbgoodstracker.data.items.remote

import com.automotivecodelab.wbgoodstracker.BuildConfig
import com.automotivecodelab.wbgoodstracker.data.util.Wrapper
import com.automotivecodelab.wbgoodstracker.domain.models.Item
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*

interface ServerApi {
    companion object {
        operator fun invoke(): ServerApi {
            return Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(BuildConfig.SERVER_URL)
                .build()
                .create(ServerApi::class.java)
        }
    }

    @Headers("Accept: application/json", "Content-Type: application/json")
    @POST("wbparserapi/add_item")
    suspend fun addItem(
        @Query("url") url: String,
        @Query("id_token") idToken: String?
    ): Response<ItemRemoteModel>

    @Headers("Accept: application/json", "Content-Type: application/json")
    @POST("wbparserapi/delete_items")
    suspend fun deleteItems(
        @Body itemIds: Wrapper<List<Int>>,
        @Query("id_token") idToken: String
    ): Response<Unit>

    @Headers("Accept: application/json", "Content-Type: application/json")
    @POST("wbparserapi/update_items")
    suspend fun updateItems(@Body itemIds: Wrapper<List<Int>>): Response<List<ItemRemoteModel>>

    @Headers("Accept: application/json", "Content-Type: application/json")
    @GET("wbparserapi/update_items")
    suspend fun getItemsForUserId(@Query("id_token") idToken: String)
    : Response<List<ItemRemoteModel>>

    @Headers("Accept: application/json", "Content-Type: application/json")
    @POST("wbparserapi/merge_items")
    suspend fun mergeItems(
        @Body itemIds: Wrapper<List<Int>>,
        @Query("id_token") idToken: String
    ): Response<List<ItemRemoteModel>>

    @Headers("Accept: application/json", "Content-Type: application/json")
    @POST("wbparserapi/get_full_data_item")
    suspend fun getItemWithFullData(@Query("id") itemId: String): Response<ItemRemoteModel>
}

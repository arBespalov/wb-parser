package com.automotivecodelab.wbgoodstracker.data.items.remote

import com.automotivecodelab.wbgoodstracker.BuildConfig
import com.automotivecodelab.wbgoodstracker.data.NetworkStatusListener
import com.automotivecodelab.wbgoodstracker.data.NoInternetConnectionException
import com.automotivecodelab.wbgoodstracker.domain.models.Item
import okhttp3.OkHttpClient
import okhttp3.internal.http2.Header
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import java.time.Duration
import java.util.concurrent.TimeUnit

interface ServerApi {
    companion object {
        operator fun invoke(networkStatusListener: NetworkStatusListener): ServerApi {
            val okHttpClient = OkHttpClient.Builder()
                .addInterceptor { chain ->
                    if (!networkStatusListener.isNetworkAvailable)
                        throw NoInternetConnectionException()
                    chain.proceed(chain.request())
                }
                .callTimeout(15, TimeUnit.SECONDS)
                .build()
            return Retrofit.Builder()
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(BuildConfig.SERVER_URL)
                .build()
                .create(ServerApi::class.java)
        }
    }

    @POST("wbparserapi/add_item")
    suspend fun addItem(
        @Query("url") url: String,
        @Query("id_token") idToken: String?
    ): ItemRemoteModel

    //todo test
    @POST("wbparserapi/delete_items")
    suspend fun deleteItems(
        @Body itemIds: List<Int>,
        @Query("id_token") idToken: String
    ): Response<Unit>

    @POST("wbparserapi/update_items")
    suspend fun updateItems(@Body itemIds: List<Int>): List<ItemRemoteModel>

    @GET("wbparserapi/update_items")
    suspend fun getItemsForUserId(@Query("id_token") idToken: String)
    : List<ItemRemoteModel>

    //todo test
    @POST("wbparserapi/merge_items")
    suspend fun mergeItems(
        @Body itemIds: List<Int>,
        @Query("id_token") idToken: String
    ): List<ItemRemoteModel>

    @POST("wbparserapi/get_full_data_item")
    suspend fun getItemWithFullData(@Query("id") itemId: String): ItemRemoteModel
}

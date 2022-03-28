package com.automotivecodelab.wbgoodstracker.data.items.remote

import com.automotivecodelab.wbgoodstracker.data.NetworkStatusListener
import com.automotivecodelab.wbgoodstracker.data.SafeApiRequest
import com.automotivecodelab.wbgoodstracker.data.util.NoInternetConnectionException
import com.automotivecodelab.wbgoodstracker.data.util.Wrapper
import com.automotivecodelab.wbgoodstracker.domain.models.Item

class ItemsRemoteDataSourceImpl(private val networkStatusListener: NetworkStatusListener) :
    SafeApiRequest(), ItemsRemoteDataSource {

    private val api = ServerApi()

    override suspend fun addItem(url: String, idToken: String?): ItemRemoteModel {
        if (!networkStatusListener.isNetworkAvailable) throw NoInternetConnectionException()
        return apiRequest { api.addItem(url, idToken) }
    }

    override suspend fun deleteItems(itemsId: List<Int>, idToken: String) {
        if (!networkStatusListener.isNetworkAvailable) throw NoInternetConnectionException()
        return apiRequest { api.deleteItems(Wrapper(itemsId), idToken) }
    }

    override suspend fun getItemsForUserId(idToken: String): List<ItemRemoteModel> {
        if (!networkStatusListener.isNetworkAvailable) throw NoInternetConnectionException()
        return apiRequest { api.getItemsForUserId(idToken) }
    }

    override suspend fun updateItems(itemsId: List<Int>): List<ItemRemoteModel> {
        if (!networkStatusListener.isNetworkAvailable) throw NoInternetConnectionException()
        return apiRequest { api.updateItems(Wrapper(itemsId)) }
    }

    override suspend fun mergeItems(itemsId: List<Int>, idToken: String): List<ItemRemoteModel> {
        if (!networkStatusListener.isNetworkAvailable) throw NoInternetConnectionException()
        return apiRequest { api.mergeItems(Wrapper(itemsId), idToken) }
    }

    override suspend fun getItemWithFullData(itemId: String): ItemRemoteModel {
        if (!networkStatusListener.isNetworkAvailable) throw NoInternetConnectionException()
        return apiRequest { api.getItemWithFullData(itemId) }
    }
}

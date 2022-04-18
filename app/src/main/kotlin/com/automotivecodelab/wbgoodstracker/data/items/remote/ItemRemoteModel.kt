package com.automotivecodelab.wbgoodstracker.data.items.remote


data class ItemRemoteModel(
    val _id: String,
    val name: String,
    val url: String,
    val img: String,
    val info: List<ItemInfoRemoteModel>,
    val observingTimeInMs: Long,
    val ordersCountSinceObservingStarted: Int,
    val estimatedIncome: Int,
    val averageOrdersCountInDay: Int,
    val averagePrice: Int,
    val totalQuantity: Int,
    val feedbacks: Int,
    val updateError: Boolean?
)

data class ItemInfoRemoteModel(
    val timeOfCreationInMs: Long,
    val ordersCount: Int,
    val sizes: List<SizeRemoteModel>
)

data class SizeRemoteModel(
    val sizeName: String,
    val quantity: Int,
    val price: Int,
    val priceWithSale: Int,
    val storeIds: List<String>?
)
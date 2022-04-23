package com.automotivecodelab.wbgoodstracker.domain.models

data class Item(
    val id: String,
    val name: String,
    val url: String,
    val img: String,
    val observingTimeInMs: Long,
    val ordersCountSinceObservingStarted: Int,
    val estimatedIncome: Int,
    val averageOrdersCountPerDay: Int,
    val averagePrice: Int,
    val totalQuantity: Int,
    val creationTimestamp: Long,
    val ordersCountDelta: Int,
    val localName: String?,
    val averagePriceDelta: Int,
    val groupName: String?,
    val totalQuantityDelta: Int,
    val lastChangesTimestamp: Long, // for sort by changes
    val lastUpdateTimestamp: Long,
    val ordersCount: Int,
    val sizes: List<Size>,
    val feedbacks: Int,
    val feedbacksDelta: Int,
    val updateError: Boolean?
)

data class Size(
    val sizeName: String,
    val quantity: Int,
    val quantityDelta: Int,
    val price: Int,
    val priceWithSale: Int,
    val storesWithQuantity: String?
)

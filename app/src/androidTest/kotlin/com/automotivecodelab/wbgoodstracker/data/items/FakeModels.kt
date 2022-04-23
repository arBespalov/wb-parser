package com.automotivecodelab.wbgoodstracker.data.items

import com.automotivecodelab.wbgoodstracker.data.items.local.ItemDBModel
import com.automotivecodelab.wbgoodstracker.data.items.local.ItemWithSizesDBModel
import com.automotivecodelab.wbgoodstracker.data.items.local.SizeDBModel
import java.util.*

fun sizeDBModel(itemId: String) = SizeDBModel(
    itemId = itemId,
    price = 1000,
    priceWithSale = 800,
    quantity = 50,
    sizeName = "",
    storesWithQuantity = "",
    quantityDelta = 0
)
fun itemWithSizesDbModel(itemId: String) = ItemWithSizesDBModel(
    item = ItemDBModel(
        id = itemId,
        name = "name",
        url = "example.com",
        img = "example.com/img.png",
        observingTimeInMs = 1000,
        ordersCountSinceObservingStarted = 0,
        estimatedIncome = 0,
        averageOrdersCountPerDay = 0,
        averagePrice = 500,
        totalQuantity = 100,
        creationTimestamp = Date().time,
        ordersCountDelta = 0,
        localName = null,
        averagePriceDelta = 0,
        groupName = null,
        totalQuantityDelta = 0,
        lastUpdateTimestamp = 1000,
        ordersCount = 0,
        feedbacks = 0,
        feedbacksDelta = 0,
        lastChangesTimestamp = 0,
        updateError = null
    ),
    sizes = listOf(
        sizeDBModel(itemId).copy(sizeName = "s"),
        sizeDBModel(itemId).copy(sizeName = "m")
    )
)
